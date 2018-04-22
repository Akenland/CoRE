package com.kylenanakdewa.core.common;

import org.bukkit.ChatColor;

import com.kylenanakdewa.core.CoreConfig;

/**
 * Common colors for multiple components and plugins.
 * @author Kyle Nanakdewa
 */
public enum CommonColors {

	/** The accent color for Project CoRE. */
	ACCENT (ChatColor.DARK_GREEN),

	/** The color for standard messages. */
	MESSAGE (CoreConfig.messageColor),

	/** The color for non-important information messages. */
	INFO (CoreConfig.infoColor),

	/** The color for error messages. */
	ERROR (CoreConfig.errorColor),

	/** The color to identify server admins. */
	ADMIN (CoreConfig.adminColor);


	private ChatColor color;

	private CommonColors(ChatColor color){
		this.color = color;
	}

	/**
	 * Gets the implementation-appropriate color formatting code for this color.
	 */
	@Override
	public String toString(){
		return color.toString();
	}

	/** Gets the ChatColor for this color. */
	public ChatColor getColor(){
		return color;
	}
}