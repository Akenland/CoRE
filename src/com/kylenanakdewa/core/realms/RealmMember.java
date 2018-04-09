package com.kylenanakdewa.core.realms;

/**
 * Represents a member of a {@link Realm}.
 * @author Kyle Nanakdewa
 */
public interface RealmMember {

	/**
	 * Gets the {@link Realm} that this object is a member of.
	 * @return their Realm, or null if they are not a member of a Realm
	 */
	public Realm getRealm();

	/**
	 * Sets the {@link Realm} that this object is a member of.
	 * @param realm the new Realm to assign
	 */
	public void setRealm(Realm realm);

	/**
	 * Gets if this object is an officer in their Realm.
	 * @return true if they're an officer of their Realm
	 */
	public boolean isRealmOfficer();

}