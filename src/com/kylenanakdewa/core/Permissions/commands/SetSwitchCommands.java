package com.kylenanakdewa.core.permissions.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.PermissionsManager;
import com.kylenanakdewa.core.permissions.sets.PermissionSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 * Handles set-switching commands (including their tab-completions) for the CoRE
 * Permissions system.
 *
 * @author Kyle Nanakdewa
 */
public final class SetSwitchCommands implements TabExecutor {

    /** The CoRE Permissions system that owns this command executor. */
    private final PermissionsManager permissionsManager;

    public SetSwitchCommands(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Make sure sender has base permission
        if (!sender.hasPermission("core.permissions.setswitch")) {
            return Error.NO_PERMISSION.displayChat(sender);
        }

        // Switching own set
        if (args.length == 1 && sender instanceof Player) {
            // Get the set
            PermissionSet set = permissionsManager.getPermissionSet(args[0]);
            if (set == null) {
                Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR
                        + " tried to switch to invalid permission set: " + args[0]);
                return Error.INVALID_ARGS.displayActionBar(sender);
            }

            // Switch the set
            permissionsManager.getPlayer((Player) sender).setCurrentSet(set);

            return true;
        }

        // Switching another player's set
        if (args.length == 2 && sender.hasPermission("core.permissions.setswitch.others")
                && permissionsManager.performAdminMultiCheck(sender)) {
            // Get the set
            PermissionSet set = permissionsManager.getPermissionSet(args[0]);
            if (set == null) {
                Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR
                        + " tried to switch to invalid permission set: " + args[0]);
                return Error.INVALID_ARGS.displayActionBar(sender);
            }

            // Get the player
            Player player = Utils.getPlayer(args[1]);
            if (player == null) {
                return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
            }

            // Switch the set
            permissionsManager.getPlayer(player).setCurrentSet(set);

            return true;
        }

        return Error.INVALID_ARGS.displayActionBar(sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Set names
        if (args.length == 1 && sender.hasPermission("core.permissions.setswitch.tab-complete")) {
            List<String> completions = new ArrayList<String>();
            for (PermissionSet set : permissionsManager.getPlayer((Player) sender).getAllSets()) {
                completions.add(set.getName());
            }
            return completions;
        }

        // Player names
        if (args.length == 2 && sender.hasPermission("core.permissions.setswitch.others")) {
            return null;
        }

        return Arrays.asList("");
    }
}