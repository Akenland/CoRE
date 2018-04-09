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
			String realmName = ((PlayerCharacter)character).getData(CorePlugin.plugin).getString("realm");
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

			return ((PlayerCharacter)character).getData(CorePlugin.plugin).getBoolean("realmofficer");
		}
		return false;
	}

	@Override
	public void setOfficer(Character character, boolean isOfficer) {
		updateRealms();

		// This only works for PlayerCharacters
		if(character instanceof PlayerCharacter){
			((PlayerCharacter)character).getData(CorePlugin.plugin).set("realmofficer", isOfficer);
			((PlayerCharacter)character).saveData();
		}
	}

}