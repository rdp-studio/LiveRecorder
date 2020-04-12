package cn.daniellee.plugin.lr.listener;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
		// 如果正在被录制则移动镜头
		if (player.getName().equals(LiveCore.recordingPlayer)) {
			if (LiveCore.recorder != null && e.getTo() != null) {
				if (LiveCore.recorder.canSee(player)) {
					LiveCore.recorder.setVelocity(LiveCore.getVectorByFormTo(e.getFrom(), e.getTo()));
					// 显示镜头位置粒子
					if (!LiveRecorder.getInstance().isHideCamera()) {
						LiveCore.recorder.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, LiveCore.recorder.getLocation(), 1);
					}
				} else {
					LiveRunnable.resetRecordedSeconds();
				}
			}
		}
		// 如果是Recorder直接跳过
        if (LiveCore.recorder != null && player.getName().equals(LiveCore.recorder.getName())) return;
        // 更新活跃玩家列表
        ActivePlayer activePlayer = LiveCore.getActivePlayerByName(player.getName());
        if (activePlayer != null) {
	        activePlayer.setLastActive(System.currentTimeMillis());
        } else {
	        activePlayer = new ActivePlayer(player.getName(), System.currentTimeMillis());
	        LiveCore.activePlayers.add(activePlayer);
        }
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer) && e.getTo() != null && LiveCore.recorder != null) {
			LiveCore.recorder.teleport(LiveCore.getLiveLocation(e.getTo()));
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer)) {
			LiveRunnable.resetRecordedSeconds();
		} else if (LiveCore.recorder != null && e.getPlayer().getName().equals(LiveCore.recorder.getName())) {
			Bukkit.broadcastMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.boardcast.offline", "&eThe live recording is over, thanks to the support of the friends~")).replace("&", "§"));
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity().getName().equals(LiveCore.recordingPlayer)) {
			LiveRunnable.resetRecordedSeconds();
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer) && LiveCore.recorder != null) {
			LiveCore.recorder.openInventory(e.getInventory());
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer) && LiveCore.recorder != null) {
			LiveCore.recorder.closeInventory();
		}
	}

}
