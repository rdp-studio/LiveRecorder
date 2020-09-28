package cn.daniellee.plugin.lr.runnable;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.component.ActionSender;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class LiveRunnable extends BukkitRunnable {

    private static int recordedSeconds = 0;

    public static void resetRecordedSeconds() {
        recordedSeconds = 0;
    }

    @Override
    public void run() {
        if (LiveCore.recorder == null) {
            Player recorder = Bukkit.getPlayer(LiveRecorder.getInstance().getConfig().getString("setting.recorder-name", "Recorder"));
            if (recorder != null && recorder.isValid()) {
                List<String> permissions = LiveRecorder.getInstance().getConfig().getStringList("setting.recorder-permission");
                if (permissions != null && !permissions.isEmpty()) {
                    String command = LiveRecorder.getInstance().getConfig().getString("setting.permission-command", "lp user {recorder} permission set {permission} true");
                    for (String permission : permissions) {
                        if (!recorder.hasPermission(permission)) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{recorder}", recorder.getName()).replace("{permission}", permission));
                        }
                    }
                }
                Bukkit.broadcastMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.boardcast.online", "&eThe live recording started, all ready for the mirror~")).replace("&", "§"));
                recorder.setAllowFlight(true);
                recorder.setFlying(true);
                recorder.setGameMode(GameMode.SPECTATOR);
                LiveCore.recorder = recorder;
            }
        } else {
            if (LiveCore.recorder.isValid()) {
                ActionSender.send(LiveCore.recorder, LiveRecorder.getInstance().getConfig().getString("message.action", "&bOnline: {online}").replace("{online}", Integer.toString(Bukkit.getOnlinePlayers().size())).replace("&", "§"));
            } else {
                if (LiveCore.recorder.isDead()) LiveCore.recorder.setHealth(20);
                if (!LiveCore.recorder.isValid()) LiveCore.recorder = null;
            }
        }
        // 每个玩家播出的时间
        int recordSeconds = LiveRecorder.getInstance().getConfig().getInt("setting.record-seconds", 15);
        // 当作不活跃的超时时间
        int inactivityTimeout = LiveRecorder.getInstance().getConfig().getInt("setting.inactivity-timeout", 5);
        synchronized (LiveCore.activePlayers) {
            // 清理不活跃的
            for (int i = 0; i < LiveCore.activePlayers.size();) {
                ActivePlayer activePlayer = LiveCore.activePlayers.get(i);
                if (System.currentTimeMillis() - activePlayer.getLastActive() > inactivityTimeout * 1000) {
                   LiveCore.activePlayers.remove(i);
                } else i++;
            }
            // 随机将一位玩家进行直播
            if (recordedSeconds == 0 && LiveCore.recorder != null && !LiveCore.activePlayers.isEmpty()) {
                ActivePlayer activePlayer;
                Player player;
                // 循环寻找下一个直播对象
                do {
                    activePlayer = LiveCore.activePlayers.get(new Random().nextInt(LiveCore.activePlayers.size()));
                    player = Bukkit.getPlayer(activePlayer.getName());
                } while (player == null || !player.isValid());
                // 计算镜头位置
                player.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.recorder-come", "&eCongratulations on your appearance, let's take a look at the camera in front of the camera~")).replace("&", "§"));
                LiveCore.recorder.teleport(LiveCore.getLiveLocation(player.getLocation()));
                LiveCore.recordingPlayer = activePlayer.getName();
            }
            recordedSeconds++;
            if (recordedSeconds > recordSeconds || LiveCore.recordingPlayer == null || Bukkit.getPlayer(LiveCore.recordingPlayer) == null) recordedSeconds = 0;
        }
    }

}
