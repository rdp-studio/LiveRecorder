package cn.daniellee.plugin.lr.core;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.util.List;

public class LiveCore {

    public static final List<ActivePlayer> activePlayers = new java.util.Vector<>();

    public static Player recorder;

    public static String recordingPlayer;

    public static Vector getVectorByFormTo(Location from, Location to) {
        double x = new BigDecimal(to.getX()).subtract(new BigDecimal(from.getX())).doubleValue();
        double y = new BigDecimal(to.getY()).subtract(new BigDecimal(from.getY())).doubleValue();
        double z = new BigDecimal(to.getZ()).subtract(new BigDecimal(from.getZ())).doubleValue();
        return new Vector(x, y, z);
    }

    public static ActivePlayer getActivePlayerByName(String name) {
        for (ActivePlayer activePlayer : activePlayers) {
            if (activePlayer.getName().equals(name)) {
                return activePlayer;
            }
        }
        return null;
    }

    public static Location getLiveLovation(Location location) {
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
