package cn.daniellee.plugin.lr.core;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LiveCore {

    public static String serverName = "Default";

    public static final ConcurrentHashMap<String, ActivePlayer> activePlayers = new ConcurrentHashMap<>();

    public static Player recorder;

    public static String recordingPlayer;

    public static String lastPlayer;

    public static String nextPlayer; // 由其他服务器传过来的指定玩家

    public static void fetchServerName() {
        if (LiveRecorder.getInstance().isBungeecord()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("GetServer");
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player != null) {
                player.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
            }
        }
    }

    public static void sendActivePlayerMessage(Player player) {
        if (LiveRecorder.getInstance().isBungeecord()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("LiveRecorder");
            ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(msgBytes);
            try {
                msgOut.writeUTF("Player");
                String[] players = (String[]) activePlayers.values().stream().filter(i -> !i.isExternal()).collect(Collectors.toList()).stream().map(i -> i.getName() + ";" + i.getLastActive() + ";" + serverName).toArray();
                msgOut.writeUTF(StringUtils.join(players, ","));
            } catch (IOException ignored){}
            out.writeShort(msgBytes.toByteArray().length);
            out.write(msgBytes.toByteArray());
            player.sendPluginMessage(LiveRecorder.getInstance(), "BungeeCord", out.toByteArray());
        }
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
