package com.kylenanakdewa.core.common;

import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.kylenanakdewa.core.CoreConfig;

/**
 * A collection of utilities that are used throughout multiple components and plugins.
 * @author Kyle Nanakdewa
 */
public final class Utils {

	// Text colours for messages
	@Deprecated
	public static ChatColor messageText = CoreConfig.messageColor;
	@Deprecated
	public static ChatColor infoText = CoreConfig.infoColor;
	@Deprecated
	public static ChatColor errorText = CoreConfig.errorColor;
	@Deprecated
	public static ChatColor adminColor = CoreConfig.adminColor;


	/**
	 * Gets a view of all currently logged in admins (players with permission "core.admin").
	 * @return a view of currently online admins
	 * @see Bukkit#getOnlinePlayers()
	 */
	public static Collection<Player> getOnlineAdmins(){
		List<Player> admins = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		admins.removeIf(player -> !player.hasPermission("core.admin"));
		return admins;
	}


	/**
	 * Sends a message to all online admins (players with permission "core.admin").
	 * @param message the message to send
	 */
	public static void notifyAdmins(String message){
		notifyAdmins(Level.INFO, message);
	}
	/**
	 * Sends an error/warning message to all online admins (players with permission "core.admin").
	 * @param message the message to send
	 */
	public static void notifyAdminsError(String message){
		notifyAdmins(Level.WARNING, message);
	}
	/**
	 * Sends a message to all online admins (players with permission "core.admin").
	 * @param level the logging level for this message
	 * @param message the message to send
	 */
	public static void notifyAdmins(Level level, String message){
		// Prepare the message
		ChatColor prefixColor = (level==Level.SEVERE || level==Level.WARNING) ? CommonColors.ERROR.getColor() : CommonColors.MESSAGE.getColor();
		String formattedMessage = prefixColor+ChatColor.translateAlternateColorCodes('&', message);

		getOnlineAdmins().forEach(player -> player.sendMessage(formattedMessage));

		// Log same message to console
		Bukkit.getLogger().log(level, ChatColor.stripColor(formattedMessage));
	}


	/**
	 * Sends a message to all online players.
	 * @param message the message to send
	 */
	public static void notifyAll(String message){
		// Prepare the message
		String formattedMessage = CommonColors.MESSAGE+ChatColor.translateAlternateColorCodes('&', message);

		// Send to every online player
		Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(formattedMessage));

		// Log same message to console
		Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor(formattedMessage));
	}


	/**
	 * Gets a string with the names of all online admins (players with permission "core.admin"), seperated by commas.
	 * @return the names of all online admins
	 */
	public static String listOnlineAdmins(){
		StringBuilder onlineAdmins = new StringBuilder();

		// Build a string with all online admins
		getOnlineAdmins().forEach(player -> {
			if(!player.hasPermission("core.invisible")) onlineAdmins.append(player.getDisplayName()).append(CommonColors.INFO+", ");
		});

		// If no online admins, show Phoenix, otherwise remove the extra comma at the end
		if(onlineAdmins.length()<3)
			onlineAdmins.append(CoreConfig.enableWolfiaFeatures ? ChatColor.DARK_RED+"P"+ChatColor.DARK_GRAY+"hoenix" : "None");
		else
			onlineAdmins.delete(onlineAdmins.length()-2, onlineAdmins.length()-1);

		return onlineAdmins.toString();
	}


	/**
	 * Gets a player object for the given username or display name.
	 * @param name the username or display name to look up
	 * @param checkOfflinePlayer true to get offline players (otherwise only online players will be returned)
	 * @return a player if one was found, otherwise null
	 * @see Bukkit#getPlayer(String)
	 * @see Bukkit#getOfflinePlayer(String)
	 */
	public static OfflinePlayer getPlayer(String name, boolean checkOfflinePlayer){
		// First check if it matches a username
		OfflinePlayer player = Bukkit.getPlayer(name);

		if(player==null && CoreConfig.searchDisplayNames){
			// Look through online players and see if it matches any display names
			name = name.toLowerCase();
			for(Player p : Bukkit.getOnlinePlayers()){
				String displayName = ChatColor.stripColor(p.getDisplayName()).toLowerCase();
				// If it's a match, set the player to this
				if(displayName.contains(name)){
					player = p;
					break;
				}
			}
		}

		// If player is still null, and checking offline players, get the offline player
		if(player==null && checkOfflinePlayer && CoreConfig.searchOfflineNames){
			// Look through every player that has ever joined, and see if they're an exact match
			for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
				String pName = p.getName();
				if(name.equalsIgnoreCase(pName)){
					player = p;
					break;
				}
			}
		}

		return player;
	}
	/**
	 * Gets a player object for the given username or display name.
	 * Offline players will not be returned.
	 * @param name the username or display name to look up
	 * @return a player if one was found, otherwise null
	 * @see Bukkit#getPlayer(String)
	 */
	public static Player getPlayer(String name){
		return (Player)getPlayer(name, false);
	}


	/**
	 * Checks if this server is running the latest version of Minecraft (at the time this plugin was released).
	 * @return true if this server is running the Minecraft version that this plugin was designed for
	 */
	public static final boolean isLatestVersion(){
		return Bukkit.getVersion().contains("1.12");
	}


	/**
	 * Sends an action bar message to a {@link Player}. Use this to send non-critical command feedback and status messages to the player.
	 * @param target the {@link Player} to send the message to
	 * @param message the message to send
	 */
	public static void sendActionBar(Player target, String message){
		// Colorize the message
		message = CommonColors.MESSAGE+ChatColor.translateAlternateColorCodes('&', message);

		// If NMS is available and config allows it, Send action bar with NMS
		if(isLatestVersion() && CoreConfig.useActionBarMessages) CommonNMS.sendActionBar(target, message);

		// Otherwise, just send a normal chat message
		else target.sendMessage(message);
	  }
	/**
	 * Sends an action bar message to a {@link CommandSender}. Use this to send non-critical command feedback and status messages to the player.
	 * If the target is not a player, a normal chat message will be sent instead.
	 * @param target the {@link CommandSender} to send the message to
	 * @param message the message to send
	 */
	public static void sendActionBar(CommandSender target, String message){
		// Colorize the message
		message = CommonColors.MESSAGE+ChatColor.translateAlternateColorCodes('&', message);

		// If it's a player, send the action bar message
		if(target instanceof Player) sendActionBar((Player)target, message);

		// If it's not a player, send a normal message
		else target.sendMessage(ChatColor.stripColor(message));
	}


	/**
	 * Sends a raw JSON message to a player. Works exactly like /tellraw.
	 * @param target the player to send the message to
	 * @param rawJsonMsg the raw JSON message to send, must be properly formatted
	 */
	public static void sendRawJson(Player target, String rawJsonMsg){
		// If latest version, use NMS code to send message
		if(isLatestVersion()) CommonNMS.sendRawJson(target, rawJsonMsg);

		// Otherwise, use vanilla command to send message
		else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw "+target.getName()+" "+rawJsonMsg);
	}

}
