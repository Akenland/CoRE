package com.kylenanakdewa.core.common.prompts;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Fired when a player triggers a prompt action.
 * @author Kyle Nanakdewa
 */
public final class PromptActionEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();

	/** The type of action. Each plugin should use a unique action type. */
	private String actionType;
	/** The content of this action. */
	private String actionContent;


	public PromptActionEvent(Player player, String action){
		super(player);

		// Split the action into its type and its content
		String[] splitAction = action.split("_", 2);
		actionType = splitAction[0];
		actionContent = splitAction[1];
	}


	@Override
	public HandlerList getHandlers(){
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
	}


	/**
	 * Checks if the type of this action matches the specified string.
	 * Use this to identify if your plugin should respond to this event.
	 * @param actionType the action type (this should usually be unique to your plugin)
	 * @return true if this event's action is of the specified type
	 */
	public boolean isType(String actionType){
		return this.actionType.equalsIgnoreCase(actionType);
	}

	/**
	 * Gets the content of this action.
	 * @return the content of the action string
	 */
	public String getAction(){
		return actionContent;
	}

}