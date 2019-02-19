package com.kylenanakdewa.core.characters.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles commands and events for Player Characters.
 * @author Kyle Nanakdewa
 */
public class PlayerCharacterManager implements Listener, TabExecutor {

	/** The CoRE plugin instance. */
	static CorePlugin plugin;

	/**
	 * Creates a PlayerCharacterManager for the specified instance of Project CoRE, and registers listeners on the server.
	 */
	public PlayerCharacterManager(CorePlugin plugin){
		PlayerCharacterManager.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Gets the character involved in an event.
	 */
	private PlayerCharacter getCharacter(PlayerEvent event){
		return PlayerCharacter.getCharacter(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent event){
		getCharacter(event).onJoin(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event){
		getCharacter(event).onQuit(event);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		getCharacter(event).onChat(event);
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		// Check aliases for nick
        if(label.equalsIgnoreCase("nick")){
            if(args.length!=2) return Error.INVALID_ARGS.displayActionBar(sender);
            return onCommand(sender, command, "player", new String[]{args[0], "set", "name", args[1]});
        }

        // If first arg is reload, reload chat formats
        if(args.length==1 && args[0].equalsIgnoreCase("reload")){
            if(!sender.hasPermission("core.admin")) return Error.NO_PERMISSION.displayChat(sender);

			PlayerCharacter.reloadCharacters();

            Utils.sendActionBar(sender, "Reloaded chat formatting and display names.");
            return true;
        }


        final OfflinePlayer player;
        final PlayerCharacter playerCharacter;

        // Always make sure first arg is a valid player
        if(args.length>=1){
            player = Utils.getPlayer(args[0], true);
            if(player==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
            playerCharacter = PlayerCharacter.getCharacter(player);
        } else return Error.INVALID_ARGS.displayActionBar(sender);


        // One arg - view player info
		if(args.length==1){
            if(!sender.hasPermission("core.player")) return Error.NO_PERMISSION.displayChat(sender);
			playerCharacter.getInfoPrompt(sender.hasPermission("core.admin")).display(sender);
			return true;
        }


        // Setting data
        if(args.length>3 && args[1].equalsIgnoreCase("set")){
            if(!sender.hasPermission("core.player.setdata")) return Error.NO_PERMISSION.displayChat(sender);

            String key = args[2].toLowerCase();

            // Merge all remaining args into a single string
            List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
            for(int i=0; i<3; i++) lastArgs.remove(0);
		    String data = ChatColor.translateAlternateColorCodes('&', String.join(" ", lastArgs));

			switch (key){
				case "name":
					playerCharacter.setName(data);
					Utils.sendActionBar(sender, playerCharacter.getUsername()+CommonColors.MESSAGE+"'s name set to "+playerCharacter.getName());
					break;
				case "title":
					playerCharacter.setTitle(data);
					Utils.sendActionBar(sender, playerCharacter.getName()+CommonColors.MESSAGE+"'s title set to "+playerCharacter.getTitle());
					break;
				default:
					playerCharacter.getData(plugin).getData().set(key, data);
					playerCharacter.saveData();
					sender.sendMessage(playerCharacter.getName()+CommonColors.MESSAGE+"'s character was updated with custom data: "+key+": "+data);
					break;
			}

            return true;
		}
		
		if(args.length==2 && args[1].equalsIgnoreCase("respawn")){
			if(player.isOnline() && player.getPlayer().isDead()){
				player.getPlayer().spigot().respawn();
				Utils.sendActionBar(sender, playerCharacter.getName()+CommonColors.MESSAGE+" was respawned.");
				return true;
			}
			else {
				Utils.sendActionBar(sender, playerCharacter.getName()+CommonColors.ERROR+" is not dead!");
				return false;
			}
		}


        return Error.INVALID_ARGS.displayChat(sender);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		if(sender.hasPermission("core.player.setdata")){
			if(args.length==2)
				return Arrays.asList("set");
        	if(args.length==3 && args[1].equalsIgnoreCase("set"))
        		return Arrays.asList("name", "title");
			if(args.length>3 && args[1].equalsIgnoreCase("set"))
				return Arrays.asList("");
		}

        return null;
	}

}