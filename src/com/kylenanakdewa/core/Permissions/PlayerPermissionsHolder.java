package com.kylenanakdewa.core.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;

/**
 * Holds permission data for a player.
 * <p>
 * Serves as a bridge between CoRE Permissions and the Bukkit Permissible.
 *
 * @author Kyle Nanakdewa
 */
final class PlayerPermissionsHolder {

    /** The player character that this holder is for. */
    private final PlayerCharacter player;

    /** The CoRE Permissions system that owns this holder. */
    private final PermissionsManager permissionsManager;

    /** The CoRE plugin that owns the permissions system. */
    private final CorePlugin plugin;

    /** The current active permission set for this player. */
    private PermissionSet currentSet;

    /** The current PermissionAttachment for this player. */
    private PermissionAttachment attachment;

    PlayerPermissionsHolder(PlayerCharacter player, PermissionsManager permissionsManager, CorePlugin plugin) {
        this.player = player;
        this.permissionsManager = permissionsManager;
        this.plugin = plugin;
    }

    /**
     * Gets the player character that this holder is for.
     */
    PlayerCharacter getPlayer() {
        return player;
    }

    /**
     * Gets the current active permission set.
     */
    PermissionSet getCurrentSet() {
        return currentSet;
    }

    /**
     * Sets the current active permission set.
     *
     * @param set    the permission set to switch to
     * @param silent whether the player should be notified
     */
    void setCurrentSet(PermissionSet set, boolean silent) {
        // Check that player can access this set
        if (!getAllSets().keySet().contains(set)) {
            Utils.notifyAdminsError(
                    player.getName() + CommonColors.ERROR + " was denied access to " + set.getName() + " permissions.");
            if (player.isOnline())
                Utils.sendActionBar(player.getPlayer().getPlayer(),
                        CommonColors.ERROR + "You don't have access to " + set.getName() + " permissions!");
            return;
        }

        // Remove existing permissions
        revokePermissions();

        if (player.isOnline()) {
            if (!silent) {
                // Notify admins and player
                Utils.notifyAdmins(
                        CommonColors.INFO + "Applied " + set.getName() + " permissions to " + player.getName());
                Utils.sendActionBar(player.getPlayer().getPlayer(), "Switched to " + set.getName() + " permissions");
            } else {
                // Log to console only
                Bukkit.getLogger()
                        .info(CommonColors.INFO + "Applied " + set.getName() + " permissions to " + player.getName());
            }

            currentSet = set;

            attachment = player.getPlayer().getPlayer().addAttachment(plugin);

            // Apply the permissions
            for (Entry<Permission, Boolean> node : set.getTotalPermissions().entrySet()) {
                attachment.setPermission(node.getKey(), node.getValue());
            }
        }

        else {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR + " could not be switched to " + set.getName()
                    + " permissions, they are offline.");
        }

    }

    /**
     * Sets this player's permission set to the one appropriate for their current
     * gamemode.
     */
    void setCurrentSetAutomatic() {
        // Make sure player is online
        if (player.isOnline()) {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR
                    + " could not have permissions switched automatically, they are offline.");
            return;
        }

        GameMode gameMode = player.getPlayer().getPlayer().getGameMode();

        PermissionSet set = getGameModeSets(gameMode).get(getBestGroup());

        setCurrentSet(set, true);
    }

    /**
     * Sets this player's gamemode and permissions, if they have access to it.
     */
    void setGameMode(GameMode gameMode) {
        PermissionSet set = getGameModeSets(gameMode).get(getBestGroup());
        String permission = "core.gamemode." + gameMode.toString().toLowerCase();

        // Make sure set grants them access to this gamemode
        if (set.hasPermission(permission) && player.isOnline()) {
            // Notify admins and player
            Utils.notifyAdmins(player.getName() + CommonColors.INFO + " switched to "
                    + gameMode.toString().toLowerCase() + " mode (" + set.getName() + " permissions)");
            Utils.sendActionBar(player.getPlayer().getPlayer(), CommonColors.INFO + "Switched to "
                    + gameMode.toString().toLowerCase() + " mode (" + set.getName() + " permissions)");

            player.getPlayer().getPlayer().setGameMode(gameMode);
        }

        else {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR + " was denied access to "
                    + gameMode.toString().toLowerCase() + "mode and " + set.getName() + " permissions.");
            if (player.isOnline())
                Utils.sendActionBar(player.getPlayer().getPlayer(), CommonColors.ERROR + "You don't have access to "
                        + gameMode.toString().toLowerCase() + "mode or " + set.getName() + " permissions!");
        }
    }

    /**
     * Revokes all permissions.
     */
    void revokePermissions() {
        attachment.remove();
        attachment = null;
        currentSet = null;

        // De-op the player
        player.getPlayer().setOp(false);
    }

    /**
     * Gets all groups that include this player.
     * <p>
     * If there are no groups that include this player, returns an empty set.
     */
    private Set<PlayerPermissionsGroup> getPermissionsGroups() {
        return permissionsManager.getPlayerPermissionsGroup(player.getPlayer());
    }

    /**
     * Attempts to determine the main group that this player is part of.
     */
    private PlayerPermissionsGroup getBestGroup() {
        Set<PlayerPermissionsGroup> groups = new HashSet<PlayerPermissionsGroup>();
        groups.addAll(getPermissionsGroups());
        groups.removeIf(group -> group.getName().equals("everyone"));

        // if(getPermissionsGroups().size()==1){
        return groups.iterator().next();
        // }
    }

    /**
     * Gets all permission sets that this player has access to.
     * <p>
     * If this player has access to no permissions sets, this will return an empty
     * map.
     */
    private Map<PermissionSet, PlayerPermissionsGroup> getAllSets() {
        Map<PermissionSet, PlayerPermissionsGroup> map = new HashMap<PermissionSet, PlayerPermissionsGroup>();

        for (PlayerPermissionsGroup group : getPermissionsGroups()) {
            for (PermissionSet set : group.getAllSets()) {
                map.put(set, group);
            }
        }

        return map;
    }

    /**
     * Gets all permission sets for the specified gamemode.
     * <p>
     * If this player has access to no permissions sets for this gamemode, this will
     * return an empty map.
     */
    private Map<PlayerPermissionsGroup, PermissionSet> getGameModeSets(GameMode gameMode) {
        Map<PlayerPermissionsGroup, PermissionSet> map = new HashMap<PlayerPermissionsGroup, PermissionSet>();

        for (PlayerPermissionsGroup group : getPermissionsGroups()) {
            map.put(group, group.getGameModeSet(gameMode));
        }

        return map;
    }

    /**
     * Gets an information prompt about this permission holder.
     */
    Prompt getPermissionsInfoPrompt() {
        Prompt prompt = new Prompt();

        prompt.addQuestion(CommonColors.INFO + "--- " + CommonColors.MESSAGE + "Player Permissions: " + player.getName()
                + CommonColors.INFO + " ---");

        // Username and UUID
        prompt.addAnswer("Username: " + player.getUsername(), "command_player " + player.getUsername());
        prompt.addAnswer("UUID: " + player.getUniqueId(), "url_https://mcuuid.net/?q=" + player.getUniqueId());

        // Current set
        prompt.addAnswer("Current set: " + getCurrentSet().getName(),
                "command_permissions set " + getCurrentSet().getName());

        // Group sets
        for (PlayerPermissionsGroup group : getPermissionsGroups()) {
            prompt.addAnswer(CommonColors.INFO + "-- " + CommonColors.MESSAGE + group.getName() + " group"
                    + CommonColors.INFO + " --", "");

            if (group.gameModeSets != null) {
                for (Entry<GameMode, PermissionSet> gameModeSet : group.gameModeSets.entrySet()) {
                    prompt.addAnswer(gameModeSet.getKey() + " set: " + gameModeSet.getValue().getName(),
                            "command_permissions set " + gameModeSet.getValue().getName());
                }
            }

            if (group.getOtherSets() != null) {
                for (PermissionSet set : group.getOtherSets()) {
                    prompt.addAnswer("Other set: " + set.getName(), "command_permissions set " + set.getName());
                }
            }
        }

        return prompt;
    }

}