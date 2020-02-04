package com.kylenanakdewa.core.permissions;

import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import com.kylenanakdewa.core.CoreConfig;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;

public final class PermsUtils {

    // Double-check a sender to make sure they're an admin
    public static boolean isDoubleCheckedAdmin(CommandSender sender){
        // If a player, check that player's admin status
        if(sender instanceof Player){
            if(new PlayerPerms((Player) sender).isAdmin()){
                return true;
            }
        }

        // If it's a console, make sure console is allowed in config
        if(sender instanceof ConsoleCommandSender){
            if(CoreConfig.allowConsoleCommands){
                return true;
            }
            Utils.notifyAdminsError("CoRE blocked the console from admin access. To change this, enable console admin commands in coreconfig.yml.");
        }

        // If it's a command block, make sure command blocks are allowed in config
        if(sender instanceof BlockCommandSender){
            Block cmdBlock = ((BlockCommandSender)sender).getBlock();
            if(CoreConfig.allowCommandBlocksCommands){
                Utils.notifyAdmins("CoRE allowed a command block at "+cmdBlock.getX()+" "+cmdBlock.getY()+" "+cmdBlock.getZ()+" to use an admin command.");
                return true;
            }
            Utils.notifyAdminsError("CoRE blocked a command block at "+cmdBlock.getX()+" "+cmdBlock.getY()+" "+cmdBlock.getZ()+" from admin access. To change this, enable command block admin commands in coreconfig.yml.");
        }

        // If it's RCON, make sure RCON is allowed in config
        if(sender instanceof RemoteConsoleCommandSender){
            if(CoreConfig.allowRconCommands){
                Utils.notifyAdmins("CoRE allowed a remote console (RCON) to use an admin command.");
                return true;
            }
            Utils.notifyAdminsError("CoRE blocked a remote console (RCON) from admin access. To change this, enable RCON admin commands in coreconfig.yml.");
        }

        // Otherwise, return false and show error
        Utils.notifyAdminsError(sender.getName()+CommonColors.ERROR+" failed security check (Admin Multi-Check failed).");
        return false;
    }

}