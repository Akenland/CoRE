package com.kylenanakdewa.core.permissions.playergroups;

import com.kylenanakdewa.core.permissions.PermissionsManager;

import org.bukkit.OfflinePlayer;

/**
 * Manages player groups for the CoRE Permissions system.
 * <p>
 * Players and permission sets are both assigned to groups, in order to link
 * them together.
 *
 * @author Kyle Nanakdewa
 */
public abstract class PlayerGroupProvider {

    /** The CoRE Permissions system that owns this provider. */
    protected final PermissionsManager permissionsManager;

    public PlayerGroupProvider(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    /**
     * Gets the group that contains the specified player.
     * <p>
     * Only one group should be returned, even if a provider theoretically allows a
     * player to be in multiple groups. In general, players should not be included
     * in multiple groups, and if detected, a warning should be displayed to admins
     * and the console.
     * <p>
     * If the player is not explicitly listed, but an "everyone" group exists, that
     * group will be returned, unless ignoreEveryone is true.
     * <p>
     * May return null if this player isn't listed in this group, and there is no
     * "everyone" group (or ignoreEveryone is true).
     */
    public PlayerGroup getGroupForPlayer(OfflinePlayer player, boolean ignoreEveryone) {
        return null;
    }

}