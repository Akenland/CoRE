package com.kylenanakdewa.core.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.kylenanakdewa.core.CoreModule;
import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.ConfigAccessor;

import org.bukkit.OfflinePlayer;
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

    /** The player permission groups that are part of this permissions system. */
    private Set<PlayerPermissionsGroup> playerPermissionsGroups;

    /** The players that are part of this permissions system. */
    private Map<UUID, PlayerPermissionsHolder> players;

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

        // Load sets and groups
        loadPermissionSetsFile();
        loadPlayersFile();

        players = new HashMap<UUID, PlayerPermissionsHolder>();

        // Register listener
        getPlugin().getServer().getPluginManager().registerEvents(new PermissionsListener(this, getPlugin()),
                getPlugin());
    }

    @Override
    public void onDisable() {

    }

    /**
     * Loads the permission sets that are defined in the sets.yml file.
     */
    private void loadPermissionSetsFile() {
        permissionSets = new HashSet<PermissionSet>();

        for (String setName : getPermissionSetsFile().getConfigurationSection("sets").getKeys(false)) {
            permissionSets.add(new CorePermissionSet(setName, this));
        }
    }

    /**
     * Loads the player permissions groups that are defined in the players.yml file.
     */
    private void loadPlayersFile() {
        playerPermissionsGroups = new HashSet<PlayerPermissionsGroup>();

        for (String groupName : getPlayersFile().getConfigurationSection("players").getKeys(false)) {
            playerPermissionsGroups.add(new CorePlayerPermissionsGroup(groupName, this));
        }
    }

    /**
     * Gets the file configuration for the sets.yml file.
     */
    FileConfiguration getPermissionSetsFile() {
        return new ConfigAccessor("permissions\\sets.yml", getPlugin()).getConfig();
    }

    /**
     * Gets the file configuration for the players.yml file.
     */
    FileConfiguration getPlayersFile() {
        return new ConfigAccessor("permissions\\players.yml", getPlugin()).getConfig();
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
    PermissionSet getPermissionSet(String id) {
        for (PermissionSet set : getPermissionSets()) {
            if (set.getName().equals(id)) {
                return set;
            }
        }
        return null;
    }

    /**
     * Gets all groups that include the specified player.
     * <p>
     * If there are no groups that include this player, returns an empty set.
     */
    Set<PlayerPermissionsGroup> getPlayerPermissionsGroup(OfflinePlayer player) {
        Set<PlayerPermissionsGroup> set = new HashSet<PlayerPermissionsGroup>();

        for (PlayerPermissionsGroup group : playerPermissionsGroups) {
            if (group.getPlayerUuids() != null && group.getPlayerUuids().contains(player.getUniqueId())) {
                set.add(group);
            }
        }

        return set;
    }

    /**
     * Gets all player UUIDs that have explicit permissions set.
     */
    private Set<UUID> getPlayersWithPermissions() {
        Set<UUID> set = new HashSet<UUID>();

        for (PlayerPermissionsGroup group : playerPermissionsGroups) {
            if (group.getPlayerUuids() != null)
                set.addAll(group.getPlayerUuids());
        }

        return set;
    }

    /**
     * Gets the specified player in this permissions system.
     */
    public PlayerPermissionsHolder getPlayer(OfflinePlayer player) {
        // Get the holder, if already stored
        PlayerPermissionsHolder holder = players.get(player.getUniqueId());

        // If not stored, create it
        if (holder == null) {
            holder = new PlayerPermissionsHolder(PlayerCharacter.getCharacter(player), this, getPlugin());
            players.put(player.getUniqueId(), holder);
        }

        return holder;
    }

    /**
     * Cleans out offline players in this permissions system.
     */
    void cleanPlayers() {
        // Remove players if they are offline
        players.values().removeIf(player -> !player.getPlayer().isOnline());
    }
}