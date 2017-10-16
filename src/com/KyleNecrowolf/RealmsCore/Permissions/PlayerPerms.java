package com.KyleNecrowolf.RealmsCore.Permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import com.KyleNecrowolf.RealmsCore.ConfigValues;
import com.KyleNecrowolf.RealmsCore.Main;
import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Player.PlayerData;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

// Represents the permissions available to a player
public class PlayerPerms extends PlayerData implements Perms {

	private final static HashMap<UUID,PermissionAttachment> permissionAttachments = new HashMap<UUID,PermissionAttachment>();
	private final static HashMap<UUID,PermissionSet> currentPermissionSet = new HashMap<UUID,PermissionSet>();

	
	// If this player is an admin in file - do NOT use this value, use isAdmin() - double-checked admin
	private final boolean isAdmin;
	
	// Permission sets available to this player
	private final PermissionSet defaultSet;
	private final PermissionSet utilitySet;
	private final PermissionSet cheatSet;
	private final List<PermissionSet> otherSets = new ArrayList<PermissionSet>();
	
	// Access to permission sets
	private final List<PermissionSet> instantAccessSets = new ArrayList<PermissionSet>();
	private final List<PermissionSet> adminSupervisedSets = new ArrayList<PermissionSet>();
	
	
	// Constructor
	public PlayerPerms(OfflinePlayer player){
		super(player);

		// Load the playerperms.yml file
		FileConfiguration file = new ConfigAccessor("playerperms.yml").getConfig();
		file.setDefaults(new YamlConfiguration());
		String path = "players." + player.getUniqueId();


		// Check if admin
		isAdmin = file.getBoolean(path+".admin", false);


		//// TODO - THIS NEEDS A REWRITE
		// This needs to be able to properly load permissions in this order: Player's perms > Inherited perms (recursively) > Everyone perms
		// Must be finished before release!

		// Temporary storage of what sets will be
		PermissionSet defaultSet;
		PermissionSet utilitySet;
		PermissionSet cheatSet;

		// First, load values from this set
		defaultSet = loadPermissionSet("default");
		utilitySet = loadPermissionSet("utility");
		cheatSet = loadPermissionSet("cheat");
		for(String set : file.getStringList(path+".permissions.other")){
			otherSets.add(new PermissionSet(set));
		}
		for(String set : file.getStringList(path+".permissions.instantAccess")){
			instantAccessSets.add(new PermissionSet(set));
		}
		for(String set : file.getStringList(path+".permissions.adminSupervised")){
			adminSupervisedSets.add(new PermissionSet(set));
		}

		//Utils.notifyAdmins("Loading explicit permissions for "+player.getName()+". Default: "+defaultSet+", Utility: "+utilitySet+", Cheat: "+cheatSet+", Other: "+otherSets+", InstantAccess: "+instantAccessSets+", AdminSupervised: "+adminSupervisedSets);

		// Then, if any are null, load them from inherited. If they aren't null, inherited sets get copied to otherSets
		String inheritedName = file.getString(path+".inherit");

		if(inheritedName!=null){
			// Load the inherited PlayerPerms
			// PROBLEM - playerperms only works for players, not arbitrary strings
			// Maybe use an interface, and have this playerperm class, and a seperate arbitrary perms class implement it
			Perms inherited;
			try {
				UUID playerUUID = UUID.fromString(inheritedName);
				OfflinePlayer inheritedPlayer = Bukkit.getOfflinePlayer(playerUUID);
				inherited = new PlayerPerms(inheritedPlayer);
			} catch(IllegalArgumentException e){
				inherited = new ArbitraryPerms(inheritedName);
			}

			if(inherited!=null){
				// Load inherited sets
				defaultSet = addInheritedSet(defaultSet, inherited.getDefault());
				utilitySet = addInheritedSet(utilitySet, inherited.getUtility());
				cheatSet = addInheritedSet(cheatSet, inherited.getCheat());

				// Do other sets and access
				otherSets.addAll(inherited.getOtherSets());

				adminSupervisedSets.addAll(inherited.getAdminSupervisedSets());
				instantAccessSets.addAll(inherited.getInstantAccessSets());
			}
		}
		
		//Utils.notifyAdmins("Loading inherited permissions for "+player.getName()+". Default: "+defaultSet+", Utility: "+utilitySet+", Cheat: "+cheatSet+", Other: "+otherSets+", InstantAccess: "+instantAccessSets+", AdminSupervised: "+adminSupervisedSets);		

		// Repeat for "everyone"
		Perms everyone = new ArbitraryPerms("everyone");
		defaultSet = addInheritedSet(defaultSet, everyone.getDefault());
		utilitySet = addInheritedSet(utilitySet, everyone.getUtility());
		cheatSet = addInheritedSet(cheatSet, everyone.getCheat());
		// Do other sets and access
		otherSets.addAll(everyone.getOtherSets());
		adminSupervisedSets.addAll(everyone.getAdminSupervisedSets());
		instantAccessSets.addAll(everyone.getInstantAccessSets());

		//Utils.notifyAdmins("Loading everyone permissions for "+player.getName()+". Default: "+defaultSet+", Utility: "+utilitySet+", Cheat: "+cheatSet+", Other: "+otherSets+", InstantAccess: "+instantAccessSets+", AdminSupervised: "+adminSupervisedSets);

		// Save the final sets
		this.defaultSet =  defaultSet;
		this.utilitySet =  utilitySet;
		this.cheatSet = cheatSet;



		/* OLD LOADING STUFF THAT CAN'T DO INHERIT PROPERLY
		// Get sets
		defaultSet = new PermissionSet(getStringFromFile(file, path, inheritedPath, ".permissions.default"));
		utilitySet = new PermissionSet(getStringFromFile(file, path, inheritedPath, ".permissions.utility"));
		cheatSet = new PermissionSet(getStringFromFile(file, path, inheritedPath, ".permissions.cheat"));

		for(String set : getStringListFromFile(file, path, inheritedPath, ".permissions.other")){
			otherSets.add(new PermissionSet(set));
		}

		
		// Get access to sets
		for(String set : getStringListFromFile(file, path, inheritedPath, ".permissions.instantAccess")){
			instantAccessSets.add(new PermissionSet(set));
		}
		for(String set : getStringListFromFile(file, path, inheritedPath, ".permissions.adminSupervised")){
			adminSupervisedSets.add(new PermissionSet(set));
		}*/
	}
	
	
	/* Easier way to get the perm sets
	private String getStringFromFile(FileConfiguration file, String pathPrefix, String inheritedPathPrefix, String path){
		return file.getString(pathPrefix+path, file.getString(inheritedPathPrefix+path));
	}
	private List<String> getStringListFromFile(FileConfiguration file, String pathPrefix, String inheritedPathPrefix, String path){
		List<String> list = file.getStringList(pathPrefix+path);
		list.addAll(file.getStringList(inheritedPathPrefix+path));
		return list;
	}*/
	// Load a permission set from file
	private PermissionSet loadPermissionSet(String setType){
		//// Take a string and get the perm set
		FileConfiguration file = new ConfigAccessor("playerperms.yml").getConfig();
		String setName = file.getString("players."+getUniqueId()+".permissions."+setType, null);

		if(setName!=null) return new PermissionSet(setName);
		else return null;
	}
	// Add inherited set
	private PermissionSet addInheritedSet(PermissionSet target, PermissionSet source){
		if(target==null) target = source;
		else if(source!=null) otherSets.add(source);
		return target;
	}
	
	
	//// Retrieving values
	// Basic data
	public boolean isAdmin(){
		// If they're online, double-check admin status
		if(isOnline()){
			Player player = (Player) getPlayer();

			// If perms are disabled, just check the permission realms.admin - do not double-check
			if(!ConfigValues.multiCheckAdmins || !ConfigValues.permsEnabled){
				return player.hasPermission("realms.admin");
			}

			// First check - Make sure they have permission realms.admin
			if(player.hasPermission("realms.admin")){
				// Second check - Make sure they're listed as admin in file
				if(isAdmin){
					return isAdmin;
				} else {
					// If second check fails, warn admins
					Utils.notifyAdminsError(getPlayer().getName()+Utils.errorText+" failed security check (not listed as admin in playerperms.yml). This could indicate they have improperly received admin permissions. Check the permissions files.");
					return false;
				}
			}
		}
		// If offline or checks fail, return false
		return false;
	}
	
	// Permission sets
	public PermissionSet getDefault(){
		return defaultSet;
	}
	public PermissionSet getUtility(){
		return utilitySet;
	}
	public PermissionSet getCheat(){
		return cheatSet;
	}
	public List<PermissionSet> getOtherSets(){
		return otherSets;
	}
	public List<PermissionSet> getAllSets(){
		List<PermissionSet> allSets = otherSets;
		if(defaultSet!=null) allSets.add(defaultSet);
		if(utilitySet!=null) allSets.add(utilitySet);
		if(cheatSet!=null) allSets.add(cheatSet);
		return allSets;
	}
	private List<String> getAllSetsNames(){
		List<String> setNames = new ArrayList<String>();
		for(PermissionSet set : getAllSets()){
			setNames.add(set.getName());
		}
		return setNames;
	}

	public PermissionSet getCurrentSet(){
		return currentPermissionSet.get(getUniqueId());
	}


	// Getting set access
	public List<PermissionSet> getInstantAccessSets(){
		return instantAccessSets;
	}
	private List<String> getInstantAccessSetsNames(){
		List<String> setNames = new ArrayList<String>();
		for(PermissionSet set : instantAccessSets){
			setNames.add(set.getName());
		}
		return setNames;
	}
	public List<PermissionSet> getAdminSupervisedSets(){
		return instantAccessSets;
	}
	private List<String> getAdminSupervisedSetsNames(){
		List<String> setNames = new ArrayList<String>();
		for(PermissionSet set : adminSupervisedSets){
			setNames.add(set.getName());
		}
		return setNames;
	}
	
	
	// Display permission info to a player
	boolean displayPermissionInfo(CommandSender sender){
		// Prepare a prompt
		Prompt prompt = new Prompt();

		prompt.addQuestion(Utils.infoText+"--- Player Permissions: "+Utils.messageText+getDisplayName()+Utils.infoText+" ---");
		
		// Username and UUID
		prompt.addAnswer("Username: "+getName(), "command_playerinfo "+getName());
		prompt.addAnswer("UUID: "+getUniqueId(), "url_https://mcuuid.net/?q="+getUniqueId());
		
		// Permission sets
		prompt.addAnswer("Current set: "+getCurrentSet(), "command_permissions set info "+getCurrentSet());

		prompt.addAnswer("Default set: "+defaultSet, "command_permissions set info "+defaultSet);
		prompt.addAnswer("Utility set: "+utilitySet, "command_permissions set info "+utilitySet);
		prompt.addAnswer("Cheat set: "+cheatSet, "command_permissions set info "+cheatSet);
		
		for(PermissionSet set : otherSets){
			prompt.addAnswer("Other set: "+set, "command_permissions set info "+set);
		}
		
		// Access to sets
		StringBuilder instantAccessSetsList = new StringBuilder();
		for(PermissionSet set: instantAccessSets){
			instantAccessSetsList.append(set).append(", ");
		}
		if(instantAccessSetsList.length()>3) prompt.addAnswer("Always available: "+instantAccessSetsList, "");
		
		StringBuilder adminSupervisedSetsList = new StringBuilder();
		for(PermissionSet set: adminSupervisedSets){
			adminSupervisedSetsList.append(set).append(", ");
		}
		if(adminSupervisedSetsList.length()>3) prompt.addAnswer("Available with admin supervision: "+adminSupervisedSetsList, "");
		
		// Display the prompt
		prompt.display(sender);
		
		return true;
	}
	
	
	//// Applying permissions
	// Applying a set - do NOT call these methods directly, call switchSet instead as it includes most of the security checks
	private boolean applySet(PermissionSet set){
		// Check that set exists
		if(set==null){
			Utils.notifyAdminsError(getName()+" could not be switched to a permission set, the set does not exist.");
			return false;
		}

		// Check if player is online
		if(!isOnline()){
			Utils.notifyAdminsError(getName()+" could not be switched to "+set.getName()+" permissions, they are offline.");
			return false;
		}
		
		Player player = (Player) getPlayer();
		
		// Check if player has access to this set
		if(!getAllSetsNames().contains(set.getName())){
			player.sendMessage(Utils.errorText+"Your permissions could not be applied (No access to "+set.getName()+"). Ask an "+ConfigValues.adminName+" for help.");
			Utils.notifyAdminsError(player.getDisplayName()+Utils.errorText+" could not be switched to "+set+" permissions, they do not have access.");
			return false;
		}
		
		// After checks finish, log to console
		Bukkit.getLogger().info(ChatColor.stripColor("Applied "+set+" permissions to "+player.getDisplayName()));
		
		// Remove previous permissions, and prepare a new attachment
		PermissionAttachment oldPerms = permissionAttachments.get(player.getUniqueId());
		if(oldPerms!=null) player.removeAttachment(oldPerms);

		PermissionAttachment playerPerms = player.addAttachment(Main.plugin);
		
		// Apply the new permissions
		for(String permissionNode : set.getTotalPermissions()){
			playerPerms.setPermission(permissionNode, true);
		}

		// Store data for later use
		permissionAttachments.put(player.getUniqueId(), playerPerms);
		currentPermissionSet.put(player.getUniqueId(), new PermissionSet(set.getName()));
		
		return true;
	}

	// Apply default set - on join
	public boolean applyDefaultSetOnJoin(){
		// Check if player is online
		if(!isOnline()){
			Utils.notifyAdminsError(getName()+" could not be switched to their default permissions, they are offline.");
			return false;
		}
		
		Player player = (Player) getPlayer();

		// De-op the player
		player.setOp(false);

		// Prepare permission attachment
		PermissionAttachment playerPerms = player.addAttachment(Main.plugin);
		permissionAttachments.put(player.getUniqueId(), playerPerms);

		// applySet can be called here because no checks need to be performed for the default set
		return applySet(defaultSet);
	}


	//// Revoking permissions
	boolean revokePermissions(){
		// Check if player is online
		if(!isOnline()){
			Utils.notifyAdminsError(getName()+" could not have permissions revoked, they are offline.");
			return false;
		}
		
		Player player = (Player) getPlayer();

		Utils.notifyAdminsError("Permissions revoked from "+player.getDisplayName()+Utils.infoText+" - RealmsCore is no longer applying any permissions to this player.");
		return removePermissions();
	}
	// Remove permissions on quit
	public boolean removePermissions(){
		// Check if player is online
		if(!isOnline()){
			Utils.notifyAdminsError(getName()+" could not have permissions revoked, they are offline.");
			return false;
		}
		
		Player player = (Player) getPlayer();

		// De-op the player
		player.setOp(false);

		// Remove the permission attachment, and clear their current set
		player.removeAttachment(permissionAttachments.get(player.getUniqueId()));
		
		permissionAttachments.remove(player.getUniqueId());
		currentPermissionSet.remove(player.getUniqueId());

		return true;
	}


	//// Switching permission set
	boolean switchSet(PermissionSet set, CommandSender sender){
		// Check that set exists
		if(set==null){
			CommandSender player = (sender==null) ? (CommandSender) getPlayer() : sender;
			Utils.sendActionBar(player, Utils.errorText+"Permission set not found.");
			return false;
		}
		
		// Check if player is online
		if(!isOnline()){
			Utils.notifyAdminsError(getName()+" could not be switched to "+set.getName()+" permissions, they are offline.");
			return false;
		}

		
		Player player = (Player) getPlayer();

		// If sender is not null, check if they're console, or an admin player
		if(sender!=null){
			if(PermsUtils.isDoubleCheckedAdmin(sender)){
				// Notify admins and player
				Utils.notifyAdmins(player.getDisplayName()+Utils.infoText+" was switched to "+set.getName()+" permissions by "+sender.getName()+".");
				Utils.sendActionBar(player, "Switched to "+set.getName()+" permissions");

				// Apply the permission set
				return applySet(set);
			}
			Utils.notifyAdminsError(sender.getName()+Utils.errorText+" failed security check (switching "+player.getName()+" to "+set+" permissions).");
			return Error.NO_PERMISSION.displayChat(sender);
		}

		// If sender is null, player is switching their own set
		// Check if it's in their list of sets
		if(getAllSetsNames().contains(set.getName())){
			// If it's also in instant access, switch the set
			if(getInstantAccessSetsNames().contains(set.getName())){
				// Notify admins and player
				Utils.notifyAdmins(player.getDisplayName()+Utils.infoText+" switched to "+set.getName()+" permissions.");
				Utils.sendActionBar(player, "Switched to "+set.getName()+" permissions");

				// Apply the permission set
				return applySet(set);
			}

			// If it's in admin supervised, check if an admin is online
			if(getAdminSupervisedSetsNames().contains(set.getName())){
				if(!Utils.getOnlineAdmins().isEmpty()){
					// Notify admins and player
					Utils.notifyAdmins(player.getDisplayName()+Utils.infoText+" switched to "+set.getName()+" permissions.");
					Utils.sendActionBar(player, "Switched to "+set.getName()+" permissions");

					// Apply the permission set
					return applySet(set);
				}
				// If no admins online, notify console and player
				Utils.notifyAdminsError(player.getDisplayName()+Utils.errorText+" could not be switched to "+set.getName()+" permissions, no admin online.");
				Utils.sendActionBar(player, Utils.errorText+set.getName()+" permissions are unavailable, an "+ConfigValues.adminName+" must be online");
				return true;
			}

			// Otherwise, only an admin can switch the player to this set
			Utils.sendActionBar(player, "An "+ConfigValues.adminName+" must approve your use of "+set.getName()+" permissions");

			// Prompt all online admins
			Prompt prompt = new Prompt();
			prompt.addQuestion(player.getDisplayName()+" would like to use "+set.getName()+" permissions.");

			prompt.addAnswer("Allow use of permissions", "command_permissions set "+player.getName()+" "+set.getName());
			prompt.addAnswer("View details for "+set.getName(), "command_permissions set info "+set.getName());
			
			List<Player> admins = Utils.getOnlineAdmins();
			admins.remove(player);
			for(Player admin : admins) prompt.display(admin);

			return true;
		}

		// If player doesn't have access to that set
		Utils.notifyAdminsError(player.getDisplayName()+Utils.errorText+" was denied access to "+set.getName()+" permissions.");
		Utils.sendActionBar(player, Utils.errorText+"You don't have access to "+set.getName()+" permissions!");

		return false;
	}

	// Switching own set
	boolean switchSet(PermissionSet set){
		return switchSet(set, null);
	}

	// Apply default set
	public boolean applyDefaultSet(){
		return switchSet(defaultSet);
	}
	// Apply utility set
	public boolean applyUtilitySet(){
		return switchSet(utilitySet);
	}
	// Apply cheat set
	public boolean applyCheatSet(){
		return switchSet(cheatSet);
	}
}
