package com.kylenanakdewa.core.common;

import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Common NMS methods. Will only maintain compatibility with latest Minecraft version.
 * @author Kyle Nanakdewa
 */
final class CommonNMS {
	/**
	 * Sends an action bar message to a player.
	 */
	static void sendActionBar(Player player, String message){
		IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
		PacketPlayOutChat bar = new PacketPlayOutChat(icbc, ChatMessageType.GAME_INFO);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(bar);
	}

	/**
	 * Sends a raw JSON message to a player.
	 */
	static void sendRawJson(Player player, String message){
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a(message));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}