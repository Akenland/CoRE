package com.kylenanakdewa.core.permissions;

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
public class PermissionSet {

	/** Unique name of this permission set. */
	private final String id;

	/**
	 * A list of other sets that are inherited by this set. All of their permissions
	 * are also included in this set.
	 * <p>
	 * This is recursive: sets can inherit other sets.
	 */
	protected List<PermissionSet> inheritedSets;

	/**
	 * The permissions included in this set, and their value.
	 * <p>
	 * Permissions that are true will be applied to the Permissible. Permissions
	 * that are false will be denied from the Permissible.
	 * <p>
	 * This map does not include inherited permissions.
	 */
	protected Map<Permission, Boolean> permissions;

	/**
	 * Creates a permission set, with the specified collection of permissions.
	 *
	 * @param id            a unique ID for the permission set
	 * @param inheritedSets permission sets which will be inherited by this set (all
	 *                      permissions will be included, recursively), can be null
	 * @param permissions   permissions to assign as part of this set, and their
	 *                      value (true to assign the permission, false to deny the
	 *                      permission), can be null
	 */
	public PermissionSet(String id, List<PermissionSet> inheritedSets, Map<Permission, Boolean> permissions) {
		this.id = id.toLowerCase();
		this.inheritedSets = inheritedSets;
		this.permissions = permissions;

		// Check that this set does not contain itself
		if (inheritedSets != null) {
			for (PermissionSet set : inheritedSets) {
				if (set.getName().equals(id)) {
					Utils.notifyAdminsError("CoRE Permissions found a permission set that inherits itself (" + id
							+ "). This can cause unpredictable results, incorrect permissions, or server instability.");
				}
			}
		}
	}

	/**
	 * Gets the unique name of this set.
	 */
	public String getName() {
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
	public List<PermissionSet> getInheritedSets() {
		return inheritedSets;
	}

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
	public Map<Permission, Boolean> getPermissions() {
		return permissions;
	}

	/**
	 * Gets the permissions included in this set, and their values. This includes
	 * all inherited permissions.
	 * <p>
	 * Permissions that are true will be applied to the Permissible. Permissions
	 * that are false will be denied from the Permissible.
	 * <p>
	 * If there are no permissions, this will return an empty map.
	 */
	public Map<Permission, Boolean> getTotalPermissions() {
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
	public boolean hasPermission(Permission permission) {
		return getTotalPermissions().getOrDefault(permission, permission.getDefault().getValue(false));
	}

	/**
	 * Gets the value of the specified permission, for this set.
	 * <p>
	 * If the specified permission does not exist in this set, returns the default
	 * value provided by the permission's plugin. This will typically be false, but
	 * some plugins may specify a default value of true.
	 */
	public boolean hasPermission(String permissionName) {
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
