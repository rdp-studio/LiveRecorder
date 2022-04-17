package cn.daniellee.plugin.lr.core;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LiveCore {

    public static String serverName = "Default";

    public static ConcurrentHashMap<String, ActivePlayer> activePlayers = new ConcurrentHashMap<>();

    public static boolean living = true;

    public static Player recorder;

    public static String recordingPlayer;

    public static String lastPlayer;

    public static String nextPlayer; // 由其他服务器传过来的指定玩家

    public static boolean otherRecording; // 其他服务器是否在直播

    public static boolean goingOther; // 正在去其他服务器

    public static HashMap<String, Integer> otherOnline = new HashMap<>();

    public static void fetchServerName() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null) {
            player.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
        }
    }

    public static void sendActivePlayerMessage(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("LiveRecorder");
        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(msgBytes);
        try {
            msgOut.writeUTF("Player");
            List<String> players = new ArrayList<>();
            activePlayers.values().stream().filter(i -> !i.isExternal() && !i.getName().equals(LiveRecorder.getInstance().getConfig().getString("setting.recorder-name", "Recorder"))).collect(Collectors.toList()).forEach(i -> players.add(i.getName() + ";" + i.getLastActive() + ";" + serverName));
            msgOut.writeUTF(LiveCore.serverName + ";" + players.size() + ";" + (LiveCore.recorder != null)); // 直播员是否在这个服务器，服务器在线人数
            msgOut.writeUTF(String.join(",", players));
        } catch (IOException ignored){}
        out.writeShort(msgBytes.toByteArray().length);
        out.write(msgBytes.toByteArray());
        player.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
    }

    // 直播员进入消息
    public static void sendJoinMessage(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("LiveRecorder");
        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(msgBytes);
        try {
            msgOut.writeUTF("Join");
        } catch (IOException ignored){}
        out.writeShort(msgBytes.toByteArray().length);
        out.write(msgBytes.toByteArray());
        player.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
    }

    // 直播员退出消息
    public static void sendLeaveMessage(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("LiveRecorder");
        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(msgBytes);
        try {
            msgOut.writeUTF("Leave");
        } catch (IOException ignored){}
        out.writeShort(msgBytes.toByteArray().length);
        out.write(msgBytes.toByteArray());
        player.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
    }

    // 跨服改变直播目标
    public static void sendChangeMessage(Player player, String name) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("LiveRecorder");
        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(msgBytes);
        try {
            msgOut.writeUTF("Change");
            msgOut.writeUTF(name);
        } catch (IOException ignored){}
        out.writeShort(msgBytes.toByteArray().length);
        out.write(msgBytes.toByteArray());
        player.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
    }

    // 发送玩家禁止被直播消息
    public static void sendRefreshMessage(Player player, String name) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("LiveRecorder");
        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(msgBytes);
        try {
            msgOut.writeUTF("Refresh");
            msgOut.writeUTF(name);
        } catch (IOException ignored){}
        out.writeShort(msgBytes.toByteArray().length);
        out.write(msgBytes.toByteArray());
        player.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void sendTargetMessage(ActivePlayer activePlayer) {
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
        recorder.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
        goingOther = true;
        // 传送直播员到目标服务器
        out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(activePlayer.getServer());
        recorder.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void recordPlayer(ActivePlayer activePlayer) {
        if (activePlayer == null) return;
        if (!activePlayer.isExternal()) { // 如果是当前服务器的玩家
            Player player = Bukkit.getPlayer(activePlayer.getName());
            if (player == null || !player.isValid()) return; // 玩家是假的就结束
            // 记录初始位置
            activePlayer.setBeginLocation(player.getLocation());
            recorder.teleport(LiveCore.getLiveLocation(player.getLocation()));
            LiveCore.recorder.setGameMode(GameMode.SPECTATOR);
            if (LiveRecorder.getInstance().isFirstPerspective()) recorder.setSpectatorTarget(player);
            recordingPlayer = player.getName();
            // 如果不是上个玩家而且没有隐藏摄像头
            if (!player.getName().equals(lastPlayer) && LiveRecorder.getInstance().showCamera()) player.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.recorder-come", "&eCongratulations on your appearance, let's take a look at the camera in front of the camera~")).replace("&", "§"));
            lastPlayer = player.getName();
            // 增加上镜次数
            LiveRecorder.getInstance().getStorage().updatePlayerData(player.getName(), "times", String.valueOf(LiveRecorder.getInstance().getStorage().getPlayerDataByName(player.getName()).getTimes() + 1));
            if (LiveRecorder.getInstance().isBungeecord()) sendRefreshMessage(player, player.getName());
        } else sendTargetMessage(activePlayer); // 发送目标玩家信息并将直播员传送到目标服务器
    }

    public static Vector getVectorByFormTo(Location from, Location to) {
        double x = new BigDecimal(to.getX()).subtract(new BigDecimal(from.getX())).doubleValue();
        double y = new BigDecimal(to.getY()).subtract(new BigDecimal(from.getY())).doubleValue();
        double z = new BigDecimal(to.getZ()).subtract(new BigDecimal(from.getZ())).doubleValue();
        return new Vector(x, y, z);
    }

    public static Location getLiveLocation(Location location) {
        // 读取镜头角度
        double pitch = LiveRecorder.getInstance().getConfig().getDouble("setting.camera-pitch", 45D);
        // 读取录制距离
        int distance = LiveRecorder.getInstance().getConfig().getInt("setting.camera-distance", 3);
        double hypotenuse = new BigDecimal(Math.cos(Math.toRadians(pitch))).multiply(new BigDecimal(distance)).doubleValue();
        double angle = location.getYaw() > 0 ? new BigDecimal(360).subtract(new BigDecimal(location.getYaw() % 360)).doubleValue() : - location.getYaw() % 360;
        double y = new BigDecimal(location.getY()).add(new BigDecimal(Math.sin(Math.toRadians(pitch))).multiply(new BigDecimal(distance))).doubleValue();
        double x = new BigDecimal(location.getX()).subtract(new BigDecimal(Math.sin(Math.toRadians(angle))).multiply(new BigDecimal(hypotenuse))).doubleValue();
        double z = new BigDecimal(location.getZ()).subtract(new BigDecimal(Math.cos(Math.toRadians(angle))).multiply(new BigDecimal(hypotenuse))).doubleValue();
        //镜头位置
       return new Location(location.getWorld(), x, y, z, location.getYaw(), (float) pitch);
    }
}
