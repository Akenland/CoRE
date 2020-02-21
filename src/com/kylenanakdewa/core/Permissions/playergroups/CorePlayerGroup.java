package com.kylenanakdewa.core.permissions.playergroups;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.PermissionsManager;
import com.kylenanakdewa.core.permissions.sets.PermissionSet;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Represents a group of players that can be assigned permission sets.
 * <p>
 * The group is loaded from the CoRE Permissions playergroups.yml file.
 *
 * @author Kyle Nanakdewa
 */
public class CorePlayerGroup extends PlayerGroup {

    /** The CoRE Permissions system that owns this group. */
    private final PermissionsManager permissionsManager;

    /** The gamemode permissions sets for this group. */
    private Map<GameMode, PermissionSet> gameModeSets;

    /** The other permission sets for this group. */
    private Set<PermissionSet> otherSets;

    /** The player UUIDs in this group. */
    private Set<UUID> playerUuids;

    /** Whether this group's data has already been loaded. */
    private boolean loaded;

    public CorePlayerGroup(String id, PermissionsManager permissionsManager) {
        super(id);
        this.permissionsManager = permissionsManager;
    }

    @Override
    public PermissionSet getGameModeSet(GameMode gameMode) {
        load();

        if (gameModeSets == null)
            return null;
        else
            return gameModeSets.get(gameMode);
    }

    @Override
    public Set<PermissionSet> getOtherSets() {
        load();
        return otherSets;
    }

    @Override
    public Set<UUID> getPlayerUuids() {
        load();
        return playerUuids;
    }

    /**
     * Loads this group's data from the CoRE Permissions playergroups.yml file.
     */
    private void load() {
        // If already loaded, don't load it again
        if (loaded)
            return;

        // Load the playergroups.yml file
        FileConfiguration file = permissionsManager.getPlayersFile();

        // Check if this group can be found in file
        if (file.contains("players." + getName())) {

            // Get gamemode sets
            gameModeSets = new HashMap<GameMode, PermissionSet>();
            for (GameMode gameMode : GameMode.values()) {
                if (file.contains("players." + getName() + "." + gameMode.name().toLowerCase())) {
                    String setName = file.getString("players." + getName() + "." + gameMode.name().toLowerCase());
                    PermissionSet set = permissionsManager.getPermissionSet(setName);

                    if (set == null)
                        Utils.notifyAdminsError("CoRE Permissions was unable to find permission set " + setName
                                + ", for player permissions group " + getName()
                                + ". Check the sets.yml and playergroups.yml file.");
                    else
                        gameModeSets.put(gameMode, set);
                }
            }

            // Get other sets
            if (file.contains("players." + getName() + ".other-sets")) {
                otherSets = new HashSet<PermissionSet>();

                for (String setName : file.getStringList("players." + getName() + ".other-sets")) {
                    PermissionSet set = permissionsManager.getPermissionSet(setName);

                    if (set == null)
                        Utils.notifyAdminsError("CoRE Permissions was unable to find permission set " + setName
                                + ", for player permissions group " + getName()
                                + ". Check the sets.yml and playergroups.yml file.");
                    else
                        otherSets.add(set);
                }
            }

            // Get the permissions in this set
            if (file.contains("players." + getName() + ".players")) {
                playerUuids = new HashSet<UUID>();

                for (String uuidString : file.getStringList("sets." + getName() + ".players")) {
                    UUID uuid = UUID.fromString(uuidString);
                    playerUuids.add(uuid);
                }
            }

            loaded = true;
        } else {
            // If set could not be loaded, show an error to admins and console
            Utils.notifyAdminsError("CoRE Permissions was unable to find player permissions group " + getName()
                    + ". Check the playergroups.yml file.");
        }
    }

}