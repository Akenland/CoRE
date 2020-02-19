package com.kylenanakdewa.core.permissions;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.GameMode;

/**
 * Represents a group of players that can be assigned permission sets.
 *
 * @author Kyle Nanakdewa
 */
public class PlayerPermissionsGroup {

    /** The unique name of this group. */
    private final String id;

    /** The gamemode permissions sets for this group. */
    protected Map<GameMode, PermissionSet> gameModeSets;

    /** The other permission sets for this group. */
    protected Set<PermissionSet> otherSets;

    /** The player UUIDs in this group. */
    protected Set<UUID> playerUuids;

    PlayerPermissionsGroup(String id) {
        this.id = id.toLowerCase();
    }

    /**
     * Gets the unique name of this group.
     */
    public String getName() {
        return id;
    }

    /**
     * Gets the permission set for the specified gamemode.
     * <p>
     * May be null, if the gamemode is not included in this group.
     */
    public PermissionSet getGameModeSet(GameMode gameMode) {
        if (gameModeSets == null)
            return null;
        else
            return gameModeSets.get(gameMode);
    }

    /**
     * Gets the other permission sets in this group.
     * <p>
     * Does not include gamemode permission sets.
     * <p>
     * May be null.
     */
    public Set<PermissionSet> getOtherSets() {
        return otherSets;
    }

    /**
     * Gets all permission sets in this group.
     * <p>
     * This includes gamemode sets, and other sets.
     * <p>
     * If there are no permissions sets in this group, this will return an empty
     * set.
     */
	public Set<PermissionSet> getAllSets() {
		Set<PermissionSet> set = new HashSet<PermissionSet>();

		// Add the gamemode sets
		if (gameModeSets) != null) {
			set.addAll(gameModeSets.values());
		}

		// Add the other sets
		if (getOtherSets() != null) {
			set.addAll(getOtherSets());
		}

		return set;
    }

    /**
     * Gets the UUIDs of all players that are part of this group.
     * <p>
     * May be null, if no players are defined.
     */
    public Set<UUID> getPlayerUuids() {
        return playerUuids;
    }

}