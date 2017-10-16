package com.KyleNecrowolf.RealmsCore.Common;

import org.bukkit.command.CommandSender;

import com.KyleNecrowolf.RealmsCore.ConfigValues;

public enum Error {

	//NO_PERMISSION("You can't use this command! Ask an "+ConfigValues.adminColor+ConfigValues.adminName+Utils.errorText+" for help.", true),
	NO_PERMISSION(String.format(ConfigValues.noPermMessage, ConfigValues.adminColor+ConfigValues.adminName+Utils.errorText), true),
	INVALID_ARGS("Invalid arguments.", false),
	PLAYER_NOT_FOUND("Player not found.", false),
	REALM_NOT_FOUND("Realm not found.", false),
	WORLD_NOT_FOUND("World not found.", false),
	ITEM_NOT_FOUND("Item not found.", false),
	ENTITY_NOT_FOUND("Entity not found.", false);


	String errorMessage;
	boolean hideUsage;

	Error(String errorMessage, boolean hideUsage){
		this.errorMessage = Utils.errorText + errorMessage;
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
