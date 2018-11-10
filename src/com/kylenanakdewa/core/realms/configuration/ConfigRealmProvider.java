package com.kylenanakdewa.core.realms.configuration;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.realms.Realm;
import com.kylenanakdewa.core.realms.RealmProvider;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A RealmProvider that uses a Bukkit {@link ConfigurationSection}.
 * <p>
 * Realms are loaded from keys in the ConfigurationSection. The Realm data is loaded from deeper keys.
 * Members are not stored in the Realm's data, but rather in the character's data file.
 * @author Kyle Nanakdewa
 */
public class ConfigRealmProvider implements RealmProvider {

	/** The ConfigurationSection that backs this provider. */
	private final ConfigurationSection config;

	/** The Realms provided by this provider. */
	private Map<String,ConfigRealm> realms;

	/**
	 * Creates a RealmProvider for the specified ConfigurationSecion.
	 * <p>
	 * Remember that ConfigurationSections must be saved in order to be persistent!
	 * @param config the ConfigurationSection to use
	 */
	public ConfigRealmProvider(ConfigurationSection config){
		this.config = config;
		realms = new HashMap<>();
	}

	/**
	 * Updates the realms in this provider.
	 */
	private void updateRealms(){
		// Remove realms which no longer exist in the config
		realms.keySet().removeIf(key -> !config.getKeys(false).contains(key));

		// Add new realms from the config
		config.getKeys(false).forEach(key -> realms.putIfAbsent(key, new ConfigRealm(this, key)));;
	}


	/**
	 * Gets the ConfigurationSection that is used for this provider.
	 * @return the ConfigurationSection backing this provider
	 */
	ConfigurationSection getConfig(){
		return config;
	}


	@Override
	public Collection<? extends Realm> getAllRealms() {
		updateRealms();
		return realms.values();
	}

	@Override
	public Realm getRealm(String identifier) {
		updateRealms();
		return realms.get(identifier);
	}

	@Override
	public Realm getCharacterRealm(Character character) {
		updateRealms();

		// This only works for PlayerCharacters
		if(character instanceof PlayerCharacter){
			String realmName = ((PlayerCharacter)character).getData(CorePlugin.plugin).getData().getString("realm");
			if(realmName!=null) return getRealm(realmName);
		}
		return null;
	}

	@Override
	public boolean isOfficer(Character character) {
		updateRealms();

		// This only works for PlayerCharacters
		if(character instanceof PlayerCharacter){
			// Always return true if player has permission
			if(((PlayerCharacter)character).isOnline() && ((Player)((PlayerCharacter)character).getPlayer()).hasPermission("core.realm.globalofficer")) return true;

			return ((PlayerCharacter)character).getData(CorePlugin.plugin).getData().getBoolean("realmofficer");
		}
		return false;
	}

	@Override
	public boolean isOfficer(Character character, Realm realm) {
		Realm characterRealm = getCharacterRealm(character);

		// Character must be in a realm, be an officer, and be in either the specified realm, or a parent realm
		// If character is in the specified realm, return true
		return isOfficer(character) && characterRealm!=null && (characterRealm.equals(realm) || characterRealm.getChildRealms().contains(realm));
	}

	@Override
	public void setOfficer(Character character, boolean isOfficer) {
		updateRealms();

		// This only works for PlayerCharacters
		if(character instanceof PlayerCharacter){
			((PlayerCharacter)character).getData(CorePlugin.plugin).getData().set("realmofficer", isOfficer);
			((PlayerCharacter)character).saveData();
		}
	}

}