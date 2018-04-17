package com.kylenanakdewa.core.realms.composite;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.realms.Realm;
import com.kylenanakdewa.core.realms.RealmProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * A Realm that retrieves its data from multiple Realms with the same identifier, across multiple RealmProviders.
 * <p>
 * The active providers within this composite provider are used to retrieve Realms. Data from multiple providers is summed.
 * In the event of a conflict, the first active provider will have priority.
 * @author Kyle Nanakdewa
 */
public class CompositeRealm implements Realm {

	/** The CompositeRealmProvider that is providing this CompositeRealm. */
	private CompositeRealmProvider compositeProvider;

	/** The unique identifier of this Realm. */
	private String identifier;

	/**
	 * Creates a CompositeRealm for the specified provider, with the specified identifier.
	 */
	CompositeRealm(CompositeRealmProvider compositeProvider, String identifier){
		this.compositeProvider = compositeProvider;
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getName() {
		String name = identifier;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && name.equals(identifier)){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) name = realm.getName();
		}
		return name;
	}

	@Override
	public void setName(String name){
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).setName(name));
	}

	@Override
	public String getTagline() {
		String tagline = null;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && tagline==null){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) tagline = realm.getTagline();
		}
		return tagline;
	}

	@Override
	public void setTagline(String tagline) {
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).setTagline(tagline));
	}

	@Override
	public ChatColor getColor() {
		ChatColor color = null;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && color==null){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) color = realm.getColor();
		}
		return color;
	}

	@Override
	public void setColor(ChatColor color) {
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).setColor(color));
	}

	@Override
	public Realm getParentRealm() {
		Realm parentRealm = null;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && parentRealm==null){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) parentRealm = realm.getParentRealm();
		}
		return parentRealm;
	}

	@Override
	public void setParentRealm(Realm realm) {
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).setParentRealm(realm));
	}

	@Override
	public List<Realm> getAllParentRealms() {
		List<Realm> realms = null;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && realms==null){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) realms = realm.getAllParentRealms();
		}
		return realms;
	}

	@Override
	public Realm getTopParentRealm() {
		Realm parentRealm = null;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && parentRealm==null){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) parentRealm = realm.getTopParentRealm();
		}
		return parentRealm;
	}

	@Override
	public Collection<Realm> getChildRealms() {
		Collection<Realm> realms = new HashSet<Realm>();
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext()){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) realms.addAll(realm.getChildRealms());
		}
		return realms;
	}

	@Override
	public Collection<PlayerCharacter> getOnlineCharacters() {
		Set<PlayerCharacter> allPlayers = new HashSet<PlayerCharacter>();
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext()){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) allPlayers.addAll(realm.getOnlineCharacters());
		}
		return allPlayers;
	}

	@Override
	public Collection<Player> getOnlinePlayers() {
		Set<Player> allPlayers = new HashSet<Player>();
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext()){
			Realm realm = activeProviders.next().getRealm(identifier);
			if(realm!=null) allPlayers.addAll(realm.getOnlinePlayers());
		}
		return allPlayers;
	}

	@Override
	public void addPlayer(PlayerCharacter character) {
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).addPlayer(character));
	}

	@Override
	public void addPlayer(Player player) {
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).addPlayer(player));
	}

	@Override
	public void removePlayer(PlayerCharacter character) {
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).removePlayer(character));
	}

	@Override
	public void removePlayer(Player player) {
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).removePlayer(player));
	}


}