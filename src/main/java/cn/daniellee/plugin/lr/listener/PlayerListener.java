package cn.daniellee.plugin.lr.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		e.getPlayer().setVelocity(e.getPlayer().getVelocity().setY(2D));
	}

}
