package com.kylenanakdewa.core.Extras;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.kylenanakdewa.core.CoreConfig;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;

public final class ListCommands implements CommandExecutor {
    
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        Prompt list = new Prompt();
        list.addQuestion(CommonColors.INFO+"--- "+CommonColors.MESSAGE+"Online Players ("+Bukkit.getOnlinePlayers().size()+"/"+Bukkit.getMaxPlayers()+")"+CommonColors.INFO+" ---");
        list.addQuestion(CommonColors.INFO+"- "+CoreConfig.adminName+"s"+CommonColors.INFO+" are marked with a "+CoreConfig.adminPrefix);

        for(Player player : (CoreConfig.listAllPlayers ? Bukkit.getOnlinePlayers() : Utils.getOnlineAdmins())){
            
            // Check if AFK
            String afkMessage = AFKListener.afkPlayers.get(player.getUniqueId());
            afkMessage = afkMessage==null ? "" : CommonColors.INFO+" - AFK"+afkMessage;

            list.addAnswer(player.getPlayerListName()+afkMessage, "command_player "+player.getName());
        }

        list.display(sender);
        return true;
    }
}