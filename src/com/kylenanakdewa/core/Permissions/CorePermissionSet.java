package com.kylenanakdewa.core.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kylenanakdewa.core.common.Utils;

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

    /** Whether this set's data has already been loaded. */
    private boolean loaded;

    CorePermissionSet(String id, PermissionsManager permissionsManager) {
        super(id, null, null);
        this.permissionsManager = permissionsManager;
    }

    @Override
    public List<PermissionSet> getInheritedSets() {
        load();
        return super.getInheritedSets();
    }

    @Override
    public Map<Permission, Boolean> getPermissions() {
        load();
        return super.getPermissions();
    }

    /**
     * Loads this set's data from the CoRE Permissions sets.yml file.
     */
    private void load() {
        // If already loaded, don't load it again
        if (loaded)
            return;

        // Load the permsets.yml file
        FileConfiguration file = permissionsManager.getPermissionSetsFile();

        // Check if this set can be found in file
        if (file.contains("sets." + getName())) {

            // Get inherited sets
            if (file.contains("sets." + getName() + ".inherited-sets")) {
                inheritedSets = new ArrayList<PermissionSet>();

                for (String setName : file.getStringList("sets." + getName() + ".inherited-sets")) {
                    PermissionSet set = permissionsManager.getPermissionSet(setName);

                    if (set == null)
                        Utils.notifyAdminsError("CoRE Permissions was unable to find permission set " + setName
                                + ", inherited by permission set " + getName() + ". Check the sets.yml file.");
                    else
                        inheritedSets.add(set);
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
        } else {
            // If set could not be loaded, show an error to admins and console
            Utils.notifyAdminsError(
                    "CoRE Permissions was unable to find permission set " + getName() + ". Check the sets.yml file.");
        }
    }

}