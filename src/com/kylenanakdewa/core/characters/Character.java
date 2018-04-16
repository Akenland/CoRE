package com.kylenanakdewa.core.characters;

import com.kylenanakdewa.core.realms.RealmMember;

/**
 * Represents a Character, usually a player or NPC.
 * @author Kyle Nanakdewa
 */
public interface Character extends RealmMember {

	/**
	 * Gets the name of this Character.
	 * @return the name of this Character, which may include color formatting
	 */
	public String getName();
	/**
	 * Sets the name of this Character.
	 * @param name the new name for this Character, which may include color formatting
	 */
	public void setName(String name);

	/**
	 * Gets the title of this Character. The title traditionally appears in front of, or as a professional alternative to, the name.
	 * @return the title of this Character, which may include color formatting
	 */
	public String getTitle();
	/**
	 * Sets the title of this Character. The title traditionally appears in front of, or as a professional alternative to, the name.
	 * @param title the new title for this Character, which may include color formatting
	 */
	public void setTitle(String title);

	/**
	 * Gets the fully-formatted combined title and name of this Character, in its Realm color (unless overriden by color codes in the name or title).
	 * @return the title and name of this Character, which may include color formatting
	 */
	public String getFormattedName();

	/**
	 * Gets the chat format used for this Character.
	 * The first format parameter is the character's name ({@link #getName()}), and the second parameter is the chat message
	 * @see AsyncPlayerChatEvent#getFormat()
	 */
	public String getChatFormat();
}