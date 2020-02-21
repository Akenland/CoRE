package com.kylenanakdewa.core.permissions.playergroups;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.kylenanakdewa.core.permissions.sets.PermissionSet;

import org.bukkit.GameMode;

/**
 * Represents a group of players that can be assigned permission sets.
 *
 * @author Kyle Nanakdewa
 */
public abstract class PlayerGroup {

    /** The unique name of this group. */
    private final String id;

    protected PlayerGroup(String id) {
        this.id = id.toLowerCase();
    }

    /**
     * Gets the unique name of this group.
     */
    public final String getName() {
        return id;
    }

    /**
     * Gets the permission set for the specified gamemode.
     * <p>
     * May be null, if the gamemode is not included in this group.
     */
    public abstract PermissionSet getGameModeSet(GameMode gameMode);

    /**
     * Gets the other permission sets in this group.
     * <p>
     * Does not include gamemode permission sets.
     * <p>
     * May be null.
     */
    public abstract Set<PermissionSet> getOtherSets();

    /**
     * Gets all permission sets in this group.
     * <p>
     * This includes gamemode sets, and other sets.
     * <p>
     * If there are no permissions sets in this group, this will return an empty
     * set.
     */
    public final Set<PermissionSet> getAllSets() {
        Set<PermissionSet> allSets = new HashSet<PermissionSet>();

        // Add the gamemode sets
        for (GameMode gameMode : GameMode.values()) {
            PermissionSet set = getGameModeSet(gameMode);
            if (set != null) {
                allSets.add(set);
            }
        }

        // Add the other sets
        Set<PermissionSet> otherSets = getOtherSets();
        if (otherSets != null) {
            allSets.addAll(otherSets);
        }

        return allSets;
    }

    /**
     * Gets the UUIDs of all players that are part of this group.
     * <p>
     * May be null, if no players are defined.
     */
    public abstract Set<UUID> getPlayerUuids();

}