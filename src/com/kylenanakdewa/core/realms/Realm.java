package com.kylenanakdewa.core.realms;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Represents a team, faction, or nation. A {@link RealmMember} is a member of a Realm.
 * <p>
 * All Realms in-use should be managed by a {@link RealmProvider}. They provide a convienient way to retrieve realms that are in use on the server.
 * @author Kyle Nanakdewa
 */
public interface Realm {

	/**
	 * Gets the short name that uniquely identifies this Realm. This name cannot contain spaces or formatting.
	 * @return the unique identifier for this Realm
	 * @see Realm#getName()
	 */
	public String getIdentifier();

	/**
	 * Gets the full name of this Realm. This name can contain spaces and formatting, does not have to be unique, and is displayed in-game.
	 * @return the full name of this Realm, which may contain color formatting, or the unique identifier of this Realm if one was not set
	 * @see Realm#getIdentifier()
	 */
	public String getName();
	/**
	 * Sets the full name of this Realm. This name can contain spaces and formatting, does not have to be unique, and is displayed in-game.
	 * @param name the new name for this Realm, or null to clear
	 */
	public void setName(String name);

	/**
	 * Gets the tagline of this Realm. This is a short description or slogan for the Realm.
	 * @return the tagline of this Realm, or null if one was not set
	 */
	public String getTagline();
	/**
	 * Sets the tagline of this Realm. This is a short description or slogan for the Realm.
	 * <p>
	 * Not all implementations allow for taglines. In those cases, this method will have no effect.
	 * @param tagline the new tagline for this Realm, or null to clear
	 */
	public void setTagline(String tagline);

	/**
	 * Gets the official color of this Realm. This color is often used to identify this Realm in-game.
	 * @return the ChatColor for this Realm, or null if one was not set
	 */
	public ChatColor getColor();
	/**
	 * Sets the official color of this Realm. This color is often used to identify this Realm in-game.
	 * <p>
	 * Not all implementations allow for realm colors. In those cases, this method will have no effect.
	 * @param color the new ChatColor for this Realm, or null to clear
	 */
	public void setColor(ChatColor color);


	/**
	 * Gets the parent of this Realm. The parent Realm has full control over this Realm.
	 * @return the parent Realm, or null if there is no parent Realm
	 * @see Realm#getAllParentRealms() for getting all parent Realms
	 * @see Realm#getTopParentRealm() for getting the top-most parent only
	 */
	public Realm getParentRealm();
	/**
	 * Sets the parent of this Realm. The parent Realm has full control over this Realm.
	 * <p>
	 * Not all implementations allow for parent realms. In those cases, this method will have no effect.
	 * @param realm the new parent Realm, or null to clear
	 */
	public void setParentRealm(Realm realm);
	/**
	 * Gets a view of all parent Realms, above this Realm. These parent Realms have full control over this Realm.
	 * @return a view of all parent Realms, ordered from lowest to highest, or null if this Realm has no parent Realm
	 * @see Realm#getParentRealm() for getting the immediate parent only
	 * @see Realm#getTopParentRealm() for getting the top-most parent only
	 */
	public List<Realm> getAllParentRealms();
	/**
	 * Gets the top parent of this Realm. The top parent Realm has no parent Realms above it.
	 * @return the top parent Realm, or null if this Realm is a top parent
	 * @see Realm#getParentRealm() for getting the immediate parent only
	 * @see Realm#getAllParentRealms() for getting all parent Realms
	 */
	public Realm getTopParentRealm();
	/**
	 * Gets a view of all child Realms, below this Realm. This Realm has full control over all child Realms.
	 * @return a view of all child Realms, or null if this Realm has no children
	 */
	public Collection<Realm> getChildRealms();


	/**
	 * Gets a view of all logged in player characters, that are members of this Realm.
	 * @return a view of currently online player characters who are members of this Realm
	 */
	public Collection<PlayerCharacter> getOnlineCharacters();
	/**
	 * Gets a view of all logged in players, that are members of this Realm.
	 * @return a view of currently online players who are members of this Realm
	 * @deprecated use {@link Realm#getOnlineCharacters()} instead
	 */
	@Deprecated
	public Collection<Player> getOnlinePlayers();


	/**
	 * Adds a PlayerCharacter to this Realm.
	 * <p>
	 * This will only add the Character directly to this Realm. It will not set any data for the Character, such as titles.
	 * To set this data, use {@link PlayerCharacter#setRealm(Realm)} instead.
	 * @param character the PlayerCharacter to be added
	 */
	public void addPlayer(PlayerCharacter character);
	/**
	 * Adds a Player to this Realm.
	 * @param player the Player to be added
	 */
	@Deprecated
	public void addPlayer(Player player);
	/**
	 * Removes a PlayerCharacter from this Realm.
	 * <p>
	 * This will only add the Character directly to this Realm. It will not set any data for the Character, such as titles.
	 * To set this data, use {@link PlayerCharacter#setRealm(Realm)} instead.
	 * @param character the PlayerCharacter to be removed
	 */
	public void removePlayer(PlayerCharacter character);
	/**
	 * Removes a Player from this Realm.
	 * @param player the Player to be removed
	 */
	@Deprecated
	public void removePlayer(Player player);
}