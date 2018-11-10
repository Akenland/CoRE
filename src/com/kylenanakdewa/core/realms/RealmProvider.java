package com.kylenanakdewa.core.realms;

import java.util.Collection;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.realms.Realm;

/**
 * A provider of Realms.
 * <p>
 * CoRE Realms are loaded from RealmProviders. This allows multiple backends to be used with the Realm system.
 * <p>
 * For example, Minecraft Scoreboards can be a realm provider. In that case, Realms are loaded from scoreboard teams.
 * @author Kyle Nanakdewa
 */
public interface RealmProvider {

	/**
	 * Gets all realms.
	 * @return a view of all realms provided by this RealmProvider
	 */
	public Collection<? extends Realm> getAllRealms();

	/**
	 * Gets a Realm by its identifier (a short unique name).
	 * @return the Realm, or null if the Realm does not exist in this provider
	 */
	public Realm getRealm(String identifier);

	/**
	 * Gets the Realm that the specified Character is a member of.
	 * @param character the character
	 * @return the Realm, or null if this Character is not a member of a Realm in this provider
	 */
	public Realm getCharacterRealm(Character character);

	/**
	 * Checks if the specified Character is an officer of their realms in this provider.
	 * @param character the character
	 * @return true if the Character is an officer in the realms that they are a member of
	 */
	public boolean isOfficer(Character character);

	/**
	 * Checks if the specified Character is an officer of the specified realm.
	 * @param character the character
	 * @param realm the realm
	 * @return true if the Character is an officer in the specified Realm
	 */
	public boolean isOfficer(Character character, Realm realm);

	/**
	 * Sets a character as an officer. Officers have full control over their own realm, and all child realms.
	 * <p>
	 * Not all implementations allow for officers. In those cases, this method will have no effect.
	 * @param character the character
	 * @param isOfficer true to set this character as an officer, or false to revoke it
	 */
	public void setOfficer(Character character, boolean isOfficer);

}