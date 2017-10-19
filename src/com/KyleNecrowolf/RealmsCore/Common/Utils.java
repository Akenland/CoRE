package com.KyleNecrowolf.RealmsCore.Common;

import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.ConfigValues;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;

public final class Utils {
	
	// Text colours for messages
	public static ChatColor messageText = ConfigValues.messageColor;
	public static ChatColor infoText = ConfigValues.infoColor;
	public static ChatColor errorText = ConfigValues.errorColor;
	public static ChatColor adminColor = ConfigValues.adminColor;
	
	
	//// Methods common to multiple Realms components
	
	// Get a list of online admins
	public static List<Player> getOnlineAdmins(){
		List<Player> admins = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()){
			if(player.hasPermission("realms.admin")) admins.add(player);
		}
		return admins;
	}

	// Notify admins and console
	public static void notifyAdmins(String message){
		notifyAdmins(Level.INFO, message);
	}
	public static void notifyAdminsError(String message){
		notifyAdmins(Level.WARNING, message);
	}
	public static void notifyAdmins(Level level, String message){
		// Prepare the message
		message = Utils.messageText+ChatColor.translateAlternateColorCodes('&', message);
		
		if(level==Level.SEVERE || level==Level.WARNING){
			message = Utils.errorText + message;
		}
		
		for(Player player : Bukkit.getOnlinePlayers()){
			// Send message to all online players with this permission
			if(player.hasPermission("realms.admin")) player.sendMessage(message);
		}
		
		// Log same message to console
		Bukkit.getLogger().log(level, ChatColor.stripColor(message));
	}


	// Send message to all online players
	public static void notifyAll(String message){
		// Prepare the message
		message = Utils.messageText+ChatColor.translateAlternateColorCodes('&', message);

		// Send to every online player
		for(Player player : Bukkit.getOnlinePlayers()) player.sendMessage(message);
		
		// Log same message to console
		Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor(message));
	}
	

	
	
	// List online admins
	public static String listOnlineAdmins(){
		StringBuilder onlineAdmins = new StringBuilder();

		// Build a string with all online admins
		for(Player onlinePlayer : getOnlineAdmins())
			onlineAdmins.append(onlinePlayer.getDisplayName()).append(Utils.infoText+", ");

		// If no online admins, show Phoenix, otherwise remove the extra comma at the end
		if(onlineAdmins.length()<3)
			onlineAdmins.append(ConfigValues.enableWolfiaFeatures ? ChatColor.DARK_RED+"P"+ChatColor.DARK_GRAY+"hoenix" : "None");
		else
			onlineAdmins.delete(onlineAdmins.length()-2, onlineAdmins.length()-1);
		
		return onlineAdmins.toString();
	}
	
	
	// Get player from display name
	public static OfflinePlayer getPlayer(String name, boolean checkOfflinePlayer){
		// First check if it matches a username
		OfflinePlayer player = Bukkit.getPlayer(name);
		
		if(player==null && ConfigValues.searchDisplayNames){
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
		if(player==null && checkOfflinePlayer && ConfigValues.searchOfflineNames){
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
	public static Player getPlayer(String name){
		return (Player) getPlayer(name,false);
	}
	
	
	// Check if server is running latest version
	public static final boolean isLatestVersion(){
		return Bukkit.getVersion().contains("1.12");
	}


	// Send actionbar message - Minecraft 1.12_R1 ONLY
    public static void sendActionBar(Player p, String msg){
		String s = messageText+ChatColor.translateAlternateColorCodes('&', msg);
    	if(isLatestVersion() && ConfigValues.useActionBarMessages){
    		IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
    		PacketPlayOutChat bar = new PacketPlayOutChat(icbc, ChatMessageType.GAME_INFO);
    		((CraftPlayer)p).getHandle().playerConnection.sendPacket(bar);
    	} else {
    		// If the server is not running the correct version, just send a normal chat message
    		p.sendMessage(s);
    	}
      }
    public static void sendActionBar(CommandSender p, String msg){
    	String s = messageText+ChatColor.translateAlternateColorCodes('&', msg);
    	// If it's a player, send the action bar message
    	if(p instanceof Player){
    		sendActionBar((Player)p,s);
    	} else {
    		// If it's not a player, send a normal message
    		p.sendMessage(s);
    	}
    }
}
