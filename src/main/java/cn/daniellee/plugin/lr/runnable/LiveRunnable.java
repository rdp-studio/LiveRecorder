package cn.daniellee.plugin.lr.runnable;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.component.ActionSender;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class LiveRunnable extends BukkitRunnable {

    private static int recordedSeconds = 0;

    // 重置直播时间（切换至下一个玩家）
    public static void resetRecordedSeconds() {
        recordedSeconds = 0;
    }

    @Override
    public void run() {
        if (!LiveCore.living) return; // 关掉之后直接返回
        if (LiveCore.recorder == null) { // 如果直播员不在线
            // 检查直播员是否在线
            Player recorder = Bukkit.getPlayer(LiveRecorder.getInstance().getConfig().getString("setting.recorder-name", "Recorder"));
            if (recorder != null && recorder.isValid()) {
                // 赋予权限
                List<String> permissions = LiveRecorder.getInstance().getConfig().getStringList("setting.recorder-permission");
                if (permissions != null && !permissions.isEmpty()) {
                    String command = LiveRecorder.getInstance().getConfig().getString("setting.permission-command", "lp user {recorder} permission set {permission} true");
                    for (String permission : permissions) {
                        if (!recorder.hasPermission(permission)) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{recorder}", recorder.getName()).replace("{permission}", permission));
                        }
                    }
                }
                // 播报直播员上线
                if (LiveRecorder.getInstance().showCamera() && !LiveCore.otherRecording) {
                    Bukkit.broadcastMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.boardcast.online", "&eThe live recording started, all ready for the mirror~")).replace("&", "§"));
                    if (LiveRecorder.getInstance().isBungeecord()) {
                        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                        if (player != null) LiveCore.sendJoinMessage(player);
                    }
                }
                recorder.setAllowFlight(true);
                recorder.setFlying(true);
                recorder.setGameMode(GameMode.SPECTATOR);
                LiveCore.recorder = recorder;
            }
        } else { // 如果直播员在线
            LiveCore.otherRecording = false;
            if (LiveCore.recorder.isValid()) {
                // 显示在线信息
                AtomicInteger online = new AtomicInteger(Bukkit.getOnlinePlayers().size());
                if (LiveRecorder.getInstance().isBungeecord()) { // 添加其他服务器在线人数
                    LiveCore.otherOnline.values().forEach(online::addAndGet);
                }
                ActionSender.send(LiveCore.recorder, LiveRecorder.getInstance().getConfig().getString("message.action", "&bOnline: {online}").replace("{online}", Integer.toString(online.get())).replace("&", "§"));
            } else {
                if (LiveCore.recorder.isDead()) LiveCore.recorder.setHealth(20);
                if (!LiveCore.recorder.isValid()) {
                    LiveCore.recorder = null;
                    return;
                }
            }
        }
        // 每个玩家播出的时间
        int recordSeconds = LiveRecorder.getInstance().getConfig().getInt("setting.record-seconds", 15);
        // 当作不活跃的超时时间
        int inactivityTimeout = LiveRecorder.getInstance().getConfig().getInt("setting.inactivity-timeout", 5);
        // 清理不活跃的
        for (ActivePlayer activePlayer : LiveCore.activePlayers.values()) {
            Player player = Bukkit.getPlayer(activePlayer.getName());
            if (System.currentTimeMillis() - activePlayer.getLastActive() > inactivityTimeout * 1000 || (!activePlayer.isExternal() && (player == null || !player.isValid()))) {
                LiveCore.activePlayers.remove(activePlayer.getName());
            }
        }
        // 发送活跃玩家列表
        if (LiveRecorder.getInstance().isBungeecord()) {
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player != null) LiveCore.sendActivePlayerMessage(player);
        } else if (LiveCore.recorder == null) return;
        // 如果收到了目标玩家，直播目标玩家
        if (LiveCore.nextPlayer != null) {
            ActivePlayer activePlayer = LiveCore.activePlayers.get(LiveCore.nextPlayer);
            LiveCore.nextPlayer = null; // 置空
            LiveCore.recordPlayer(activePlayer);
            recordedSeconds = 0;
        } else if (LiveCore.recorder != null && recordedSeconds == 0 && !LiveCore.activePlayers.isEmpty()) { // 随机将一位玩家进行直播
            ActivePlayer activePlayer = (ActivePlayer) LiveCore.activePlayers.values().toArray()[new Random().nextInt(LiveCore.activePlayers.size())];
            LiveCore.recordPlayer(activePlayer);
        }
        recordedSeconds++;
        if (recordedSeconds > recordSeconds || LiveCore.recordingPlayer == null || Bukkit.getPlayer(LiveCore.recordingPlayer) == null || LiveCore.activePlayers.get(LiveCore.recordingPlayer) == null) recordedSeconds = 0;
    }

}
