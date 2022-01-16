package cn.daniellee.plugin.lr.listener;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import cn.daniellee.plugin.lr.model.PlayerData;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.math.BigDecimal;

public class PlayerListener implements Listener {

	private long lastUpdateActive = 0;

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		// 如果是Recorder直接跳过
		if (LiveCore.recorder != null && player.getName().equals(LiveCore.recorder.getName())) return;
		ActivePlayer activePlayer = LiveCore.activePlayers.get(player.getName());
		// 如果不是第一人称且此正在被录制则移动镜头
		if (!LiveRecorder.getInstance().isFirstPerspective() && player.getName().equals(LiveCore.recordingPlayer)) {
			if (LiveCore.recorder != null && e.getTo() != null) {
				// 位置追踪
				if (LiveCore.recorder.canSee(player)) {
					LiveCore.recorder.setVelocity(LiveCore.getVectorByFormTo(e.getFrom(), e.getTo()));
					// 显示镜头位置粒子
					if (LiveRecorder.getInstance().showCamera()) {
						LiveCore.recorder.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, LiveCore.recorder.getLocation(), 1);
					}
				} else {
					LiveCore.recorder.teleport(LiveCore.getLiveLocation(e.getTo()));
					LiveCore.recorder.setGameMode(GameMode.SPECTATOR);
				}
				// 镜头修正
				if (activePlayer != null) {
					if (activePlayer.getBeginLocation() == null) activePlayer.setBeginLocation(e.getFrom());
					double distance = Math.sqrt(new BigDecimal(activePlayer.getBeginLocation().getX()).subtract(new BigDecimal(e.getTo().getX())).pow(2).add(new BigDecimal(activePlayer.getBeginLocation().getZ()).subtract(new BigDecimal(e.getTo().getZ())).pow(2)).add(new BigDecimal(activePlayer.getBeginLocation().getY()).subtract(new BigDecimal(e.getTo().getY())).pow(2)).doubleValue());
					if (distance > LiveRecorder.getInstance().getConfig().getInt("setting.camera-reset-distance", 50)) {
						activePlayer.setBeginLocation(e.getTo());
						LiveCore.recorder.teleport(LiveCore.getLiveLocation(e.getTo()));
						LiveCore.recorder.setGameMode(GameMode.SPECTATOR);
					}
				}
			}
		}
		// 每半秒更新一次，避免过度耗费性能
		long now = System.currentTimeMillis();
		if (!LiveCore.living || now - lastUpdateActive < 500) return;
		lastUpdateActive = now;
		// 更新活跃玩家列表
		if (activePlayer != null) {
			activePlayer.setLastActive(now);
			if (LiveRecorder.getInstance().isBungeecord()) {
				activePlayer.setExternal(false);
				activePlayer.setServer(LiveCore.serverName);
			}
		} else {
			PlayerData playerData = LiveRecorder.getInstance().getStorage().getPlayerDataByName(player.getName());
			if (playerData != null && playerData.isDenied()) return; // 如果玩家拒绝被直播
			activePlayer = new ActivePlayer(player.getName(), now);
			if (LiveRecorder.getInstance().isBungeecord()) {
				activePlayer.setExternal(false);
				activePlayer.setServer(LiveCore.serverName);
			}
			LiveCore.activePlayers.put(player.getName(), activePlayer);
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer) && e.getTo() != null && LiveCore.recorder != null) {
			ActivePlayer activePlayer = LiveCore.activePlayers.get(e.getPlayer().getName());
			if (activePlayer != null) activePlayer.setBeginLocation(e.getTo());
			LiveCore.recorder.teleport(LiveCore.getLiveLocation(e.getTo()));
			LiveCore.recorder.setGameMode(GameMode.SPECTATOR);
			if (LiveRecorder.getInstance().isFirstPerspective()) LiveCore.recorder.setSpectatorTarget(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer)) {
			LiveRunnable.resetRecordedSeconds();
		} else if (LiveCore.recorder != null && e.getPlayer().getName().equals(LiveCore.recorder.getName())) {
			if (LiveRecorder.getInstance().showCamera() && !LiveCore.goingOther) {
				Bukkit.broadcastMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.boardcast.offline", "&eThe live recording is over, thanks to the support of the friends~")).replace("&", "§"));
			}
			if (LiveRecorder.getInstance().isBungeecord() && !LiveCore.goingOther) {
				Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
				if (player != null) LiveCore.sendLeaveMessage(player);
			}
			LiveCore.recorder = null;
			LiveCore.goingOther = false;
		}
		LiveCore.activePlayers.remove(e.getPlayer().getName()); // 移出活跃玩家列表
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity().getName().equals(LiveCore.recordingPlayer)) {
			LiveRunnable.resetRecordedSeconds();
		} else if (LiveCore.recorder != null && e.getEntity().getName().equals(LiveCore.recorder.getName())) {
			LiveCore.recorder.setHealth(20);
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer) && LiveCore.recorder != null && LiveRecorder.getInstance().getConfig().getBoolean("setting.show-inventory", false)) {
			LiveCore.recorder.openInventory(e.getInventory());
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer) && LiveCore.recorder != null && LiveRecorder.getInstance().getConfig().getBoolean("setting.show-inventory", false)) {
			LiveCore.recorder.closeInventory();
		}
	}

}
