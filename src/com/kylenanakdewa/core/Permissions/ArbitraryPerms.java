package com.kylenanakdewa.core.permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import com.kylenanakdewa.core.common.ConfigAccessor;

// Represents an entry in PlayerPerms listed under an arbitrary name, instead of a player UUID
class ArbitraryPerms implements Perms {

    // Name of the entry
    private final String name;

    // Sets contained in this ArbitraryPerms
	private final PermissionSet defaultSet;
	private final PermissionSet utilitySet;
	private final PermissionSet cheatSet;
	private final List<PermissionSet> otherSets = new ArrayList<PermissionSet>();

	// Access to permission sets
	private final List<PermissionSet> instantAccessSets = new ArrayList<PermissionSet>();
	private final List<PermissionSet> adminSupervisedSets = new ArrayList<PermissionSet>();


    // Constructor
    ArbitraryPerms(String name){
        this.name =  name;

        // Load the playerperms.yml file
		FileConfiguration file = new ConfigAccessor("playerperms.yml").getConfig();
        String path = "players." + name;

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


		// Repeat for "everyone"
		if(!name.equalsIgnoreCase("everyone")){
			Perms everyone = new ArbitraryPerms("everyone");
			defaultSet = addInheritedSet(defaultSet, everyone.getDefault());
			utilitySet = addInheritedSet(utilitySet, everyone.getUtility());
			cheatSet = addInheritedSet(cheatSet, everyone.getCheat());
			// Do other sets and access
			otherSets.addAll(everyone.getOtherSets());
			adminSupervisedSets.addAll(everyone.getAdminSupervisedSets());
			instantAccessSets.addAll(everyone.getInstantAccessSets());
		}


		// Save the final sets
		this.defaultSet =  defaultSet;
		this.utilitySet =  utilitySet;
		this.cheatSet = cheatSet;
    }

    // Load a permission set from file
	private PermissionSet loadPermissionSet(String setType){
		//// Take a string and get the perm set
		FileConfiguration file = new ConfigAccessor("playerperms.yml").getConfig();
		String setName = file.getString("players."+name+".permissions."+setType);

		if(setName!=null) return new PermissionSet(setName);
		else return null;
	}
	// Add inherited set
	private PermissionSet addInheritedSet(PermissionSet target, PermissionSet source){
		if(target==null) target = source;
		else if(source!=null) otherSets.add(source);
		return target;
	}


    // Getting permission sets
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
		allSets.add(defaultSet); allSets.add(utilitySet); allSets.add(cheatSet);
		return allSets;
    }

    public List<PermissionSet> getAdminSupervisedSets(){
        return adminSupervisedSets;
    }
    public List<PermissionSet> getInstantAccessSets(){
        return instantAccessSets;
    }
}