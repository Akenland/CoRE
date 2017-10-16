package com.KyleNecrowolf.RealmsCore.Extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Player.PlayerData;
import com.KyleNecrowolf.RealmsCore.Player.PlayerListener;
import com.KyleNecrowolf.RealmsCore.Realm.Realm;

public final class MsgCommands implements TabExecutor {

    //// Replies - first UUID is receiving player, second UUID is player to reply to
    private static final HashMap<UUID,UUID> replies = new HashMap<UUID,UUID>();


    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Check permissions
        if(!sender.hasPermission("realms.msg")) return Error.NO_PERMISSION.displayChat(sender);


        // If replying, re-run with target player
        if(label.equalsIgnoreCase("r") && sender instanceof Player){
            Player targetPlayer = Bukkit.getPlayer(replies.get(((Player)sender).getUniqueId()));
            
            // If player not found, show error
            if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

            // Add target as first arg
            String[] newArgs = new String[args.length+1];
                newArgs[0] = targetPlayer.getName();
                System.arraycopy(args, 0, newArgs, 1, args.length);
            return onCommand(sender, command, "msg", newArgs);
        }


        // Minimum 2 args
        if(args.length<2) return Error.INVALID_ARGS.displayActionBar(sender);


        // Sender name
        String senderName = ChatColor.GRAY+"<"+sender.getName()+"> "+ChatColor.RESET;
        if(sender instanceof Player){
            Player player = (Player) sender;
            senderName = String.format(PlayerListener.getChatFormat(player), player.getDisplayName(), "");
        }

        // Message
        List<String> messages = new ArrayList<String>(Arrays.asList(args));
        messages.remove(0);
        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", messages));


        // Target players to receive the message
        List<Player> targetPlayers = new ArrayList<Player>();
        String targetName;
        
        // If first arg is "admin", send to all admins        
        if(args[0].equalsIgnoreCase("admin") || args[0].equalsIgnoreCase("admins")){
            targetPlayers = Utils.getOnlineAdmins();
            targetName = Utils.adminColor+"Admins";
            targetPlayers.remove(sender);
        }
        // If "realm", send to all realm members
        else if(args[0].equalsIgnoreCase("realm") && sender instanceof Player){
            Player player = (Player) sender;
            Realm realm = new PlayerData(player).getRealm();
            // If player has no realm, return error
            if(realm.getName().equals("") || !realm.exists()) return Error.REALM_NOT_FOUND.displayActionBar(sender);
            // Get a list of other online players in realm
            targetPlayers = realm.getOnlinePlayers();
            targetPlayers.remove(player);
            targetName = realm.getColor()+realm.getFullName();
        }
        // Otherwise, send to the named player
        else {
            Player targetPlayer = Utils.getPlayer(args[0]);
            if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
            targetPlayers.add(targetPlayer);
            targetName = targetPlayer.getDisplayName();
        }
        
        String finalMessage = senderName+Utils.infoText+"[To "+targetName+Utils.infoText+"] "+ChatColor.RESET+message;
        sender.sendMessage(finalMessage);

        // Send message to all targeted players
        for(Player target : targetPlayers){
            target.sendMessage(finalMessage);

            // Notify target player
            Utils.sendActionBar(target, "Private message received. Type /r <message...> to reply.");
            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BELL, 10000f, 1f);

            // Save reply
            if(sender instanceof Player) replies.put(target.getUniqueId(), ((Player)sender).getUniqueId());

            // Warn sender if target is AFK
            if(AFKListener.afkPlayers.containsKey(target.getUniqueId())){
                sender.sendMessage(target.getDisplayName()+Utils.messageText+" is AFK, and may not see your message.");
            }
        }
        
        return true;
    }


    //// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        // If on second arg (the message), hide completions
        if(args.length>1) return Arrays.asList("");

        return null;
    }
}