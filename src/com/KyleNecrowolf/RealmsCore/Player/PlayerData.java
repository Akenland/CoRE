package com.KyleNecrowolf.RealmsCore.Player;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.ConfigValues;
import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Extras.AFKListener;
import com.KyleNecrowolf.RealmsCore.Permissions.PlayerPerms;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;
import com.KyleNecrowolf.RealmsCore.Realm.Realm;

public class PlayerData {
	
	private final OfflinePlayer player;
	private FileConfiguration file;
	private ConfigAccessor config;
	
	private boolean loaded;
	
	//// Constructor
	public PlayerData(OfflinePlayer player) {
		// Save the information in this object
		this.player = player;
	}
	
	
	//// Loading
	private void load(){
		// If already loaded, don't load again
		if(loaded) return;
		
		// Load the playerdata file
		config = new ConfigAccessor("players\\" + player.getUniqueId() + ".yml");
		file = config.getConfig();
		
		// If it exists, set loaded to true
		if(file.contains("playerdata")){
			loaded = true;
		}

		// Some basic values needed in all playerdata files, such as username and IP - only saved if file is modified
		file.set("playerdata.username", player.getName());
		if(player.isOnline()) file.set("playerdata.ip", ((Player) player).getAddress().getAddress().getHostAddress());
		if(!file.contains("playerdata.firstlogin")) file.set("playerdata.firstlogin", player.getFirstPlayed());
		file.set("playerdata.lastlogin", player.getLastPlayed());
	}
	
	
	//// Getting data
	// Getting the file for other plugins to read from
	public FileConfiguration getFile(){
		load();
		return file;
	}

	
	// Getting the player object if needed
	public OfflinePlayer getPlayer(){
		return player;
	}
	public boolean isOnline(){
		return player.isOnline();
	}
	
	// Convienience methods for common values
	public String getName(){
		return player.getName();
	}
	public UUID getUniqueId(){
		return player.getUniqueId();
	}
	public String getDisplayName(){
		return getFile().getString("playerdata.displayname",getName());
	}
	public String getTitle(){
		return getFile().getString("playerdata.title","");
	}
	public Realm getRealm(){
		return new Realm(getFile().getString("playerdata.realm",""));
	}
	public boolean isRealmOfficer(){
		return getFile().getBoolean("playerdata.realmofficer");
	}
	public String getIP(){
		return getFile().getString("playerdata.ip","Unknown");
	}
	public long getLastLogin(){
		return getFile().getLong("playerdata.lastlogin");
	}
	public LocalDateTime getLastLoginDate(){
		long lastlogin = getLastLogin();
		return lastlogin!=0 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(lastlogin), ZoneId.systemDefault()) : null;
	}
	public String getLastLoginString(){
		LocalDateTime lastLogin = getLastLoginDate();
		return lastLogin!=null ? lastLogin.format(DateTimeFormatter.ofPattern("MMM d, uuuu, h:mm:ss a")) : "Unknown";
	}
	public long getFirstLogin(){
		return getFile().getLong("playerdata.firstlogin");
	}
	public LocalDateTime getFirstLoginDate(){
		long firstLogin = getFirstLogin();
		return firstLogin!=0 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(firstLogin), ZoneId.systemDefault()) : null;
	}
	public String getFirstLoginString(){
		LocalDateTime firstLogin = getFirstLoginDate();
		return firstLogin!=null ? firstLogin.format(DateTimeFormatter.ofPattern("MMM d, uuuu, h:mm:ss a")) : "Unknown";
	}
	public boolean isAdmin(){
		return new PlayerPerms(player).isAdmin();
	}

	// Get their display name and set it
	public void loadDisplayName(){
		if(!ConfigValues.formatChat || !isOnline()) return;
		
		String displayName = getDisplayName();
		((Player) player).setDisplayName(displayName);

		// If player is admin, add the admin prefix
		if(isAdmin()) ((Player) player).setPlayerListName(ConfigValues.adminPrefix+ChatColor.GRAY+displayName);
		else ((Player) player).setPlayerListName(displayName);
	}

	// Get the chat format and load it into the hashmap
	public void loadChatFormat(){
		// Default chat format
		String chatFormat = ChatColor.GRAY+"<%s> "+ChatColor.RESET+"%s";

		load();

		// Get their data if loaded
		if(loaded){
			// Get their title, if it exists
			String title = getTitle();
			title = (title!="") ? title+" " : "";

			// If they're an admin, add the prefix
			String adminPrefix = isAdmin() ? ConfigValues.adminPrefix : "";

			// Get their realm (color)
			Realm realm = getRealm();
			ChatColor realmColor = realm.getColor();
			ChatColor parentRealmColor = realm.getTopParent().getColor();

			// Put together the final chat format, and save it in the hashmap
			chatFormat = parentRealmColor+"<"+adminPrefix+realmColor+title+"%s"+parentRealmColor+"> "+ChatColor.RESET+"%s";
		}

		PlayerListener.chatFormats.put(player.getUniqueId(), chatFormat);
	}
	
	
	// Display this player's data to a CommandSender
	boolean displayInfo(CommandSender sender){
		Prompt prompt = new Prompt();
		
		prompt.addQuestion(Utils.infoText+"--- Player Info: "+Utils.messageText+getDisplayName()+Utils.infoText+" ---");
		
		// Realm info
		Realm realm = getRealm();
		String title = getTitle();
		if(!realm.getName().equals("")){
			title = (!title.equals("")) ? title : "Member";
			prompt.addAnswer(realm.getColor()+title+" of the "+realm.getFullName(), "command_realm "+realm.getName());
		}

		// Check if AFK
		if(isOnline()){
			String afkMessage = AFKListener.afkPlayers.get(player.getUniqueId());
			if(afkMessage!=null) prompt.addAnswer("AFK"+afkMessage, ""); 
		}

		// Check if admin
		if(isAdmin() || (!isOnline() && new PlayerPerms(player).getDefault().hasPermission("realms.admin"))) prompt.addAnswer(ConfigValues.adminColor+"Server "+ConfigValues.adminName, "");

		// Basic info
		prompt.addAnswer("Username: "+player.getName()+" "+Utils.infoText+player.getUniqueId(), "url_https://mcuuid.net/?q="+player.getName());
		//prompt.addAnswer("UUID: "+player.getUniqueId(), "url_https://mcuuid.net/?q="+player.getUniqueId());
		prompt.addAnswer("First joined "+getFirstLoginString(), "");
		prompt.addAnswer("Last joined "+getLastLoginString(), "");

		// Admin info
		if(sender.hasPermission("realms.player.admin")){
	
			prompt.addAnswer("IP: "+getIP(), "url_http://www.whatsmyip.org/ip-geo-location/?ip="+getIP().substring(getIP().indexOf('/')+1));
			
			// Game info
			if(player.isOnline()){
				Location loc = ((Player) player).getLocation();
				prompt.addAnswer("Location: "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ()+", "+loc.getWorld().getName(), "command_tp "+player.getName());
				prompt.addAnswer(((Player) player).getGameMode()+" mode", "");
				prompt.addAnswer("Permissions: "+new PlayerPerms(player).getCurrentSet().getName(), "command_permissions player "+player.getName());
			}
		}
		
		prompt.display(sender);
		
		return true;
	}
	
	
	//// Saving data
	// Set a value, without saving the file immediately
	public void set(String path, Object value){
		load();
		file.set(path, value);
	}
	// Set a value and save the file
	public void save(String path, Object value){
		set(path, value);
		save();
	}
	// Save the file
	public void save(){
		config.saveConfig();
	}
	
}
