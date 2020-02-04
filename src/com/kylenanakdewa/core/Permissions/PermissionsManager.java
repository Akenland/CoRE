package com.kylenanakdewa.core.permissions;

import java.util.HashSet;
import java.util.Set;

import com.kylenanakdewa.core.CoreModule;
import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.common.ConfigAccessor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * The CoRE Permissions system.
 * <p>
 * Manages permission sets and all functionality for CoRE Permissions.
 *
 * @author Kyle Nanakdewa
 */
public final class PermissionsManager extends CoreModule {

    /** All permission sets that are part of this permissions system. */
    private Set<PermissionSet> permissionSets;

    /**
     * Creates a new instance of the CoRE Permissions system.
     *
     * @param plugin the CoRE instance that owns this permissions system
     */
    public PermissionsManager(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        // Set up commands
        getPlugin().getCommand("permissions").setExecutor(new PermissionsCommands(this));

        // Load sets
        loadPermissionSetsFile();
    }

    @Override
    public void onDisable() {

    }

    /**
     * Loads the permission sets that are defined in the permsets.yml file.
     */
    private void loadPermissionSetsFile() {
        permissionSets = new HashSet<PermissionSet>();

        for (String setName : getPermissionSetsFile().getConfigurationSection("sets").getKeys(false)) {
            permissionSets.add(new CorePermissionSet(setName, this));
        }
    }

    /**
     * Gets the file configuration for the permsets.yml file.
     */
    FileConfiguration getPermissionSetsFile() {
        return new ConfigAccessor("permsets.yml", getPlugin()).getConfig();
    }

    /**
     * Gets all permission sets that are part of this permissions system.
     */
    private Set<PermissionSet> getPermissionSets() {
        return permissionSets;
    }

    /**
     * Gets a specific permission set, by its ID.
     * <p>
     * If no set has that ID, returns null.
     */
    public PermissionSet getPermissionSet(String id) {
        for (PermissionSet set : getPermissionSets()) {
            if (set.getName().equals(id)) {
                return set;
            }
        }
        return null;
    }
}