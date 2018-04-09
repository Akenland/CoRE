package com.kylenanakdewa.core.Permissions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.CommandSender;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.ConfigAccessor;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;

// Represents a set of permissions that may be applied to a player (PlayerPerms)
public class PermissionSet {

	// Name of this permission set
	private final String name;
	
	private boolean loaded;
	
	// List of sets to inherit permissions from
	private List<PermissionSet> inheritedSets = new ArrayList<PermissionSet>();
	
	// The permissions contained in this set, not including inherited permissions
	private List<String> permissions = new ArrayList<String>();
	
	
	// Constructor
	public PermissionSet(String name){
		this.name = name.toLowerCase();
	}
	
	
	// Load set from file
	private void load(){
		// If already loaded, don't load it again
		if(loaded) return;
		
		// Load the permsets.yml file
		FileConfiguration file = new ConfigAccessor("permsets.yml").getConfig();
		
		// Check if this set can be found in file
		if(file.contains("sets."+name)){
			
			// Get inherited sets
			List<String> inheritedSetNames = file.getStringList("sets."+name+".inheritedSets");
			for(String setName : inheritedSetNames){
				inheritedSets.add(new PermissionSet(setName));
			}
			
			// Get the permissions in this set
			permissions = file.getStringList("sets."+name+".permissions");

			loaded = true;
		}
		// If set could not be loaded, show an error to admins and console
		else {
			Utils.notifyAdminsError("Permission set "+name+" could not be loaded by RealmsCore! Check the permsets.yml file.");
		}
	}
	
	
	// Get the name of this set
	public String getName(){
		return name;
	}
	@Override
	public String toString(){
		return name;
	}

	
	// Get a list of permission sets inherited by this set
	public List<PermissionSet> getInheritedSets(){
		load();
		return inheritedSets;
	}
	
	
	// Get a list of permissions contained in this set, not including inherited permissions
	public List<String> getPermissions(){
		load();
		return permissions;
	}
	
	// Get a list of permissions contained in this set, including inherited permissions
	public List<String> getTotalPermissions(){
		load();
		
		// Add the permissions from this set
		List<String> totalPermissions = permissions;
		
		// Add the permissions from each inherited set
		for(PermissionSet set : inheritedSets){
			totalPermissions.addAll(set.getTotalPermissions());
		}
		
		return totalPermissions;
	}

	// See if a permission is included in this set
	public boolean hasPermission(String permission){
		return getTotalPermissions().contains(permission);
	}


	// Display information about this set
	public void displayInfo(CommandSender sender){
		load();
		
		Prompt info = new Prompt();
		info.addQuestion(CommonColors.INFO+"--- Permission Set: "+CommonColors.MESSAGE+name+CommonColors.INFO+" ---");

		for(PermissionSet inherited : inheritedSets){
			info.addAnswer("Inherits "+inherited.name, "command_permissions set info "+inherited.name);
		}

		info.addAnswer("Permissions: "+permissions, "");

		info.display(sender);
	}
}
