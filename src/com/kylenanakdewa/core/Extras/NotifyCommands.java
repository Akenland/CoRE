package com.kylenanakdewa.core.Extras;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;

public final class NotifyCommands implements CommandExecutor {

    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Check permissions
        if(!sender.hasPermission("core.admin")) return Error.NO_PERMISSION.displayChat(sender);

        // Make sure there are args
        if(args.length==0) return Error.INVALID_ARGS.displayActionBar(sender);
        String message = String.join(" ", args);

        if(command.getName().equalsIgnoreCase("notifyadmins")) Utils.notifyAdmins(message);
        if(command.getName().equalsIgnoreCase("notifyall")) Utils.notifyAll(message);

        return true;
    }
}