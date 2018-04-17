package com.kylenanakdewa.core.realms.configuration;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.realms.Realm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * A Realm backed by a Bukkit {@link ConfigurationSection}.
 * <p>
 * This type of Realm does not necessarily need a provider.
 * However, when managing multiple Realms, it can still be convienient to use a ConfigRealmProvider.
 * <p>
 * This type of Realm will only support parent/child realms when a ConfigRealmProvider is used.
 * @author Kyle Nanakdewa
 */
public class ConfigRealm implements Realm {

	/** The ConfigRealmProvider that is providing this Realm. */
	private final ConfigRealmProvider provider;
	/** The ConfigurationSection that backs this Realm. */
	private final ConfigurationSection config;
	/** The unique identifier for this Realm. */
	private final String identifier;


	private ConfigRealm(ConfigRealmProvider provider, ConfigurationSection config, String identifier){
		this.provider = provider;
		this.config = config;
		this.identifier = identifier;
	}
	/**
	 * Creates a new Realm in the specified ConfigRealmProvider.
	 * <p>
	 * The Realm will be backed by a ConfigurationSection, in the provider's ConfigurationSection, under a key matching the identifier.
	 */
	ConfigRealm(ConfigRealmProvider provider, String identifier){
		this(provider, provider.getConfig().getConfigurationSection(identifier), identifier);
	}
	/**
	 * Creates a new Realm backed by the specified ConfigurationSection.
	 * <p>
	 * Does not use or require a ConfigRealmProvider. Parent/child realms will be unavailable.
	 */
	public ConfigRealm(ConfigurationSection config, String identifier){
		this(null, config, identifier);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getName() {
		return config.getString("fullname", identifier);
	}

	@Override
	public void setName(String name) {
		config.set("fullname", name);
	}

	@Override
	public String getTagline() {
		return config.getString("tagline");
	}

	@Override
	public void setTagline(String tagline) {
		config.set("tagline", tagline);
	}

	@Override
	public ChatColor getColor() {
		try{
			return ChatColor.valueOf(config.getString("color"));
		} catch(IllegalArgumentException e){
			return null;
		}
	}

	@Override
	public void setColor(ChatColor color) {
		config.set("color", color.name());
	}

	@Override
	public Realm getParentRealm() {
		String realmName = config.getString("parent");

		// Not available without a provider
		return (provider==null || realmName==null) ? null : provider.getRealm(realmName);
	}

	@Override
	public void setParentRealm(Realm realm) {
		// Not available without a provider
		if(provider!=null){
			config.set("parent", realm.getIdentifier());
		}
	}

	@Override
	public List<Realm> getAllParentRealms() {
		// Not available without a provider
		if(getParentRealm()==null) return null;

		List<Realm> realms = new ArrayList<Realm>();
		Realm realmToCheck = getParentRealm();
		while(realmToCheck!=null){
			realms.add(realmToCheck);
			realmToCheck = realmToCheck.getParentRealm();
		}
		return realms;
	}

	@Override
	public Realm getTopParentRealm() {
		// Not available without a provider
		if(getParentRealm()==null) return null;

		List<Realm> realms = getAllParentRealms();
		// Return the last (highest) parent realm
		return realms.get(realms.size()-1);
	}

	@Override
	public Collection<Realm> getChildRealms() {
		// Not available without a provider
		if(provider==null) return new HashSet<Realm>();

		// Get all realms in the provider, whose parents include this realm
		Collection<Realm> realms = new HashSet<Realm>(provider.getAllRealms());
		realms.removeIf(realm -> !realm.getAllParentRealms().contains(this));

		return realms;
	}

	@Override
	public Collection<PlayerCharacter> getOnlineCharacters() {
		Collection<PlayerCharacter> characters = new HashSet<PlayerCharacter>();
		getOnlinePlayers().forEach(player -> characters.add(PlayerCharacter.getCharacter(player)));
		return characters;
	}

	@Override
	public Collection<Player> getOnlinePlayers() {
		// Iterate through online players and return ones who are members
		Collection<Player> players = new HashSet<Player>(Bukkit.getOnlinePlayers());
		players.removeIf(player -> PlayerCharacter.getCharacter(player).getRealm()!=this);
		return players;
	}

	@Override
	public void addPlayer(PlayerCharacter character) {
		character.getData(CorePlugin.plugin).set("realm", this.getIdentifier());
		character.saveData();
	}

	@Override
	public void addPlayer(Player player) {
		addPlayer(PlayerCharacter.getCharacter(player));
	}

	@Override
	public void removePlayer(PlayerCharacter character) {
		if(character.getRealm().equals(this)){
			character.getData(CorePlugin.plugin).set("realm", null);
			character.saveData();
		}
	}

	@Override
	public void removePlayer(Player player) {
		removePlayer(PlayerCharacter.getCharacter(player));
	}
}