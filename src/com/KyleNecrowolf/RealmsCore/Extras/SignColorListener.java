package com.KyleNecrowolf.RealmsCore.Extras;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public final class SignColorListener implements Listener {

    @EventHandler
    public void onSignPlace(SignChangeEvent event){
        for(int i=0; i<4; i++) event.setLine(i, ChatColor.translateAlternateColorCodes('&', event.getLine(i)));
    }

}