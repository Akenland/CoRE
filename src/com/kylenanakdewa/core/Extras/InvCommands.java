package com.kylenanakdewa.core.Extras;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.Permissions.PermsUtils;

public final class InvCommands implements CommandExecutor {
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        //// Initial checks for all commands
        // Check permissions - all commands require individual permission nodes, plus double-checked admin
        if(!sender.hasPermission("core."+command.getName()) || !PermsUtils.isDoubleCheckedAdmin(sender)){
            Utils.notifyAdminsError(sender.getName()+CommonColors.ERROR+" failed security check ("+command.getName()+" "+String.join(" ", args)+").");
            return Error.NO_PERMISSION.displayChat(sender);
        }

        // Command must be run in-game
        if(!(sender instanceof Player)) return Error.NO_PERMISSION.displayChat(sender);

        // Get target player
        final Player targetPlayer = args.length==1 ? Utils.getPlayer(args[0]) : (Player) sender;
        if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);


        Inventory inv = null;
        String invName = null;

        // Get the inventory
        switch(command.getName().toLowerCase()){
            case "inventory":
                inv = targetPlayer.getInventory();
                invName = "inventory";
                break;
            case "enderchest":
                inv = targetPlayer.getEnderChest();
                invName = "Ender Chest";
                break;
        }

        // Show the inventory to the sender
        Utils.sendActionBar(sender, "Showing "+invName+" of "+targetPlayer.getDisplayName());
        ((Player)sender).openInventory(inv);
        
        return true;
    }
}