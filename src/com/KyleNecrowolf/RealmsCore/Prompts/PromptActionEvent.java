package com.KyleNecrowolf.RealmsCore.Prompts;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PromptActionEvent extends Event {    
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private String actionType;
    private String actionContent;

    //// Constructor
    PromptActionEvent(Player player, String[] action){
        this.player = player;
        
        // Split the action into its type and its content
	    actionType = action[0];
	    actionContent = action[1];
    }

    //// Bukkit Event Handler List
    public HandlerList getHandlers(){
        return handlers;
    }
    public static HandlerList getHandlerList(){
        return handlers;
    }


    //// Getters
    // Check if action type matches a given string - use this to identify if your plugin should respond to this event
    public boolean isType(String actionType){
        return this.actionType.equalsIgnoreCase(actionType);
    }

    // Get the content of the action
    public String getAction(){
        return actionContent;
    }

    // Get the player that selected this action
    public Player getPlayer(){
        return player;
    }
}