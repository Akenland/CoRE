package com.KyleNecrowolf.RealmsCore.Player;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.KyleNecrowolf.RealmsCore.ConfigValues;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Permissions.PlayerPerms;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

public final class PlayerListener implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		PlayerPerms data = new PlayerPerms(player);

		// Set up permissions, apply the player's default set
		final boolean isOp = player.isOp();
		if(ConfigValues.permsEnabled) data.applyDefaultSetOnJoin();

		if(ConfigValues.formatChat){
			// Load their nickname
			data.loadDisplayName();

			// Set join message
			String joinMessage = String.format(ConfigValues.joinQuitColor+ConfigValues.joinMessage, player.getDisplayName()+ConfigValues.joinQuitColor);
			event.setJoinMessage(joinMessage+".");
			// If player was opped, hide their join message
			if(isOp){
				Utils.notifyAdmins(joinMessage+" silently.");
				event.setJoinMessage(null);
			}
			// Check if this is their first time on the server
			if(!player.hasPlayedBefore()){
				event.setJoinMessage(joinMessage+". "+ConfigValues.firstJoinMessage);
			}
		}

		// Send them a list of online admins
		if(ConfigValues.showAdminListOnJoin) player.sendMessage(Utils.infoText+"Online "+ConfigValues.adminName+"s: "+Utils.listOnlineAdmins());
		
		// Show them the motd_join prompt
		if(ConfigValues.showMotdPromptOnJoin) new Prompt("motd","join").display(player);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();

		if(ConfigValues.formatChat){
			// Set quit message
			String quitMessage = String.format(ConfigValues.joinQuitColor+ConfigValues.quitMessage, player.getDisplayName()+ConfigValues.joinQuitColor);
			event.setQuitMessage(quitMessage+".");
			// If player was opped, hide their join message
			if(player.isOp()){
				Utils.notifyAdmins(quitMessage+" silently.");
				event.setQuitMessage(null);
			}
		}


		// Remove permissions
		new PlayerPerms(player).removePermissions();

		// Remove cached chat format
		chatFormats.remove(player.getUniqueId());
	}
	
	
	static HashMap<UUID,String> chatFormats = new HashMap<UUID,String>(); 
	public static String getChatFormat(Player player){
		// Check if there's a cached message format for this player
		String chatFormat = chatFormats.get(player.getUniqueId());
		if(chatFormat==null){
			new PlayerData(player).loadChatFormat();
			chatFormat = chatFormats.get(player.getUniqueId());
		}
		return chatFormat;
	}
	@EventHandler
	public void onChatMessage(AsyncPlayerChatEvent event){
		if(!ConfigValues.formatChat) return;

		// Convert &codes into colors
		event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
		
		Player player = event.getPlayer();
		
		// Use the cached chat format
		event.setFormat(getChatFormat(player));
	}
}
