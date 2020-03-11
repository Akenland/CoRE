package com.kylenanakdewa.core.permissions;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.core.permissions.playergroups.PlayerGroup;
import com.kylenanakdewa.core.permissions.sets.PermissionSet;

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
public final class PlayerPermissionsHolder {

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
    private void setCurrentSet(PermissionSet set, boolean silent) {
        // Check that player can access this set
        if (!getAllSets().contains(set)) {
            Utils.notifyAdminsError(
                    player.getName() + CommonColors.ERROR + " was denied access to " + set.getName() + " permissions.");
            if (player.isOnline())
                Utils.sendActionBar(player.getPlayer().getPlayer(),
                        CommonColors.ERROR + "You don't have access to " + set.getName() + " permissions!");
            return;
        }

        // Remove existing permissions
        revokePermissions(true);

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
     * Sets the current active permission set. The player and online admins will be
     * notified.
     */
    public void setCurrentSet(PermissionSet set) {
        setCurrentSet(set, false);
    }

    /**
     * Sets this player's permission set to the one appropriate for their current
     * gamemode.
     * <p>
     * If the player is offline, this may throw an exception. Make sure the player
     * is online before calling.
     */
    void setCurrentSetAutomatic() {
        // Make sure player is online
        /*
         * if (player.isOnline()) { Utils.notifyAdminsError(player.getName() +
         * CommonColors.ERROR +
         * " could not have permissions switched automatically, they are offline.");
         * return; }
         */

        GameMode gameMode = player.getPlayer().getPlayer().getGameMode();

        PermissionSet set = getGameModeSet(gameMode);
        // Warn if set is not found
        if (set == null) {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR + " is in "
                    + gameMode.toString().toLowerCase() + " mode, but does not have a permission set.");
            return;
        }

        setCurrentSet(set, true);
    }

    /**
     * Sets this player's gamemode and permissions, if they have access to it.
     */
    public void setGameMode(GameMode gameMode) {
        PermissionSet set = getGameModeSet(gameMode);
        // Warn if set not found
        if (set == null) {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR + " couldn't be switched to "
                    + gameMode.toString().toLowerCase() + "mode, they don't have a permission set.");
            // Notify the player
            if (player.isOnline()) {
                Utils.sendActionBar(player.getPlayer().getPlayer(),
                        CommonColors.ERROR + "You don't have access to " + gameMode.toString().toLowerCase() + "mode!");
            }
            return;
        }

        String permission = "core.gamemode." + gameMode.toString().toLowerCase();

        // Make sure set grants them access to this gamemode
        if (set.hasPermission(permission) && player.isOnline()) {
            // Notify admins and player
            Utils.notifyAdmins(player.getName() + CommonColors.INFO + " switched to "
                    + gameMode.toString().toLowerCase() + " mode (" + set.getName() + " permissions)");
            Utils.sendActionBar(player.getPlayer().getPlayer(), CommonColors.INFO + "Switched to "
                    + gameMode.toString().toLowerCase() + " mode (" + set.getName() + " permissions)");

            // Permissions will switch automatically, when their gamemode changes
            player.getPlayer().getPlayer().setGameMode(gameMode);
        }

        else {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR + " was denied access to "
                    + gameMode.toString().toLowerCase() + "mode and " + set.getName() + " permissions.");
            if (player.isOnline())
                Utils.sendActionBar(player.getPlayer().getPlayer(),
                        CommonColors.ERROR + "You don't have access to " + gameMode.toString().toLowerCase() + "mode!");
        }
    }

    /**
     * Revokes all permissions. If silent is false, online admins will be notified.
     */
    void revokePermissions(boolean silent) {
        if (attachment != null)
            attachment.remove();
        attachment = null;
        currentSet = null;

        // De-op the player
        player.getPlayer().setOp(false);

        if (!silent) {
            // Notify admins and player
            Utils.notifyAdmins(CommonColors.ERROR + "Revoked permissions from " + player.getName());
        }
    }

    /**
     * Revokes all permissions. Online admins will be notified.
     */
    public void revokePermissions() {
        revokePermissions(false);
    }

    /**
     * Gets this player's group.
     * <p>
     * More specifically, this will check all providers, in order, for a group that
     * explicitly contains this player (ignoring "everyone" groups). If a group is
     * not found, and ignoreEveryone is false, checks for the first provider with an
     * "everyone" group.
     * <p>
     * May return null if this player isn't listed in any groups, and there are no
     * "everyone" groups (or ignoreEveryone is true).
     */
    private PlayerGroup getGroup(boolean ignoreEveryone) {
        return permissionsManager.getPlayerGroup(player.getPlayer(), ignoreEveryone);
    }

    /**
     * Gets all permission sets that this player has access to.
     * <p>
     * This includes gamemode sets, and other sets.
     * <p>
     * If this player doesn't have access to any permissions sets, this will return
     * an empty set.
     */
    public Set<PermissionSet> getAllSets() {
        PlayerGroup group = getGroup(false);
        if (group != null)
            return group.getAllSets();
        else
            return new HashSet<PermissionSet>();
    }

    /**
     * Gets the permission set for the specified gamemode.
     * <p>
     * Returns null if this player doesn't have a permission set for this gamemode.
     */
    private PermissionSet getGameModeSet(GameMode gameMode) {
        PlayerGroup group = getGroup(false);
        if (group != null) {
            return group.getGameModeSet(gameMode);
        } else {
            return null;
        }
    }

    /**
     * Gets an information prompt about this permission holder.
     */
    public Prompt getPermissionsInfoPrompt() {
        Prompt prompt = new Prompt();

        prompt.addQuestion(CommonColors.INFO + "--- " + CommonColors.MESSAGE + "Player Permissions: " + player.getName()
                + CommonColors.INFO + " ---");

        // Username and UUID
        prompt.addAnswer("Username: " + player.getUsername(), "command_player " + player.getUsername());
        prompt.addAnswer("UUID: " + player.getUniqueId(), "url_https://mcuuid.net/?q=" + player.getUniqueId());

        // Current set
        String setName = getCurrentSet() != null ? getCurrentSet().getName() : "None";
        prompt.addAnswer("Current set: " + setName, "command_permissions set " + setName);

        // Group name
        if (getGroup(false) != null) {
            prompt.addAnswer(CommonColors.INFO + "-- " + CommonColors.MESSAGE + getGroup(false).getName() + " group"
                    + CommonColors.INFO + " --", "");

            // Gamemode sets
            for (GameMode gameMode : GameMode.values()) {
                PermissionSet set = getGameModeSet(gameMode);
                if (set != null) {
                    prompt.addAnswer(gameMode + " set: " + set.getName(), "command_permissions set " + set.getName());
                }
            }

            // Other sets
            if (getGroup(false).getOtherSets() != null) {
                for (PermissionSet set : getGroup(false).getOtherSets()) {
                    prompt.addAnswer("Other set: " + set.getName(), "command_permissions set " + set.getName());
                }
            }
        } else {
            prompt.addAnswer("This player isn't in a group, and therefore has no permissions.", "");
        }

        return prompt;
    }

}