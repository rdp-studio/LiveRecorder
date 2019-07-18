package cn.daniellee.plugin.lr.listener;

import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        // 更新活跃玩家列表
        ActivePlayer activePlayer = LiveCore.getActivePlayerByName(player.getName());
        if (activePlayer != null) {
            activePlayer.setLastActive(System.currentTimeMillis());
        } else {
            activePlayer = new ActivePlayer(player.getName(), System.currentTimeMillis());
            LiveCore.activePlayers.add(activePlayer);
        }
        // 如果正在被录制则移动镜头
		if (player.getName().equals(LiveCore.recordingPlayer)) {
            if (LiveCore.recorder != null && e.getTo() != null) {
                LiveCore.recorder.setVelocity(LiveCore.getVectorByFormTo(e.getFrom(), e.getTo()));
            }
        }
	}

}
