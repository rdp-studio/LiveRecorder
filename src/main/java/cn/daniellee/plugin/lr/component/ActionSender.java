package cn.daniellee.plugin.lr.component;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/**
 * 用于发送ActionBar消息
 */
public class ActionSender {

	public static void send(Player player, String message) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
//		try {
//			Object chatComponentText = LiveRecorder.getNMSClass("ChatComponentText").getConstructor(new Class[] { String.class }).newInstance(message);
//			Object chatMessageType = LiveRecorder.getNMSClass("ChatMessageType").getField("GAME_INFO").get(null);
//			Object packetPlayOutChat = LiveRecorder.getNMSClass("PacketPlayOutChat").getConstructor(new Class[] { LiveRecorder.getNMSClass("IChatBaseComponent"), LiveRecorder.getNMSClass("ChatMessageType") }).newInstance(new Object[] { chatComponentText, chatMessageType });
//			Object getHandle = player.getClass().getMethod("getHandle").invoke(player);
//			Object playerConnection = getHandle.getClass().getField("playerConnection").get(getHandle);
//
//			playerConnection.getClass().getMethod("sendPacket", LiveRecorder.getNMSClass("Packet")).invoke(playerConnection, packetPlayOutChat);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
