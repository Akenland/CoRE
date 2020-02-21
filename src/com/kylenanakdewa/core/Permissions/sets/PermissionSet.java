package com.kylenanakdewa.core.permissions.sets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.permissions.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;

/**
 * Represents a set of permission nodes that may be applied to a
 * {@link org.bukkit.permissions.Permissible}.
 * <p>
 * Only one permission set may be applied to a Permissible at a time, although
 * sets may inherit from other sets, thus allowing a Permissible to have all
 * nodes from both sets.
 *
 * @author Kyle Nanakdewa
 */
public abstract class PermissionSet {

	/** Unique name of this permission set. */
	private final String id;

	/**
	 * Creates a permission set, with the specified collection of permissions.
	 *
	 * @param id a unique ID for the permission set
	 */
	protected PermissionSet(String id) {
		this.id = id.toLowerCase();
	}

	/**
	 * Gets the unique name of this set.
	 */
	public final String getName() {
		return id;
	}

	/**
	 * Gets a list of other sets that are inherited by this set. All of their
	 * permissions are also included in this set.
	 * <p>
	 * This is recursive: sets can inherit other sets.
	 * <p>
	 * May be null.
	 */
	public abstract List<PermissionSet> getInheritedSets();

	/**
	 * Gets the permissions included in this set, and their values.
	 * <p>
	 * Permissions that are true will be applied to the Permissible. Permissions
	 * that are false will be denied from the Permissible.
	 * <p>
	 * This map does not include inherited permissions.
	 * <p>
	 * May be null.
	 */
	public abstract Map<Permission, Boolean> getPermissions();

	/**
	 * Gets the permissions included in this set, and their values. This includes
	 * all inherited permissions.
	 * <p>
	 * Permissions that are true will be applied to the Permissible. Permissions
	 * that are false will be denied from the Permissible.
	 * <p>
	 * If there are no permissions, this will return an empty map.
	 */
	public final Map<Permission, Boolean> getTotalPermissions() {
		Map<Permission, Boolean> map = new HashMap<Permission, Boolean>();

		// Add the permissions from this set
		if (getPermissions() != null) {
			map.putAll(getPermissions());
		}

		// Add the permissions from each inherited set
		if (getInheritedSets() != null) {
			for (PermissionSet set : getInheritedSets()) {
				map.putAll(set.getPermissions());
			}
		}

		return map;
	}

	/**
	 * Gets the value of the specified permission, for this set.
	 * <p>
	 * If the specified permission does not exist in this set, returns the default
	 * value provided by the permission's plugin. This will typically be false, but
	 * some plugins may specify a default value of true.
	 */
	public final boolean hasPermission(Permission permission) {
		return getTotalPermissions().getOrDefault(permission, permission.getDefault().getValue(false));
	}

	/**
	 * Gets the value of the specified permission, for this set.
	 * <p>
	 * If the specified permission does not exist in this set, returns the default
	 * value provided by the permission's plugin. This will typically be false, but
	 * some plugins may specify a default value of true.
	 */
	public final boolean hasPermission(String permissionName) {
		// Attempt to get a permission registered on the server
		Permission permission = Bukkit.getPluginManager().getPermission(permissionName);

		// If permission not found, create an instance of it
		if (permission == null)
			permission = new Permission(permissionName);

		return hasPermission(permission);
	}

	/**
	 * Gets a Prompt with this set's information.
	 */
	public Prompt getInfoPrompt() {
		Prompt info = new Prompt();
		info.addQuestion(CommonColors.INFO + "--- Permission Set: " + CommonColors.MESSAGE + getName()
				+ CommonColors.INFO + " ---");

		// List inherited sets, with action to view info on those sets
		if (getInheritedSets() != null) {
			for (PermissionSet inherited : getInheritedSets()) {
				info.addAnswer("Inherits " + inherited.getName(), "command_permissions set " + inherited.getName());
			}
		}

		// List permission nodes in this set, coloured based on their value
		if (getPermissions() != null && !getPermissions().isEmpty()) {
			String permissionsList = "Permission nodes: ";
			for (Map.Entry<Permission, Boolean> node : getPermissions().entrySet()) {
				ChatColor color = node.getValue() ? ChatColor.GREEN : ChatColor.RED;
				permissionsList += color + node.getKey().getName() + CommonColors.MESSAGE + ", ";
			}
			info.addAnswer(permissionsList, "");

		} else {
			info.addAnswer(CommonColors.INFO + "This set contains no permission nodes.", "");
		}

		return info;
	}
}
