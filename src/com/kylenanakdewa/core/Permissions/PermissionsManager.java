package com.kylenanakdewa.core.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.kylenanakdewa.core.CoreModule;
import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.ConfigAccessor;
import com.kylenanakdewa.core.permissions.playergroups.core.CorePlayerGroupProvider;
import com.kylenanakdewa.core.permissions.commands.GameModeCommands;
import com.kylenanakdewa.core.permissions.commands.PermissionsCommands;
import com.kylenanakdewa.core.permissions.commands.SetSwitchCommands;
import com.kylenanakdewa.core.permissions.multicheck.AdminMultiCheck;
import com.kylenanakdewa.core.permissions.playergroups.PlayerGroup;
import com.kylenanakdewa.core.permissions.playergroups.PlayerGroupProvider;
import com.kylenanakdewa.core.permissions.sets.CorePermissionSet;
import com.kylenanakdewa.core.permissions.sets.PermissionSet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
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

    /** The player group providers that are part of this permissions system. */
    private LinkedHashSet<PlayerGroupProvider> playerGroupProviders;

    /** The players that are part of this permissions system. */
    private Map<UUID, PlayerPermissionsHolder> players;

    /** The Admin Multi-Check system. */
    private AdminMultiCheck adminMultiCheck;

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
        getPlugin().getCommand("permset").setExecutor(new SetSwitchCommands(this));
        getPlugin().getCommand("gamemode").setExecutor(new GameModeCommands(this));

        // Load permission sets
        loadPermissionSetsFile();

        // Load player groups
        registerPlayerGroupProvider(new CorePlayerGroupProvider(this));

        // Set up player permissions holders
        players = new HashMap<UUID, PlayerPermissionsHolder>();

        // Set up Admin Multi-Check
        adminMultiCheck = new AdminMultiCheck(this);
        PermsUtils.adminMultiCheck = adminMultiCheck;

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
     * Gets the file configuration for the sets.yml file.
     */
    public FileConfiguration getPermissionSetsFile() {
        return getPermissionsConfigFile("sets.yml");
    }

    /**
     * Gets a file configuration for a file in the permissions config folder.
     */
    public FileConfiguration getPermissionsConfigFile(String fileName) {
        return new ConfigAccessor("permissions\\" + fileName, getPlugin()).getConfig();
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

    /**
     * Registers a player group in this permissions system. Player groups are used
     * to link players to permission sets.
     */
    private void registerPlayerGroupProvider(PlayerGroupProvider playerGroupProvider) {
        playerGroupProviders.add(playerGroupProvider);
    }

    /**
     * Gets all groups that include the specified player, in order of priority.
     * <p>
     * If there are no groups that include this player, returns an empty set.
     */
    @Deprecated
    LinkedHashSet<PlayerGroup> getPlayerGroups(OfflinePlayer player, boolean ignoreEveryone) {
        LinkedHashSet<PlayerGroup> groups = new LinkedHashSet<PlayerGroup>();

        for (PlayerGroupProvider provider : playerGroupProviders) {
            PlayerGroup group = provider.getGroupForPlayer(player, ignoreEveryone);
            if (group != null) {
                groups.add(group);
            }
        }

        return groups;
    }

    /**
     * Gets the highest-priority group for the specified player.
     * <p>
     * More specifically, this will check all providers, in order, for a group that
     * explicitly contains this player (ignoring "everyone" groups). If a group is
     * not found, and ignoreEveryone is false, checks for the first provider with an
     * "everyone" group.
     * <p>
     * May return null if this player isn't listed in any groups, and there are no
     * "everyone" groups (or ignoreEveryone is true).
     */
    PlayerGroup getPlayerGroup(OfflinePlayer player, boolean ignoreEveryone) {
        // Check all providers in order, ignoring "everyone" groups
        for (PlayerGroupProvider provider : playerGroupProviders) {
            PlayerGroup group = provider.getGroupForPlayer(player, true);
            if (group != null) {
                return group;
            }
        }

        // Failing that, check for "everyone" groups
        if (!ignoreEveryone) {
            for (PlayerGroupProvider provider : playerGroupProviders) {
                PlayerGroup group = provider.getGroupForPlayer(player, false);
                if (group != null) {
                    return group;
                }
            }
        }

        return null;
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

    /**
     * Returns true if the specified CommandSender is a verified admin.
     */
    public boolean performAdminMultiCheck(CommandSender sender) {
        return adminMultiCheck.performCheck(sender);
    }

}