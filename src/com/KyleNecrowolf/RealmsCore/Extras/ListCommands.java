package com.KyleNecrowolf.RealmsCore.Extras;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.ConfigValues;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

public final class ListCommands implements CommandExecutor {
    
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        Prompt list = new Prompt();
        list.addQuestion(Utils.infoText+"--- "+Utils.messageText+"Online Players ("+Bukkit.getOnlinePlayers().size()+"/"+Bukkit.getMaxPlayers()+")"+Utils.infoText+" ---");
        list.addQuestion(Utils.infoText+"- "+ConfigValues.adminName+"s"+Utils.infoText+" are marked with a "+ConfigValues.adminPrefix);

        for(Player player : (ConfigValues.listAllPlayers ? Bukkit.getOnlinePlayers() : Utils.getOnlineAdmins())){
            
            // Check if AFK
            String afkMessage = AFKListener.afkPlayers.get(player.getUniqueId());
            afkMessage = afkMessage==null ? "" : Utils.infoText+" - AFK"+afkMessage;

            list.addAnswer(player.getPlayerListName()+afkMessage, "command_player "+player.getName());
        }

        list.display(sender);
        return true;
    }
}