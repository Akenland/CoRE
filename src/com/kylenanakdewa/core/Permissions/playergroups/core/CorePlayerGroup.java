package com.kylenanakdewa.core.permissions.playergroups.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.PermissionsManager;
import com.kylenanakdewa.core.permissions.playergroups.PlayerGroup;
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

    /** The group provider that owns this group. */
    private final CorePlayerGroupProvider groupProvider;

    /** The gamemode permissions sets for this group. */
    private Map<GameMode, PermissionSet> gameModeSets;

    /** The other permission sets for this group. */
    private Set<PermissionSet> otherSets;

    /** The player UUIDs in this group. */
    private Set<UUID> playerUuids;

    /** Whether this group's data has already been loaded. */
    private boolean loaded;

    public CorePlayerGroup(String id, PermissionsManager permissionsManager, CorePlayerGroupProvider groupProvider) {
        super(id);
        this.permissionsManager = permissionsManager;
        this.groupProvider = groupProvider;
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
        FileConfiguration file = groupProvider.getPlayersFile();

        // Check if this group can be found in file
        if (file.contains("playergroups." + getName())) {

            // Get gamemode sets
            gameModeSets = new HashMap<GameMode, PermissionSet>();
            for (GameMode gameMode : GameMode.values()) {
                if (file.contains("playergroups." + getName() + "." + gameMode.name().toLowerCase())) {
                    String setName = file.getString("playergroups." + getName() + "." + gameMode.name().toLowerCase());
                    PermissionSet set = permissionsManager.getPermissionSet(setName);

                    if (set == null)
                        Utils.notifyAdminsError(
                                "CoRE Permissions was unable to find permission set " + setName + ", for player group "
                                        + getName() + ". Check the sets.yml and playergroups.yml file.");
                    else
                        gameModeSets.put(gameMode, set);
                }
            }

            // Get other sets
            if (file.contains("playergroups." + getName() + ".other-sets")) {
                otherSets = new HashSet<PermissionSet>();

                for (String setName : file.getStringList("playergroups." + getName() + ".other-sets")) {
                    PermissionSet set = permissionsManager.getPermissionSet(setName);

                    if (set == null)
                        Utils.notifyAdminsError("CoRE Permissions was unable to find permission set " + setName
                                + ", for player permissions group " + getName()
                                + ". Check the sets.yml and playergroups.yml file.");
                    else
                        otherSets.add(set);
                }
            }

            // Get other sets from inherited groups
            if (file.contains("playergroups." + getName() + ".inherit")) {
                for (String groupName : file.getStringList("playergroups." + getName() + ".inherit")) {
                    PlayerGroup group = groupProvider.getGroup(groupName);

                    if (group == null)
                        Utils.notifyAdminsError("CoRE Permissions was unable to find inherited player group "
                                + groupName + ", for player permissions group " + getName()
                                + ". Check the playergroups.yml file.");
                    else
                        otherSets.addAll(group.getAllSets());
                }
            }

            // Get the players in this group
            if (file.contains("playergroups." + getName() + ".players")) {
                playerUuids = new HashSet<UUID>();

                for (String uuidString : file.getStringList("playergroups." + getName() + ".players")) {
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