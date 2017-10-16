package com.KyleNecrowolf.RealmsCore.Extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Permissions.PlayerPerms;

public final class GameModeCommands implements TabExecutor {

    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If a gamemode alias is used, re-run this command
        switch(label){
			case "gms": case "gm0": case "s": case "survival": case "0":
			case "gmc": case "gm1": case "c": case "creative": case "1":
			case "gma": case "gm2": case "a": case "adventure": case "2":
			case "gmsp": case "gm3": case "sp": case "spectator": case "3": case "vanish": case "noclip": case "spectate":
                // Turn the label into the first arg
                String[] newArgs = new String[args.length+1];
                newArgs[0] = label;
                System.arraycopy(args, 0, newArgs, 1, args.length);

                // Re-run the command with the label chnaged to an arg
                return onCommand(sender, command, "gamemode", newArgs);

            // Otherwise, proceed as normal
			default:
                break;
		}


        // Check number of args
        if(args.length==0){
            Utils.sendActionBar(sender, Utils.errorText+"Specify a game mode to switch to");
            return false;
        }

        // Identify gamemode
		GameMode gameMode;
		switch(args[0]){
			// Survival mode
			case "gms": case "gm0": case "s": case "survival": case "0":
				gameMode = GameMode.SURVIVAL; break;
			// Creative mode
			case "gmc": case "gm1": case "c": case "creative": case "1":
				gameMode = GameMode.CREATIVE; break;
			// Adventure mode
			case "gma": case "gm2": case "a": case "adventure": case "2":
				gameMode = GameMode.ADVENTURE; break;
			// Spectator mode
			case "gmsp": case "gm3": case "sp": case "spectator": case "3": case "vanish": case "noclip": case "spectate":
				gameMode = GameMode.SPECTATOR; break;
			// Unknown
			default:
				return Error.INVALID_ARGS.displayActionBar(sender);
		}

        // If sender doesn't have permission for that gamemode
        String permission = "realms.gamemode."+gameMode.toString().toLowerCase();
		if(!sender.hasPermission(permission)){
            // Check if their cheat set has the permission, and if so, switch to it
            PlayerPerms playerPerms = new PlayerPerms((Player)sender);
            if(playerPerms.getCheat()!=null && playerPerms.getCheat().getTotalPermissions().contains(permission)){
                playerPerms.applyCheatSet();
            } else {
                sender.sendMessage(Utils.errorText+"You can't switch to this gamemode! Ask an admin for help.");
			    return false;
            }
		}


        // If one arg, changing own gamemode
        if(args.length==1 && sender instanceof Player && sender.hasPermission(permission)){
            Player player = (Player) sender;
			Utils.notifyAdmins(player.getDisplayName()+Utils.infoText+" switched to "+gameMode.toString().toLowerCase()+" mode.");
			Utils.sendActionBar(player, Utils.messageText+"Switched to "+gameMode.toString().toLowerCase()+" mode.");
            player.setGameMode(gameMode);
            return true;
        }


        // If two args, changing another player's gamemode
        if(args.length==2){
            // Check permissions
            if(!sender.hasPermission("realms.gamemode.others") || !sender.hasPermission(permission)) return Error.NO_PERMISSION.displayChat(sender);

            // Check that player is valid
            Player targetPlayer = Utils.getPlayer(args[1]);
			if(targetPlayer==null){
				return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
			}

			Utils.notifyAdmins(targetPlayer.getDisplayName()+Utils.infoText+" was switched to "+gameMode.toString().toLowerCase()+" mode by "+sender.getName());
			Utils.sendActionBar(targetPlayer, Utils.messageText+"Switched to "+gameMode.toString().toLowerCase()+" mode.");
            targetPlayer.setGameMode(gameMode);
            return true;
        }


        return Error.INVALID_ARGS.displayActionBar(sender);
    }


    //// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        //// Completing player names
        if(args.length>1 && sender.hasPermission("realms.gamemode.others")){
            return null;
        }

        //// Completing gamemode
        if(args.length<=1){
            List<String> modes = new ArrayList<String>();
            
            // Check which gamemodes they have access to, and list those
            for(GameMode mode : GameMode.values()){
                if(sender.hasPermission("realms.gamemode."+mode.toString().toLowerCase())){
                    modes.add(mode.toString());
                }
            }

            return modes;
        }

        return Arrays.asList("");
    }
}