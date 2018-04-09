package com.kylenanakdewa.core.Extras;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.kylenanakdewa.core.CoreConfig;
import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;

public final class AFKListener implements Listener, CommandExecutor {
    
    // HashMap of AFK players - with AFK message
    public static final HashMap<UUID,String> afkPlayers = new HashMap<UUID,String>();
    // Stored timers for players that could be marked as AFK
    private static final HashMap<UUID,Integer> afkTimers = new HashMap<UUID,Integer>();

    // Whether AFK timer is enabled
    private static final boolean afkTimerEnabled = (CoreConfig.afkTime==0) ? false : true;

    // Time before player is marked as AFK
    private static int afkTime = (int)(CoreConfig.afkTime*20);


    //// SETTING AFK STATUS
    // Set a player as AFK
    public void setAFK(Player player, String reason){
        // Cancel timer
        UUID uuid = player.getUniqueId();
        Integer timer = afkTimers.get(uuid);
        if(timer!=null){
            Bukkit.getScheduler().cancelTask(timer);
            afkTimers.remove(uuid);
        }

        // Format reason correctly
        reason = (reason==null || reason.equals("")) ? "" : " - "+ChatColor.translateAlternateColorCodes('&', reason);

        // Send message to all online players
        Utils.notifyAll(player.getDisplayName()+CoreConfig.joinQuitColor+" is now AFK"+(reason.length()>2 ? reason : "."));

        // Set sleeping ignored
        player.setSleepingIgnored(true);

        // Add to HashMap
        afkPlayers.put(player.getUniqueId(), reason);
    }
    // Cancel AFK status
    public void cancelAFK(Player player){
        UUID uuid = player.getUniqueId();

        // Cancel timer
        Integer timer = afkTimers.get(uuid);
        if(timer!=null){
            Bukkit.getScheduler().cancelTask(timer);
            afkTimers.remove(uuid);
        } // If player is AFK, cancel it
        else if(afkPlayers.containsKey(player.getUniqueId())){
            // Send message to all online players
            Utils.notifyAll(player.getDisplayName()+CoreConfig.joinQuitColor+" is no longer AFK.");

            // Set sleeping ignored
            player.setSleepingIgnored(false);

            // Remove from HashMap
            afkPlayers.remove(player.getUniqueId());
        }


        // Schedule the AFK status, and save the scheduled task so it can be cancelled
        if(afkTimerEnabled){
            afkTimers.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.plugin, () -> {
                setAFK(player, null);
            }, afkTime));
        }
    }


    //// EVENT HANDLERS
    // Start timer on player join
    @EventHandler
    public void afkPlayerJoin(PlayerJoinEvent event){
        // Start timer
        cancelAFK(event.getPlayer());
    }
    // End timer and remove player on quit
    @EventHandler
    public void afkPlayerQuit(PlayerQuitEvent event){
        UUID uuid = event.getPlayer().getUniqueId();

        // Cancel timer
        Integer timer = afkTimers.get(uuid);
        if(timer!=null) Bukkit.getScheduler().cancelTask(timer);

        // Remove player from hashmaps
        afkTimers.remove(uuid);
        afkPlayers.remove(uuid);
    }
    // Reset timer/status on movement, chat, and commands
	@EventHandler
	public void afkPlayerMovement(PlayerMoveEvent event){
        // If player moves at least 1 block, cancel AFK timer/status
        if(event.getFrom().distanceSquared(event.getTo()) > 0.02) cancelAFK(event.getPlayer());
    }
    @EventHandler
    public void afkPlayerChat(AsyncPlayerChatEvent event){
        // Cancel AFK timer/status on chat message
        cancelAFK(event.getPlayer());
    }
    @EventHandler
    public void afkPlayerCommand(PlayerCommandPreprocessEvent event){
        // If command is /vanish (spectator mode), do not cancel AFK
        if(event.getMessage().equalsIgnoreCase("/vanish")) return;

        // Cancel AFK timer/status on command
        cancelAFK(event.getPlayer());
    }
    // Shorten timer when players are in bed
    @EventHandler
    public void afkTimeEnterBed(PlayerBedEnterEvent event){
        // Halve the time every time a player enters bed
        afkTime = afkTime/2;
    }
    @EventHandler
    public void afkTimeLeaveBed(PlayerBedLeaveEvent event){
        // Reset to config value
        afkTime = (int)(CoreConfig.afkTime*20);
    }


    //// COMMANDS
    @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player && sender.hasPermission("core.afk") && CoreConfig.afkEnabled){
            setAFK((Player)sender, String.join(" ", args));
            return true;
        }

        return Error.NO_PERMISSION.displayChat(sender);
    }
}