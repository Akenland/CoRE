package com.kylenanakdewa.core.realms.composite;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.realms.Realm;
import com.kylenanakdewa.core.realms.RealmProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A RealmProvider that allows the use of multiple other RealmProviders on a single server.
 * <p>
 * This provider will not provide any Realms on its own; it requires other RealmProviders to be registered with it.
 * <p>
 * The active providers within this composite provider are used to retrieve Realms. Data from multiple providers is summed.
 * In the event of a conflict, the first active provider will have priority.
 * @author Kyle Nanakdewa
 */
public class CompositeRealmProvider implements RealmProvider {

	/** A collection of all RealmProviders registered on the server. */
	private static Map<String,RealmProvider> registeredProviders = new HashMap<String,RealmProvider>();


	/** A list of RealmProviders currently active in this composite provider. */
	private List<RealmProvider> activeProviders;

	/** The Realms available in this composite provider. */
	private Map<String,CompositeRealm> realms;


	/**
	 * Registers a RealmProvider for use with a CompositeRealmProvider. <p>
	 * Each registered provider must have a unique name to identify it. The format pluginname.providername is recommended.
	 * If there is already a provider registered with the specified name, the new provider will not be registered.
	 * @param name a unique name to identify this RealmProvider
	 * @param provider the RealmProvider to register
	 */
	public static void registerProvider(String name, RealmProvider provider){
		registeredProviders.putIfAbsent(name, provider);
	}
	/**
	 * Unregisters all RealmProviders.
	 */
	public static void unregisterAllProviders(){
		registeredProviders.clear();
	}


	/**
	 * Creates a new CompositeRealmProvider, with no active providers.
	 */
	public CompositeRealmProvider(){
		activeProviders = new ArrayList<RealmProvider>();
		realms = new HashMap<String,CompositeRealm>();
	}

	/**
	 * Activates a provider in this composite provider.
	 * The provider must first be registered with {@link #registerProvider(String, RealmProvider)}.
	 * @param providerName the name of the provider to activate
	 * @throws IllegalArgumentException if the specified provider is not registered on this server
	 */
	public void activateProvider(String providerName){
		RealmProvider provider = registeredProviders.get(providerName);
		if(provider==null) throw new IllegalArgumentException(providerName+" is not a registered provider");
		else activeProviders.add(provider);
	}

	/**
	 * Gets an iterator over active providers in this composite provider.
	 * @return an iterator over the the active providers, from highest to lowest priority
	 */
	public Iterator<RealmProvider> getActiveProviders(){
		return activeProviders.iterator();
	}

	/**
	 * Updates the Realms available in this composite provider.
	 */
	private void updateRealms(){
		// Remove old realms
		realms.values().removeIf(realm -> !hasRealm(realm));

		// Add new realms
		getActiveProviders().forEachRemaining(provider -> {
			for(Realm realm : provider.getAllRealms()){
				realms.putIfAbsent(realm.getIdentifier(), new CompositeRealm(this, realm.getIdentifier()));
			}
		});
	}

	/**
	 * Gets the Realm from a specific provider.
	 * @param provider the RealmProvider to retrieve from
	 * @param realm the Realm to retrieve
	 * @return the appropriate Realm from the provider
	 * @throws IllegalArgumentException if the provider or realm cannot be found in this composite provider
	 */
	private Realm getRealm(RealmProvider provider, CompositeRealm realm){
		if(!activeProviders.contains(provider)) throw new IllegalArgumentException(provider+" is not an active provider");
		if(!realms.containsValue(realm)) throw new IllegalArgumentException(realm.getIdentifier()+" is not a realm in this composite provider");

		return provider.getRealm(realm.getIdentifier());
	}

	/**
	 * Checks if the specified Realm exists in this composite provider.
	 * More specifically, checks if the Realm exists in at least one active provider in this composite provider.
	 * @param realm the Realm to check
	 * @return true if the Realm still exists in at least one active provider, otherwise false
	 */
	private boolean hasRealm(Realm realm){
		Iterator<RealmProvider> activeProviders = getActiveProviders();
		while(activeProviders.hasNext()){
			// If any provider has this realm, return true
			if(activeProviders.next().getRealm(realm.getIdentifier())!=null) return true;
		}
		// Otherwise, return false
		return false;
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
		Realm realm = null;
		Iterator<RealmProvider> activeProviders = getActiveProviders();
		while(activeProviders.hasNext() && realm==null){
			realm = activeProviders.next().getCharacterRealm(character);
		}
		return realm;
	}

	@Override
	public boolean isOfficer(Character character) {
		updateRealms();
		// Return true if the character is an officer of any realms
		Iterator<RealmProvider> activeProviders = getActiveProviders();
		while(activeProviders.hasNext()){
			if(activeProviders.next().isOfficer(character)) return true;
		}
		return false;
	}

	@Override
	public void setOfficer(Character character, boolean isOfficer) {
		updateRealms();
		// Apply to all providers
		getActiveProviders().forEachRemaining(provider -> provider.setOfficer(character, isOfficer));
	}

}