package com.kylenanakdewa.core.realms.composite;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.realms.Realm;
import com.kylenanakdewa.core.realms.RealmProvider;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
			name = activeProviders.next().getRealm(identifier).getName();
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
			tagline = activeProviders.next().getRealm(identifier).getName();
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
			color = activeProviders.next().getRealm(identifier).getColor();
		}
		return color;
	}

	@Override
	public void setColor(ChatColor color) {
		compositeProvider.getActiveProviders().forEachRemaining(provider -> provider.getRealm(identifier).setColor(color));
	}

	@Override
	public Realm getParentRealm() {
		Realm realm = null;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && realm==null){
			realm = activeProviders.next().getRealm(identifier).getParentRealm();
		}
		return realm;
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
			realms = activeProviders.next().getRealm(identifier).getAllParentRealms();
		}
		return realms;
	}

	@Override
	public Realm getTopParentRealm() {
		Realm realm = null;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && realm==null){
			realm = activeProviders.next().getRealm(identifier).getTopParentRealm();
		}
		return realm;
	}

	@Override
	public Collection<Realm> getChildRealms() {
		Collection<Realm> realms = null;
		Iterator<RealmProvider> activeProviders = compositeProvider.getActiveProviders();
		while(activeProviders.hasNext() && realms==null){
			realms = activeProviders.next().getRealm(identifier).getChildRealms();
		}
		return realms;
	}

	@Override
	public Collection<PlayerCharacter> getOnlineCharacters() {
		// Only use the first provider
		return compositeProvider.getActiveProviders().next().getRealm(identifier).getOnlineCharacters();
	}

	@Override
	public Collection<Player> getOnlinePlayers() {
		// Only use the first provider
		return compositeProvider.getActiveProviders().next().getRealm(identifier).getOnlinePlayers();
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