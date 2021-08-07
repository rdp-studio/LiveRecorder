package cn.daniellee.plugin.lr.component;

import cn.daniellee.plugin.lr.LiveRecorder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

/**
 * 用于发送ActionBar消息
 */
public class LocationSender {

	public static void send(Player player, Location pre, Location current) {
		try {
			Class clazz = LiveRecorder.getNMSClass("PacketPlayOutEntity$PacketPlayOutEntityLook");
			long x = new BigDecimal(current.getX()).multiply(new BigDecimal(32)).subtract(new BigDecimal(pre.getX()).multiply(new BigDecimal(32))).multiply(new BigDecimal(128)).longValue();
			long y = new BigDecimal(current.getY()).multiply(new BigDecimal(32)).subtract(new BigDecimal(pre.getY()).multiply(new BigDecimal(32))).multiply(new BigDecimal(128)).longValue();
			long z = new BigDecimal(current.getZ()).multiply(new BigDecimal(32)).subtract(new BigDecimal(pre.getZ()).multiply(new BigDecimal(32))).multiply(new BigDecimal(128)).longValue();

			byte yaw = (byte) (current.getYaw() * 256.0F / 360.0F);
			byte pitch = (byte) (current.getPitch() * 256.0F / 360.0F);

			Bukkit.broadcastMessage(x + "|" + y + "|" + z + "|" + yaw + "|" + pitch);
			Object packet = clazz.getConstructor(new Class[] { int.class, byte.class, byte.class, boolean.class }).newInstance(new Object[] { player.getEntityId(), yaw, pitch, false });
//
//			Object packet = clazz.getConstructor(new Class[] { int.class, long.class, long.class, long.class, byte.class, byte.class, boolean.class }).newInstance(new Object[] { player.getEntityId(), x, y, z, yaw, pitch, false });

//			Field x = clazz.getDeclaredField("x");
//			x.setAccessible(true);
//			x.setDouble(packet, location.getX());
//			Field y = clazz.getDeclaredField("y");
//			y.setAccessible(true);
//			y.setDouble(packet, location.getY() + 10);
//			Field z = clazz.getDeclaredField("z");
//			z.setAccessible(true);
//			z.setDouble(packet, location.getZ());
//			Field yaw = clazz.getDeclaredField("yaw");
//			yaw.setAccessible(true);
//			yaw.setFloat(packet, location.getYaw());
//			Field pitch = clazz.getDeclaredField("pitch");
//			pitch.setAccessible(true);
//			pitch.setFloat(packet, location.getPitch());
//			Field f = clazz.getDeclaredField("f");
//			f.setAccessible(true);
//			f.setBoolean(packet, true);
//
//			Field hasPos = clazz.getDeclaredField("hasPos");
//			hasPos.setAccessible(true);
//			hasPos.setBoolean(packet, true);
//
//			Field hasLook = clazz.getDeclaredField("hasLook");
//			hasLook.setAccessible(true);
//			hasLook.setBoolean(packet, true);
//
//			Bukkit.broadcastMessage(packet.toString()  + "------" + x.get(packet));
//
//			Class clazz = LiveRecorder.getNMSClass("PacketPlayOutPosition");
//			Object packet = clazz.getConstructor(new Class[] { double.class, double.class, double.class, float.class, float.class, Set.class, int.class });

			Object getHandle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = getHandle.getClass().getField("playerConnection").get(getHandle);

			playerConnection.getClass().getMethod("sendPacket", LiveRecorder.getNMSClass("Packet")).invoke(playerConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
