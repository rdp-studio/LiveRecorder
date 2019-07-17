package cn.daniellee.plugin.lr.listener;

import cn.daniellee.plugin.lr.LiveRecorder;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
//		Location from = e.getFrom();
		Location to = e.getTo().clone();
//		double x = new BigDecimal(e.getTo().getX()).subtract(new BigDecimal(e.getFrom().getX())).doubleValue();
//		double y = new BigDecimal(e.getTo().getY()).subtract(new BigDecimal(e.getFrom().getY())).doubleValue();
//		double z = new BigDecimal(e.getTo().getZ()).subtract(new BigDecimal(e.getFrom().getZ())).doubleValue();
//		Vector vector = new Vector(x, y, z);
		Player recorder = Bukkit.getPlayer("Recorder");
		to.setY(to.getY() + 2);

		try {
			PacketContainer container = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
			Bukkit.broadcastMessage(container.toString());
//			container.getDoubles().write(1, to.getX());
//			container.getDoubles().write(2, to.getY());
//			container.getDoubles().write(3, to.getZ());
//			container.getBytes().write(0, (byte) to.getYaw());
//			container.getBytes().write(1, (byte) to.getPitch());
			LiveRecorder.getInstance().getProtocolManager().sendServerPacket(recorder, container);
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}

//		Bukkit.broadcastMessage(e.getTo().subtract(e.getFrom()).toString());
//		recorder.getLocation().setDirection(e.getPlayer().getLocation().getDirection()).toVector()
//		recorder.setVelocity();
	}

}
