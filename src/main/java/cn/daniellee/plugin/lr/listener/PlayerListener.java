package cn.daniellee.plugin.lr.listener;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent e) {
		String recorderName = LiveRecorder.getInstance().getConfig().getString("setting.recorder-name", "Recorder");
		if (recorderName.equals(e.getPlayer().getName())) {
			e.allow();
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player != LiveCore.recorder) {
	        // 更新活跃玩家列表
	        ActivePlayer activePlayer = LiveCore.getActivePlayerByName(player.getName());
	        if (activePlayer != null) {
		        activePlayer.setLastActive(System.currentTimeMillis());
	        } else {
		        activePlayer = new ActivePlayer(player.getName(), System.currentTimeMillis());
		        LiveCore.activePlayers.add(activePlayer);
	        }
        }
        // 如果正在被录制则移动镜头
        if (player.getName().equals(LiveCore.recordingPlayer)) {
	        if (LiveCore.recorder != null && e.getTo() != null) {
	        	if (LiveCore.recorder.canSee(player)) {
			        LiveCore.recorder.setVelocity(LiveCore.getVectorByFormTo(e.getFrom(), e.getTo()));
			        LiveCore.recorder.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, LiveCore.recorder.getLocation(), 0);
		        } else {
			        LiveRunnable.resetRecordedSeconds();
		        }
	        }
        }
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer) && e.getTo() != null) {
			LiveCore.recorder.teleport(LiveCore.getLiveLovation(e.getTo()));
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getName().equals(LiveCore.recordingPlayer)) {
			LiveRunnable.resetRecordedSeconds();
		} else if (e.getPlayer() == LiveCore.recorder) {
			Bukkit.broadcastMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.boardcast.offline", "&eThe live recording is over, thanks to the support of the friends~")).replace("&", "§"));
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity().getName().equals(LiveCore.recordingPlayer)) {
			LiveRunnable.resetRecordedSeconds();
		}
	}

}
