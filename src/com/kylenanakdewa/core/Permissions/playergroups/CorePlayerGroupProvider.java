package com.kylenanakdewa.core.permissions.playergroups;

import java.util.LinkedHashSet;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.PermissionsManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages player groups for the CoRE Permissions system.
 * <p>
 * Players and permission sets are both assigned to groups, in order to link
 * them together.
 * <p>
 * Groups are loaded from the CoRE Permissions playergroups.yml file.
 *
 * @author Kyle Nanakdewa
 */
public class CorePlayerGroupProvider {

    /** The CoRE Permissions system that owns this provider. */
    private final PermissionsManager permissionsManager;

    /** The player groups that are from this provider. */
    private LinkedHashSet<PlayerGroup> playerGroups;

    /** Whether this provider's set of groups has already been loaded. */
    private boolean loaded;

    public CorePlayerGroupProvider(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    /**
     * Gets the file configuration for the playergroups.yml file.
     */
    FileConfiguration getPlayersFile() {
        return permissionsManager.getPermissionsConfigFile("playergroups.yml");
    }

    /**
     * Loads the player permissions groups that are defined in the playergroups.yml
     * file.
     */
    public LinkedHashSet<PlayerGroup> getPlayerGroups() {
        if (loaded) {
            return playerGroups;
        }

        playerGroups = new LinkedHashSet<PlayerGroup>();

        for (String groupName : getPlayersFile().getConfigurationSection("players").getKeys(false)) {
            playerGroups.add(new CorePlayerGroup(groupName, permissionsManager));
        }

        loaded = true;

        return playerGroups;
    }

    /**
     * Gets a group by its unique name.
     * <p>
     * May be null, if group does not exist in this provider.
     */
    private PlayerGroup getGroup(String groupName) {
        for (PlayerGroup group : getPlayerGroups()) {
            if (group.getName().equals(groupName)) {
                return group;
            }
        }

        return null;
    }

    /**
     * Gets the group that contains the specified player.
     * <p>
     * If the player is part of multiple groups, this will only return the first
     * listed group that they are part of. If the player is not explicitly listed,
     * but an "everyone" group exists, that group will be returned.
     * <p>
     * In general, players should not be included in multiple groups, and if
     * detected, a warning will be displayed to admins and the console.
     * <p>
     * May be null if this player isn't listed in the playergroups.yml file, and
     * there is no "everyone" group.
     */
    public PlayerGroup getGroupForPlayer(OfflinePlayer player) {
        PlayerGroup groupForPlayer = null;

        // Look for a group that contains this player
        for (PlayerGroup group : playerGroups) {
            if (group.getPlayerUuids() != null && group.getPlayerUuids().contains(player.getUniqueId())) {
                if (groupForPlayer == null) {
                    groupForPlayer = group;
                }

                // Warn admins if a player is part of multiple groups, as this
                else {
                    Utils.notifyAdminsError(player.getName() + CommonColors.INFO + " (" + player.getUniqueId() + ") "
                            + CommonColors.ERROR + "is incorrectly listed in player permissions group "
                            + group.getName() + ", when they should only be listed in group " + groupForPlayer.getName()
                            + ". Check the playergroups.yml file.");
                }
            }
        }

        // If none, look for the "everyone" group
        if (groupForPlayer == null) {
            groupForPlayer = getGroup("everyone");
        }

        return groupForPlayer;
    }

}