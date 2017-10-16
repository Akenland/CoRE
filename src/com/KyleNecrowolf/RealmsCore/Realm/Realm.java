package com.KyleNecrowolf.RealmsCore.Realm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.ConfigValues;
import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Player.PlayerData;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

// Represents a realm/nation/faction
public class Realm {

	private final String name;
	
	private String fullName;
	private ChatColor color;
	private String tagline;
	private Realm parent;
	
	private boolean loaded;

	
	//// Constructor
	public Realm(String name){
		this.name =  name.toLowerCase();
		
		// Load the realms file
		FileConfiguration file = new ConfigAccessor("realms.yml").getConfig();
		
		// Check if realm exists in file
		loaded = file.contains("realms."+name);
		
		// Get the full name of the realm (for example, "Thaindom of Eborin" vs. "Eborin"), will be same as name if it doesn't exist
		fullName = file.getString("realms."+name+".fullname", name);
		
		// Get the color, or grey if none is found
		try{color = ChatColor.valueOf(file.getString("realms."+ name +".color", "GRAY"));}
		catch(IllegalArgumentException e){color = ChatColor.GRAY;}

		// Get the tagline of the realm
		tagline = file.getString("realms."+name+".tagline");
		
		// Get the parent realm
		if(file.contains("realms."+name+".parent")){
			parent = new Realm(file.getString("realms."+name+".parent"));
		}
	}
	
	
	//// Methods
	// Realm name
	public String getName(){
		return name;
	}
	
	// Realm full name
	public String getFullName(){
		return color+fullName;
	}
	public void setFullName(String fullName){
		this.fullName = fullName;
		save();
	}
	
	// Realm color
	public ChatColor getColor(){
		return color;
	}
	public void setColor(ChatColor color){
		this.color = color;
		save();
	}

	// Realm tagline
	public String getTagline(){
		return tagline;
	}
	public void setTagline(String tagline){
		this.tagline = tagline;
		save();
	}

	// If realm exists in file
	public boolean exists(){
		return loaded;
	}

	// Save to file
	private void save(){
		// Load the realms file
		ConfigAccessor file = new ConfigAccessor("realms.yml");
		
		// Save full name of the realm
		if(!fullName.equals(name)) file.getConfig().set("realms."+name+".fullname", fullName);
		
		// Set the color, or grey if none is found
		if(!ChatColor.GRAY.equals(color)) file.getConfig().set("realms."+ name +".color", color.name());

		// Get the tagline of the realm
		file.getConfig().set("realms."+name+".tagline", tagline);
		
		// Get the parent realm
		if(parent!=null) file.getConfig().set("realms."+name+".parent", parent.name);

		// Save the file
		file.saveConfig();
	}
	
	// Parent realm
	public Realm getParent(){
		//return (parent!=null) ? parent : this;
		return parent;
	}
	public void setParent(Realm parent){
		this.parent = parent;
		save();
	}
	// Get top-most parent realm (Waramon, Eborin, Sholkingham, Runnach)
	public Realm getTopParent(){
		Realm topParent = getParent();
		// Keep getting the parent until there isn't one
		while(topParent != null){
			// See what the new top parent would be
			Realm newTopParent = topParent.getParent();
			// If it doesn't exist, return the last parent found
			if(newTopParent==null) return topParent;
			// Otherwise, repeat until we've found the top-most parent
			topParent = newTopParent;
		}
		
		// If top parent is null, then this realm is top parent, return it
		return this;
	}


	// Get online players that are part of this realm
	public List<Player> getOnlinePlayers(){
		List<Player> players = new ArrayList<Player>();
		// Check if each online player is a member of this realm
		for(Player player : Bukkit.getOnlinePlayers()){
			if(new PlayerData(player).getRealm().getName().equals(name)) players.add(player);
		}
		return players;
	}
	
	
	// Display realm information to a CommandSender
	public boolean displayInfo(CommandSender sender){
		// If realm doesn't exist in file, show an error
		if(!loaded) return Error.REALM_NOT_FOUND.displayActionBar(sender);

		Realm topParent = getTopParent();

		Prompt prompt = new Prompt();

		prompt.addQuestion(Utils.infoText+"--- Realm Info: "+getFullName()+Utils.infoText+" ---");
		if(tagline!=null) prompt.addQuestion(" "+ChatColor.GRAY+ChatColor.ITALIC.toString()+tagline);
		if(parent!=null && (!ConfigValues.enableWolfiaFeatures || parent!=topParent)) prompt.addQuestion("- Faction of the "+parent.getFullName());
		if(ConfigValues.enableWolfiaFeatures) prompt.addQuestion("- Located in "+topParent.getFullName());

		List<Player> onlinePlayers = getOnlinePlayers();
		if(!onlinePlayers.isEmpty()){
			prompt.addQuestion(Utils.infoText+"-- Online players --");
			for(Player player : onlinePlayers){
				// Get their title, if it exists
				String title = new PlayerData(player).getTitle();
				title = (title!="") ? title+" " : "";
				prompt.addAnswer(getColor()+title+player.getDisplayName(), "command_player "+player.getName());
			}
		}

		prompt.display(sender);
		return true;
	}


	// Check if a player has edit access (officer) for this realm
	public boolean isOfficer(OfflinePlayer player){
		PlayerData data = new PlayerData(player);

		// Figure out whether this player is a leader of this realm, or any parent realms
		boolean hasEditAccess = false;
		String playerRealmName = data.getRealm().getName();

		Realm realmToCheck = this;
		// Keep getting the parent until there isn't one
		while(realmToCheck!=null && !hasEditAccess){
			// If player does not have edit access at all, don't check anything
			if(!data.isRealmOfficer()) break;
			// Check this realm
			if(playerRealmName.equalsIgnoreCase(realmToCheck.getName())) hasEditAccess = true;
			// Otherwise check the parent realm, and loop
			else realmToCheck =  realmToCheck.getParent();
		}

		return hasEditAccess;
	}


	// Get sub-realms (child realms)
	public Collection<Realm> getChildRealms(){
		Collection<Realm> realms = new ArrayList<Realm>();
		for(String realmName : new ConfigAccessor("realms.yml").getConfig().getConfigurationSection("realms").getKeys(false)){
			Realm realm = new Realm(realmName);
			Realm realmToCheck = realm.getParent();
			// Keep getting the parent until there isn't one
			while(realmToCheck!=null){
				if(realmToCheck.getName().equals(name)){
					realms.add(realm);
					break;
				}
				realmToCheck = realmToCheck.getParent();
			}
		}
		return realms;
	}
}
