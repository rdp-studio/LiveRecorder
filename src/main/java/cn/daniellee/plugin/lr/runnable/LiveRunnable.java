package cn.daniellee.plugin.lr.runnable;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.component.ActionSender;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class LiveRunnable extends BukkitRunnable {

    private static int recordedSeconds = 0;

    public static void resetRecordedSeconds() {
        recordedSeconds = 0;
    }

    @Override
    public void run() {
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
                if (LiveRecorder.getInstance().showCamera()) Bukkit.broadcastMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.boardcast.online", "&eThe live recording started, all ready for the mirror~")).replace("&", "§"));
                recorder.setAllowFlight(true);
                recorder.setFlying(true);
                recorder.setGameMode(GameMode.SPECTATOR);
                LiveCore.recorder = recorder;
            } else return; // 直播员不在线直接结束
        } else { // 如果直播员在线
            if (LiveCore.recorder.isValid()) {
                // 显示在线信息
                ActionSender.send(LiveCore.recorder, LiveRecorder.getInstance().getConfig().getString("message.action", "&bOnline: {online}").replace("{online}", Integer.toString(Bukkit.getOnlinePlayers().size())).replace("&", "§"));
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
            if (player == null || !player.isValid() || System.currentTimeMillis() - activePlayer.getLastActive() > inactivityTimeout * 1000) {
                LiveCore.activePlayers.remove(activePlayer.getName());
            }
        }
        // 发送活跃玩家列表
        if (LiveRecorder.getInstance().isBungeecord()) LiveCore.sendActivePlayerMessage(LiveCore.recorder);
        // 如果收到了目标玩家，直播目标玩家
        if (LiveCore.nextPlayer != null) {
            Player player = Bukkit.getPlayer(LiveCore.nextPlayer);
            ActivePlayer activePlayer = LiveCore.activePlayers.get(LiveCore.nextPlayer);
            LiveCore.nextPlayer = null; // 置空
            if (player == null || !player.isValid() || activePlayer == null || activePlayer.isExternal()) return; // 玩家是假的就结束
            // 记录初始位置
            activePlayer.setBeginLocation(player.getLocation());
            LiveCore.recorder.teleport(LiveCore.getLiveLocation(player.getLocation()));
            LiveCore.recordingPlayer = player.getName();
            // 如果不是上个玩家而且没有隐藏摄像头
            if (!player.getName().equals(LiveCore.lastPlayer) && LiveRecorder.getInstance().showCamera()) player.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.recorder-come", "&eCongratulations on your appearance, let's take a look at the camera in front of the camera~")).replace("&", "§"));
            LiveCore.lastPlayer = player.getName();
            // 增加上镜次数
            LiveRecorder.getInstance().getStorage().updatePlayerData(player.getName(), "times", String.valueOf(LiveRecorder.getInstance().getStorage().getPlayerDataByName(player.getName()).getTimes() + 1));
            recordedSeconds = 0;
        }
        // 随机将一位玩家进行直播
        else if (recordedSeconds == 0 && !LiveCore.activePlayers.isEmpty()) {
            ActivePlayer activePlayer = ((List<ActivePlayer>) LiveCore.activePlayers.values()).get(new Random().nextInt(LiveCore.activePlayers.size()));
            if (!activePlayer.isExternal()) { // 如果是当前服务器的玩家
                Player player = Bukkit.getPlayer(activePlayer.getName());
                if (player == null || !player.isValid()) return; // 玩家是假的就结束
                // 记录初始位置
                activePlayer.setBeginLocation(player.getLocation());
                LiveCore.recorder.teleport(LiveCore.getLiveLocation(player.getLocation()));
                LiveCore.recordingPlayer = player.getName();
                // 如果不是上个玩家而且没有隐藏摄像头
                if (!player.getName().equals(LiveCore.lastPlayer) && LiveRecorder.getInstance().showCamera()) player.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.recorder-come", "&eCongratulations on your appearance, let's take a look at the camera in front of the camera~")).replace("&", "§"));
                LiveCore.lastPlayer = player.getName();
                // 增加上镜次数
                LiveRecorder.getInstance().getStorage().updatePlayerData(player.getName(), "times", String.valueOf(LiveRecorder.getInstance().getStorage().getPlayerDataByName(player.getName()).getTimes() + 1));
            } else { // 发送目标玩家信息并将直播员传送到目标服务器
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Forward");
                out.writeUTF(activePlayer.getServer());
                out.writeUTF("LiveRecorder");
                ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
                DataOutputStream msgOut = new DataOutputStream(msgBytes);
                try {
                    msgOut.writeUTF("Target");
                    msgOut.writeUTF(activePlayer.getName());
                } catch (IOException ignored){}
                out.writeShort(msgBytes.toByteArray().length);
                out.write(msgBytes.toByteArray());
                LiveCore.recorder.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
                // 传送直播员到目标服务器
                out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(activePlayer.getServer());
                LiveCore.recorder.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
            }
        }
        recordedSeconds++;
        if (recordedSeconds > recordSeconds || LiveCore.recordingPlayer == null || Bukkit.getPlayer(LiveCore.recordingPlayer) == null) recordedSeconds = 0;
    }

}
