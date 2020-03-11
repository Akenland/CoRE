package com.kylenanakdewa.core.permissions.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.PermissionsManager;
import com.kylenanakdewa.core.permissions.multicheck.AdminMultiCheck;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 * Handles gamemode commands (including their tab-completions) that integrate
 * with the CoRE Permissions system.
 *
 * @author Kyle Nanakdewa
 */
public final class GameModeCommands implements TabExecutor {

    /** The CoRE Permissions system that owns this command executor. */
    private final PermissionsManager permissionsManager;

    public GameModeCommands(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameMode gameMode = null;

        // Aliases
        switch (label.toLowerCase()) {
            case "gms":
                gameMode = GameMode.SURVIVAL;
                break;
            case "gmc":
                gameMode = GameMode.CREATIVE;
                break;
            case "gma":
                gameMode = GameMode.ADVENTURE;
                break;
            case "gmsp":
                gameMode = GameMode.SPECTATOR;
                break;
            default:
                break;
        }

        // Check number of args
        if (gameMode == null && args.length == 0) {
            Utils.sendActionBar(sender, CommonColors.ERROR + "Specify a game mode to switch to");
            return false;
        }

        // Argument
        switch (args[0].toLowerCase()) {
            case "survival":
                gameMode = GameMode.SURVIVAL;
                break;
            case "creative":
                gameMode = GameMode.CREATIVE;
                break;
            case "adventure":
                gameMode = GameMode.ADVENTURE;
                break;
            case "spectator":
                gameMode = GameMode.SPECTATOR;
                break;
            default:
                return Error.INVALID_ARGS.displayActionBar(sender);
        }

        // Get target player
        Player player = null;
        if (args.length == 2 && sender.hasPermission("core.gamemode.others") && permissionsManager.performAdminMultiCheck(sender)) {
            player = Utils.getPlayer(args[1]);
        } else if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player == null) {
            return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
        }

        // Switch gamemode and permissions, as needed
        permissionsManager.getPlayer(player).setGameMode(gameMode);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // First arg, return list of gamemodes
        if (alias.equalsIgnoreCase("gamemode") && args.length == 1) {
            List<String> completions = new ArrayList<String>();
            for (GameMode gameMode : GameMode.values()) {
                completions.add(gameMode.toString().toLowerCase());
            }
            return completions;
        }

        // Second arg, return list of players
        if (alias.equalsIgnoreCase("gamemode") && args.length == 2 && sender.hasPermission("core.gamemode.others")) {
            return null;
        }

        // Otherwise, return no completions
        return Arrays.asList("");
    }

}