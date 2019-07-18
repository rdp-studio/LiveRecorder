package cn.daniellee.plugin.lr.runnable;

import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Random;

public class LiveRunnable extends BukkitRunnable {

    private static int recordedSeconds = 0;

    @Override
    public void run() {
        if (LiveCore.recorder == null) LiveCore.recorder = Bukkit.getPlayer("Recorder");
        // 每个玩家播出的时间
        int recordSeconds = 30;
        // 当作不活跃的超时时间
        int inactivityTimeout = 15;
        synchronized (LiveCore.activePlayers) {
            // 清理不活跃的
            for (int i = 0; i < LiveCore.activePlayers.size();) {
                ActivePlayer activePlayer = LiveCore.activePlayers.get(i);
                if (System.currentTimeMillis() - activePlayer.getLastActive() > inactivityTimeout * 1000) {
                   LiveCore.activePlayers.remove(i);
                } else i++;
            }
            // 随机将一位玩家进行直播
            if (recordedSeconds == 0 && LiveCore.recorder != null) {
                ActivePlayer activePlayer = LiveCore.activePlayers.get(new Random().nextInt(LiveCore.activePlayers.size()));
                // 计算镜头位置
                Player player = Bukkit.getPlayer(activePlayer.getName());
                Location location = player.getLocation();
                // 读取镜头角度
                float pitch = 45F;
                // 读取录制距离
                int distance = 5;
                double hypotenuse = new BigDecimal(Math.cos(Math.toRadians(pitch))).multiply(new BigDecimal(distance)).doubleValue();
                double angle = new BigDecimal(location.getYaw() % 360).abs().doubleValue();
                double calcAngle = angle % 90;
                BigDecimal addX = new BigDecimal(Math.sin(Math.toRadians(calcAngle))).multiply(new BigDecimal(hypotenuse));
                BigDecimal addZ = new BigDecimal(Math.cos(Math.toRadians(calcAngle))).multiply(new BigDecimal(hypotenuse));
                double x;
                double y = new BigDecimal(location.getY()).add(new BigDecimal(Math.sin(Math.toRadians(pitch))).multiply(new BigDecimal(distance))).doubleValue();
                double z;
                if (angle > 0 && angle < 180) {
                    x = new BigDecimal(location.getX()).subtract(addX).doubleValue();
                } else {
                    x = new BigDecimal(location.getX()).add(addX).doubleValue();
                }
                if (angle > 90 && angle < 270) {
                    z = new BigDecimal(location.getZ()).add(addZ).doubleValue();
                } else {
                    z = new BigDecimal(location.getZ()).subtract(addZ).doubleValue();
                }
                //镜头位置
                Location targetLocation = new Location(player.getWorld(), x, y, z, location.getYaw(), pitch);
                LiveCore.recorder.teleport(targetLocation);
                LiveCore.recordingPlayer = activePlayer.getName();
            }
            recordedSeconds++;
            if (recordedSeconds == recordSeconds) recordedSeconds = 0;
        }
    }

}
