package com.kylenanakdewa.core.characters.players;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.kylenanakdewa.core.CoreConfig;
import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.Extras.AFKListener;
import com.kylenanakdewa.core.Permissions.PlayerPerms;
import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.ConfigAccessor;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.core.common.savedata.SaveDataSection;
import com.kylenanakdewa.core.realms.Realm;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Represents a character that is a {@link Player} on a Bukkit server.
 * <p>
 * CoRE uses this class for all player-related features, and it is the primary way to access CoRE data for a player.
 * <p>
 * This class also provides methods to store your own plugin's data within the character data.
 * @author Kyle Nanakdewa
 */
public class PlayerCharacter implements Character {

	/** All loaded PlayerCharacters on this server. */
	private static final Map<UUID,PlayerCharacter> playerCharacters = new HashMap<UUID,PlayerCharacter>();
	/** The Project CoRE plugin instance. */
	private static final CorePlugin plugin = PlayerCharacterManager.plugin;


	/** The Player object associated with this Character. */
	private final OfflinePlayer player;
	/** The ConfigAccessor associated with this Character. */
	private final ConfigAccessor dataFile;

	/** The name of this Character. This is the display name (nickname), not the Minecraft profile name (username). */
	private String name;
	/** The title of this Character. */
	private String title;
	/** The Realm of this Character. */
	private Realm realm;

	/** The chat format for the player. */
	private String chatFormat;

	/** The IP Address of the player. */
	private String ipAddress;
	/** The last join timestamp of the player. */
	private long lastLogin;
	/** The first join timestamp of the player. */
	private long firstLogin;

	private PlayerCharacter(OfflinePlayer player){
		this.player = player;
		dataFile = new ConfigAccessor("players"+File.separator+getUniqueId()+".yml");

		// Load name and title from player file
		ConfigurationSection coreData = getData(plugin);
		name = coreData.getString("displayname", getUsername());
		title = coreData.getString("title");
		ipAddress = coreData.getString("ip");
		lastLogin = coreData.getLong("lastlogin", player.getLastPlayed());
		firstLogin = coreData.getLong("firstlogin", player.getFirstPlayed());

		// Load Realm from server's RealmProvider
		plugin.getRealmProvider().getCharacterRealm(this);
	}

	/**
	 * Gets a PlayerCharacter object for the specified player.
	 * @param player the player
	 * @return the PlayerCharacter object
	 */
	public static PlayerCharacter getCharacter(OfflinePlayer player){
		// Retrieve character
		PlayerCharacter character = playerCharacters.get(player.getUniqueId());

		// If not present, create it
		if(character==null){
			character = new PlayerCharacter(player);
			playerCharacters.put(player.getUniqueId(), character);
		}

		return character;
	}
	/**
	 * Clears inactive PlayerCharacters from memory.
	 */
	private static void clearCharacters(){
		playerCharacters.keySet().forEach(uuid -> {
			// If player is not online, clear from memory
			if(!Bukkit.getPlayer(uuid).isOnline())
				playerCharacters.remove(uuid);
		});
	}
	/**
	 * Reloads all online PlayerCharacters.
	 */
	static void reloadCharacters(){
		playerCharacters.values().forEach(character -> character.updateDisplayName());
	}


	/**
	 * Checks if the Player represented by this Character is online.
	 * @return true if the player is logged in to the server
	 */
	public boolean isOnline(){
		return player.isOnline();
	}

	/**
	 * Gets the username (profile name) of the Player represented by this Character.
	 * @return the username (profile name) of the player
	 * @see OfflinePlayer#getName()
	 */
	public String getUsername(){
		return player.getName();
	}
	/**
	 * Gets the UUID of the Player represented by this Character.
	 * @return the UUID of the player
	 * @see OfflinePlayer#getUniqueId()
	 */
	public UUID getUniqueId(){
		return player.getUniqueId();
	}
	/**
	 * Gets the Bukkit Player represented by this Character.
	 * @return the player
	 */
	public OfflinePlayer getPlayer(){
		return player;
	}

	/**
	 * Checks if the Player represented by this Character is a server admin.
	 * @return true if the player is an admin
	 * @deprecated this needs to be redone
	 */
	@Deprecated
	private boolean isAdmin(){
		return new PlayerPerms(player).isAdmin();
	}


	@Override
	public String getName(){
		return ChatColor.getLastColors(getTitle()) + name;
	}
	@Override
	public void setName(String name){
		this.name = name;
		updateDisplayName();
	}

	@Override
	public String getTitle(){
		return (realm!=null ? realm.getColor() : "") + (title!=null ? title : "");
	}
	@Override
	public void setTitle(String title){
		this.title = title;
		updateDisplayName();
	}

	@Override
	public String getFormattedName(){
		return getTitle() + (ChatColor.stripColor(getTitle()).length()>0 ? " " : "") + name;
	}

	/**
	 * Updates the display name, list name, and chat format, for the Player represented by this Character.
	 * This must be called after changing data, to make the changes visible.
	 */
	private void updateDisplayName(){
		if(!CoreConfig.formatChat || !isOnline()) return;

		// Display name - used in most messages
		((Player)player).setDisplayName(name);

		// Tab List name - should have admin prefix
		String adminPrefix = isAdmin() ? CoreConfig.adminPrefix+ChatColor.RESET : "";
		((Player)player).setPlayerListName(adminPrefix+name);

		// Chat format string
		ChatColor realmColor = realm!=null ? realm.getColor() : ChatColor.GRAY;
		ChatColor topParentRealmColor = realm!=null && realm.getTopParentRealm()!=null ? realm.getTopParentRealm().getColor() : realmColor;
		String spacedTitle = getTitle() + (ChatColor.stripColor(getTitle()).length()>0 ? " " : "");

		chatFormat = topParentRealmColor+"<" + adminPrefix+ChatColor.GRAY + spacedTitle+"%s" + topParentRealmColor+"> " + ChatColor.RESET+"%s";
	}


	@Override
	public Realm getRealm(){
		return realm;
	}

	@Override
	public void setRealm(Realm realm){
		// Remove them from old realm
		if(this.realm!=null) this.realm.removePlayer(this);

		// Reset their title
		title = null;

		// Add them to new realm
		this.realm = realm;
		if(realm!=null) realm.addPlayer(this);

		updateDisplayName();
	}

	@Override
	public boolean isRealmOfficer(){
		return plugin.getRealmProvider().isOfficer(this);
	}


	/**
	 * Gets the IP address of the Player represented by this Character.
	 * @return the last-known IP address, or null if it is unknown
	 * @see Player#getAddress()
	 */
	public String getIP(){
		return ipAddress;
	}
	/**
	 * Gets the time (in milliseconds) of when the Player represented by this Character was last online.
	 * @return the last time the player joined the server, or 0 if they have never been online
	 * @see OfflinePlayer#getLastPlayed()
	 */
	public long getLastLogin(){
		return lastLogin;
	}
	/**
	 * Gets the last join date, as a Java date.
	 * @return the date/time, or null if the player has never been online
	 */
	public LocalDateTime getLastLoginDate(){
		return lastLogin!=0 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(lastLogin), ZoneId.systemDefault()) : null;
	}
	/**
	 * Gets the last join date, as a formatted string.
	 * @return the date/time string, or "Unknown" if the player has never been online
	 */
	public String getLastLoginString(){
		LocalDateTime lastDate = getLastLoginDate();
		return lastDate!=null ? lastDate.format(DateTimeFormatter.ofPattern("MMM d, uuuu, h:mm:ss a")) : "Unknown";
	}
	/**
	 * Gets the time (in milliseconds) of when the Player represented by this Character was first online.
	 * @return the first time the player joined the server, or 0 if they have never been online
	 * @see OfflinePlayer#getFirstPlayed()
	 */
	public long getFirstLogin(){
		return firstLogin;
	}
	/**
	 * Gets the first join date, as a Java date.
	 * @return the date/time, or null if the player has never been online
	 */
	public LocalDateTime getFirstLoginDate(){
		return firstLogin!=0 ? LocalDateTime.ofInstant(Instant.ofEpochMilli(firstLogin), ZoneId.systemDefault()) : null;
	}
	/**
	 * Gets the first join date, as a formatted string.
	 * @return the date/time string, or "Unknown" if the player has never been online
	 */
	public String getFirstLoginString(){
		LocalDateTime firstDate = getFirstLoginDate();
		return firstDate!=null ? firstDate.format(DateTimeFormatter.ofPattern("MMM d, uuuu, h:mm:ss a")) : "Unknown";
	}
	/**
	 * Gets the chat format used for the Player represented by this Character.
	 * The first format parameter is the character's name, and the second parameter is the chat message
	 * @see AsyncPlayerChatEvent#getFormat()
	 */
	public String getChatFormat(){
		return chatFormat;
	}


	/**
	 * Called when the Player represented by this Character joins the server.
	 * @param event the PlayerJoinEvent
	 */
	void onJoin(PlayerJoinEvent event){
		updateDisplayName();
		ipAddress = event.getPlayer().getAddress().getAddress().getHostAddress();

		// Set up permissions, apply the player's default set
		final boolean isOp = player.isOp();
		if(CoreConfig.permsEnabled) new PlayerPerms(event.getPlayer()).applyDefaultSetOnJoin();

		if(CoreConfig.formatChat){
			// Set join message
			String joinMessage = String.format(CoreConfig.joinQuitColor+CoreConfig.joinMessage, getName()+CoreConfig.joinQuitColor);
			event.setJoinMessage(joinMessage+".");
			// If player was opped, hide their join message
			if(isOp){
				Utils.notifyAdmins(joinMessage+" silently.");
				event.setJoinMessage(null);
			}
			// Check if this is their first time on the server
			if(!player.hasPlayedBefore()){
				event.setJoinMessage(joinMessage+". "+CoreConfig.firstJoinMessage);
			}
		}

		// Send them a list of online admins
		if(CoreConfig.showAdminListOnJoin) event.getPlayer().sendMessage(CommonColors.INFO+"Online "+CoreConfig.adminName+"s: "+Utils.listOnlineAdmins());

		// Show them the motd_join prompt
		if(CoreConfig.showMotdPromptOnJoin) Prompt.getFromPluginFolder("motd","join").display(event.getPlayer());
	}
	/**
	 * Called when the Player represented by this Character quits the server.
	 * @param event the PlayerQuitEvent
	 */
	void onQuit(PlayerQuitEvent event){
		lastLogin = event.getPlayer().getLastPlayed();

		if(CoreConfig.formatChat){
			// Set quit message
			String quitMessage = String.format(CoreConfig.joinQuitColor+CoreConfig.quitMessage, getName()+CoreConfig.joinQuitColor);
			event.setQuitMessage(quitMessage+".");
			// If player was opped, hide their join message
			if(event.getPlayer().isOp()){
				Utils.notifyAdmins(quitMessage+" silently.");
				event.setQuitMessage(null);
			}
		}

		// Remove permissions
		new PlayerPerms(player).removePermissions();

		// Remove this stored PlayerCharacter
		playerCharacters.remove(player.getUniqueId());
		// Remove all inactive PlayerCharacters
		clearCharacters();
	}
	/**
	 * Called when the Player represented by this Character sends a chat message.
	 * @param event the AsyncPlayerChatEvent
	 */
	void onChat(AsyncPlayerChatEvent event){
		if(CoreConfig.formatChat){
			// Translate color codes of message
			event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));

			// Set chat format
			event.setFormat(chatFormat);
		}
	}


	/**
	 * Gets a plugin's data section for this Character.
	 * <p>
	 * Any plugin can save data in a player's data file. This data is stored in a seperate key per plugin.
	 * <p>
	 * Changes can be made to this data using {@link ConfigurationSection#set(String, Object)}.
	 * To save changes, you must call {@link PlayerCharacter#saveData()}.
	 * @param plugin the plugin for which to retrieve a data section
	 * @return the plugin's data section, as a Bukkit ConfigurationSection
	 */
	public SaveDataSection getData(Plugin plugin){
		return new SaveDataSection(dataFile.getConfig(), plugin.getName());
		//return dataFile.getConfig().getConfigurationSection(plugin.getName());
	}
	/**
	 * Saves all data for this Character.
	 * <p>
	 * You should not call this method unless there is actually data that needs to be saved.
	 * If your plugin does not need to persist data, do not call this method.
	 * Small amounts of easily re-creatable data should not be saved (but can still be set in the data section).
	 */
	public void saveData(){
		// Save basic data
		ConfigurationSection data = getData(plugin);
		data.set("username", getUsername());
		data.set("ip", ipAddress);
		data.set("firstlogin", firstLogin);
		data.set("lastlogin", lastLogin);
		data.set("displayname", name);
		data.set("title", title);

		dataFile.saveConfig();
	}


	/**
	 * Creates a Prompt with information about this PlayerCharacter.
	 * @param includeAdminInfo whether private info (location, permissions, gamemode, IP, location) should be included in the Prompt
	 */
	public Prompt getInfoPrompt(boolean includeAdminInfo){
		Prompt prompt = new Prompt();
		prompt.addQuestion(CommonColors.INFO+"--- Player Info: "+CommonColors.MESSAGE+getName()+CommonColors.INFO+" ---");

		// Realm info
		if(getRealm()!=null){
			String displayedTitle = getTitle() + ((ChatColor.stripColor(getTitle()).length()<2) ? "Member" : "");
			prompt.addAnswer(displayedTitle+" of the "+getRealm().getName(), "command_realm "+getRealm().getIdentifier());
		}

		// Check if AFK
		if(isOnline()){
			String afkMessage = AFKListener.afkPlayers.get(getUniqueId());
			if(afkMessage!=null) prompt.addAnswer("AFK"+afkMessage, ""); 
		}

		// Check if admin
		if(isAdmin() || (!isOnline() && new PlayerPerms(player).getDefault().hasPermission("core.admin"))) prompt.addAnswer(CoreConfig.adminColor+"Server "+CoreConfig.adminName, "");

		// Basic info
		prompt.addAnswer("Username: "+getUsername()+" "+CommonColors.INFO+getUniqueId(), "url_https://mcuuid.net/?q="+getUsername());
		prompt.addAnswer("First joined "+getFirstLoginString(), "");
		prompt.addAnswer("Last joined "+getLastLoginString(), "");

		// Admin info
		if(includeAdminInfo){
	
			prompt.addAnswer("IP: "+getIP(), "url_http://www.whatsmyip.org/ip-geo-location/?ip="+getIP().substring(getIP().indexOf('/')+1));
			
			// Game info
			if(player.isOnline()){
				Location loc = ((Player) player).getLocation();
				prompt.addAnswer("Location: "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ()+", "+loc.getWorld().getName(), "command_tp "+getUsername());
				prompt.addAnswer(((Player) player).getGameMode()+" mode", "");
				prompt.addAnswer("Permissions: "+new PlayerPerms(player).getCurrentSet().getName(), "command_permissions player "+getUsername());
			}
		}

		return prompt;
	}
}