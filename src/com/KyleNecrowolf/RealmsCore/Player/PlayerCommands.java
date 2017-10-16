package com.KyleNecrowolf.RealmsCore.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;

// /player command - manage player data
public final class PlayerCommands implements TabExecutor {

    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Check aliases for nick
        if(label.equalsIgnoreCase("nick")){
            if(args.length!=2) return Error.INVALID_ARGS.displayActionBar(sender);
            return onCommand(sender, command, "player", new String[]{args[0], "set", "displayname", args[1]});
        }

        // If first arg is reload, reload chat formats
        if(args.length==1 && args[0].equalsIgnoreCase("reload")){
            if(!sender.hasPermission("realms.admin")) return Error.NO_PERMISSION.displayChat(sender);
            
            // Clear all cached chat formats
            PlayerListener.chatFormats.clear();
            // Reload everyone's display names
            for(Player player : Bukkit.getOnlinePlayers()) new PlayerData(player).loadDisplayName();

            Utils.sendActionBar(sender, "Reloaded chat formatting and display names.");
            return true;
        }


        final OfflinePlayer player;
        final PlayerData playerData;

        // Always make sure first arg is a valid player
        if(args.length>=1){
            player = Utils.getPlayer(args[0], true);
            if(player==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
            playerData = new PlayerData(player);
        } else return Error.INVALID_ARGS.displayActionBar(sender);


        // One arg - view player info
		if(args.length==1){
            if(!sender.hasPermission("realms.player")) return Error.NO_PERMISSION.displayChat(sender);
			return playerData.displayInfo(sender);
        }


        // Setting data
        if(args.length>3 && args[1].equalsIgnoreCase("set")){
            if(!sender.hasPermission("realms.player.set")) return Error.NO_PERMISSION.displayChat(sender);

            String key = args[2];

            // Merge all remaining args into a single string
            List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
            for(int i=0; i<3; i++) lastArgs.remove(0);
		    String data = ChatColor.translateAlternateColorCodes('&', String.join(" ", lastArgs));
		
		    // Store the data in playerdata
		    playerData.save("playerdata."+key, data);
		
		    // Update chat format and display name
		    PlayerListener.chatFormats.remove(player.getUniqueId());
            if(player.isOnline()) playerData.loadDisplayName();
            
            sender.sendMessage(playerData.getDisplayName()+Utils.messageText+"'s player data was updated with new "+key+": "+data);
            return true;
        }


        return Error.INVALID_ARGS.displayChat(sender);
    }


    //// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        // Set command - return common data keys
        if(args.length==3 && args[1].equalsIgnoreCase("set")){
            return Arrays.asList("displayname", "title", "realm");
        }

        // Set realm command - return realms
        if(args.length==4 && args[1].equalsIgnoreCase("set") && args[2].equalsIgnoreCase("realm")){
            if(sender.hasPermission("realms.admin")) return new ArrayList<String>(new ConfigAccessor("realms.yml").getConfig().getConfigurationSection("realms").getKeys(false));
            else if (sender instanceof Player) return Arrays.asList(new PlayerData((Player)sender).getRealm().getName());
        }

        if(args.length==2){
            return Arrays.asList("set");
        }

        return null;
    }
}