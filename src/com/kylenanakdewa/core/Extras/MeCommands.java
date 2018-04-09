package com.kylenanakdewa.core.Extras;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;

public final class MeCommands implements TabExecutor {

    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permissions
        if(!sender.hasPermission("core.msg")) return Error.NO_PERMISSION.displayChat(sender);

        // Make sure there are args
        if(args.length==0) return Error.INVALID_ARGS.displayActionBar(sender);

        // Get sender name
        String senderName = (sender instanceof Player) ? ((Player)sender).getDisplayName() : sender.getName();

        // Send message to all online players
        for(Player target : Bukkit.getOnlinePlayers()){
            target.sendMessage(CommonColors.MESSAGE+"*"+senderName+CommonColors.MESSAGE+" "+String.join(" ", args)+CommonColors.MESSAGE+"*");
        }

        return true;
    }


    //// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Hide completions
        return Arrays.asList("");
    }
}