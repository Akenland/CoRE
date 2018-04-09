package com.kylenanakdewa.core.common;

import org.bukkit.command.CommandSender;

import com.kylenanakdewa.core.CoreConfig;

public enum Error {

	NO_PERMISSION(String.format(CoreConfig.noPermMessage, CommonColors.ADMIN+CoreConfig.adminName+CommonColors.ERROR), true),
	INVALID_ARGS("Invalid arguments.", false),
	PLAYER_NOT_FOUND("Player not found.", false),
	REALM_NOT_FOUND("Realm not found.", false),
	WORLD_NOT_FOUND("World not found.", false),
	ITEM_NOT_FOUND("Item not found.", false),
	ENTITY_NOT_FOUND("Entity not found.", false);


	private String errorMessage;
	private boolean hideUsage;

	private Error(String errorMessage, boolean hideUsage){
		this.errorMessage = CommonColors.ERROR + errorMessage;
		this.hideUsage = hideUsage;
	}

	public boolean displayChat(CommandSender sender){
		sender.sendMessage(errorMessage);
		return hideUsage;
	}

	public boolean displayActionBar(CommandSender sender){
		Utils.sendActionBar(sender, errorMessage);
		return hideUsage;
	}
}
