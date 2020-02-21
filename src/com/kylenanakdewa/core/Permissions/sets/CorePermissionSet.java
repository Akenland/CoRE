package com.kylenanakdewa.core.permissions.sets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.PermissionsManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;

/**
 * Represents a set of permission nodes that may be applied to a
 * {@link org.bukkit.permissions.Permissible}.
 * <p>
 * The set is loaded from the CoRE Permissions sets.yml file.
 * <p>
 * Only one permission set may be applied to a Permissible at a time, although
 * sets may inherit from other sets, thus allowing a Permissible to have all
 * nodes from both sets.
 *
 * @author Kyle Nanakdewa
 */
public class CorePermissionSet extends PermissionSet {

    /** The CoRE Permissions system that owns this set. */
    private final PermissionsManager permissionsManager;

    /**
     * A list of other sets that are inherited by this set. All of their permissions
     * are also included in this set.
     * <p>
     * This is recursive: sets can inherit other sets.
     */
    private List<PermissionSet> inheritedSets;

    /**
     * The permissions included in this set, and their value.
     * <p>
     * Permissions that are true will be applied to the Permissible. Permissions
     * that are false will be denied from the Permissible.
     * <p>
     * This map does not include inherited permissions.
     */
    private Map<Permission, Boolean> permissions;

    /** Whether this set's data has already been loaded. */
    private boolean loaded;

    public CorePermissionSet(String id, PermissionsManager permissionsManager) {
        super(id);
        this.permissionsManager = permissionsManager;
    }

    @Override
    public List<PermissionSet> getInheritedSets() {
        load();
        return inheritedSets;
    }

    @Override
    public Map<Permission, Boolean> getPermissions() {
        load();
        return permissions;
    }

    /**
     * Loads this set's data from the CoRE Permissions sets.yml file.
     */
    private void load() {
        // If already loaded, don't load it again
        if (loaded) {
            return;
        }

        // Load the permsets.yml file
        FileConfiguration file = permissionsManager.getPermissionSetsFile();

        // Check if this set can be found in file
        if (file.contains("sets." + getName())) {

            // Get inherited sets
            if (file.contains("sets." + getName() + ".inherited-sets")) {
                inheritedSets = new ArrayList<PermissionSet>();

                for (String setName : file.getStringList("sets." + getName() + ".inherited-sets")) {
                    // Check that this set does not contain itself
                    if (setName.toLowerCase().equals(getName())) {
                        Utils.notifyAdminsError("CoRE Permissions found a permission set that inherits itself ("
                                + setName
                                + "). This can cause unpredictable results, incorrect permissions, or server instability.");
                    }

                    // Get the set from the permissions manager
                    PermissionSet set = permissionsManager.getPermissionSet(setName);

                    // If set does not exist, show an error
                    if (set == null) {
                        Utils.notifyAdminsError("CoRE Permissions was unable to find permission set " + setName
                                + ", inherited by permission set " + getName() + ". Check the sets.yml file.");
                    }

                    // Otherwise, inherit the set
                    else {
                        inheritedSets.add(set);
                    }
                }
            }

            // Get the permissions in this set
            if (file.contains("sets." + getName() + ".permissions")) {
                permissions = new HashMap<Permission, Boolean>();

                for (String node : file.getStringList("sets." + getName() + ".permissions")) {
                    // If node begins with !, it is false, otherwise true
                    boolean value = !node.startsWith("!");

                    permissions.put(new Permission(node), value);
                }
            }

            loaded = true;
        }

        else {
            // If set could not be loaded, show an error to admins and console
            Utils.notifyAdminsError(
                    "CoRE Permissions was unable to find permission set " + getName() + ". Check the sets.yml file.");
        }
    }

}