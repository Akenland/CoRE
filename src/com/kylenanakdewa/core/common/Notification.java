package com.kylenanakdewa.core.common;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

/**
 * A Notification that can be sent to a CommandSender, alerting them of something, or providing information.
 * @author Kyle Nanakdewa
 */
public class Notification {

    /**
     * Notifies all players on the server.
     */
    public static Notification all(String message){
        return null;
    }


    /** The type (category/importance) of this Notification. */
    private final NotificationType type;
    /** The content of this Notification. */
    private String content;
    /** The sound played by this Notification. */
    private Sound sound;

    private Notification(NotificationType type){
        this.type = type;


    }

    /**
     * Notifies the specified CommandSenders.
     * @param targets the CommandSenders who should receive this Notification
     */
    public void send(CommandSender[] targets){
        for(CommandSender target : targets){
            target.sendMessage(content);
        }
    }


    public enum NotificationType {
        /** A non-important status message. */
        INFO (CommonColors.INFO.getColor(), null, null),
        /** A semi-important response message. */
        MESSAGE (CommonColors.MESSAGE.getColor(), null, null),
        /** An error message. */
        ERROR (CommonColors.ERROR.getColor(), "An error has occurred", Sound.BLOCK_NOTE_BASS);

        ChatColor color;
        private NotificationType(ChatColor color, String defaultMessage, Sound sound){
            this.color = color;
        }
    }
}