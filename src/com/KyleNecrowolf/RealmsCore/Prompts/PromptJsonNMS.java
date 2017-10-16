package com.KyleNecrowolf.RealmsCore.Prompts;

import org.bukkit.entity.Player;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;

// Small utility class to send raw JSON messages to a client, when on a supported server version
final class PromptJsonNMS {

    // Send a raw JSON message to the client
    static void sendRawJson(Player player, String rawJsonMessage){
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a(rawJsonMessage));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}