package com.kylenanakdewa.core.Extras;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.PermsUtils;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ModerationCommands implements CommandExecutor, Listener {

    //// HashMap for muted players - integer is for the task that unmutes them, may be null
    private static final HashMap<UUID,Integer> mutedPlayers = new HashMap<UUID,Integer>();


    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        //// Initial checks for all commands
        // Check permissions - all commands require individual permission nodes, plus double-checked admin
        if(!sender.hasPermission("core."+command.getName()) || !PermsUtils.isDoubleCheckedAdmin(sender)){
            Utils.notifyAdminsError(sender.getName()+CommonColors.ERROR+" failed security check ("+command.getName()+" "+String.join(" ", args)+").");
            return Error.NO_PERMISSION.displayChat(sender);
        }

        // Get target player
        OfflinePlayer targetPlayer = null;
        if(args.length>=1){
            targetPlayer = Utils.getPlayer(args[0], true);
        }
        if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);



        //// Kick command
        if(command.getName().equalsIgnoreCase("kick")){
            // Make sure player is online
            if(!targetPlayer.isOnline()) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

            // Get the kick message
            List<String> messageArgs = new ArrayList<String>(Arrays.asList(args));
            messageArgs.remove(0);
            String message = String.join(" ", messageArgs);
            // If no message supplied, add a generic message
            if(message.length()<=1) message = "Kicked from server";

            Utils.notifyAdminsError(targetPlayer.getName()+CommonColors.ERROR+" was kicked from the server by "+sender.getName()+" (reason: "+message+")");
            ((Player)targetPlayer).kickPlayer(message);
            return true;
        }


        //// Ban command
        if(command.getName().equalsIgnoreCase("ban")){
            // Get the ban message
            List<String> messageArgs = new ArrayList<String>(Arrays.asList(args));
            messageArgs.remove(0);
            String message = String.join(" ", messageArgs);
            // If no message supplied, add a generic message
            if(message.length()<=1) message = null;

            Utils.notifyAdminsError(targetPlayer.getName()+CommonColors.ERROR+" was banned from the server by "+sender.getName()+" (reason: "+message+")");

            // Ban the player
            Bukkit.getBanList(Type.NAME).addBan(targetPlayer.getName(), message, null, sender.getName());
            if(targetPlayer.isOnline()) ((Player)targetPlayer).kickPlayer(message);

            return true;
        }


        // Temp Ban
        if(command.getName().equalsIgnoreCase("tempban")){
            // Get the ban length
            Date expires = null;
            if(args.length>=2){
                String input = args[1];

                // Try to parse as duration, otherwise parse as date
                try{
                    Duration duration = Duration.parse("P"+input);
                    expires = Date.from(Instant.now().plus(duration));
                } catch(DateTimeParseException e){}
                try{
                    LocalDate date = LocalDate.parse(input);
                    expires = Date.from(Instant.from(date));
                } catch(DateTimeParseException e){}
            }
            if(expires==null){
                sender.sendMessage(CommonColors.MESSAGE+"Enter a duration as 1d or T2h30m (T for time), or a date as 2017-12-03 (yyyy-mm-dd).");
                return Error.INVALID_ARGS.displayActionBar(sender);
            }

            // Get the ban message
            List<String> messageArgs =  new ArrayList<String>(Arrays.asList(args));
            messageArgs.remove(1); messageArgs.remove(0);
            String message = String.join(" ", messageArgs);
            // If no message supplied, add a generic message
            if(message.length()<=1) message = null;

            Utils.notifyAdminsError(targetPlayer.getName()+CommonColors.ERROR+" was banned from the server until "+expires+" by "+sender.getName()+" (reason: "+message+")");

            // Ban the player
            Bukkit.getBanList(Type.NAME).addBan(targetPlayer.getName(), message, expires, sender.getName());
            if(targetPlayer.isOnline()) ((Player)targetPlayer).kickPlayer(message);

            return true;
        }


        //// Mute command
        if(command.getName().equalsIgnoreCase("mute")){
            // Get the time, and schedule the unmuting
            long ticks = 0;
            int unmuteTask = 0;
            if(args.length>=2){
                String input = args[1];
                try{
                    Duration duration = Duration.parse("PT"+input);
                    ticks = duration.getSeconds()*20;

                    // Schedule unmuting task
                    final OfflinePlayer finalTargetPlayer = targetPlayer;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.plugin, () -> {
                        unmutePlayer(finalTargetPlayer, null);
                    }, ticks);

                } catch(DateTimeParseException e){}
                if(ticks==0){
                    sender.sendMessage(CommonColors.MESSAGE+"Enter a duration, such as 15s or 2h30m.");
                    return Error.INVALID_ARGS.displayActionBar(sender);
                }
            }

            String lengthString = (ticks==0) ? "" : " for "+ticks/20+" seconds";
            Utils.notifyAdminsError(targetPlayer.getName()+CommonColors.ERROR+" was muted"+lengthString+" by "+sender.getName());

            // Mute the player
            if(targetPlayer.isOnline()) ((CommandSender)targetPlayer).sendMessage(CommonColors.ERROR+"You have been muted. Your chat messages will not be visible to other players.");
            mutedPlayers.put(targetPlayer.getUniqueId(), unmuteTask);

            return true;
        }

        // Unmute
        if(command.getName().equalsIgnoreCase("unmute")){
            unmutePlayer(targetPlayer, sender);
            return true;
        }


        return Error.INVALID_ARGS.displayActionBar(sender);
    }


    //// Event Listener
    // Block chat if a player is muted
    @EventHandler
    public void blockMutedChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        // If player is muted
        if(mutedPlayers.containsKey(player.getUniqueId())){
            // Show message only to player and console
            String message = CommonColors.INFO+"[MUTED]"+String.format(event.getFormat(), player.getDisplayName(), event.getMessage());
            player.sendMessage(message);
            Bukkit.getLogger().info(ChatColor.stripColor(message));

            // Cancel the chat event
            event.setCancelled(true);
        }
    }


    //// Unmuting player
    private void unmutePlayer(OfflinePlayer targetPlayer, CommandSender sender){
        // Cancel unmuting task
        Integer unmuteTask = mutedPlayers.get(targetPlayer.getUniqueId());
        if(unmuteTask!=null){
            Bukkit.getScheduler().cancelTask(unmuteTask);
        }

        // Remove from hashmap
        mutedPlayers.remove(targetPlayer.getUniqueId());

        // Notify player
        if(targetPlayer.isOnline()) ((CommandSender)targetPlayer).sendMessage(CommonColors.MESSAGE+"You are no longer muted.");

        String senderString = (sender==null) ? "automatically" : "by "+sender.getName();
        Utils.notifyAdminsError(targetPlayer.getName()+CommonColors.ERROR+" was unmuted "+senderString);
    }
}