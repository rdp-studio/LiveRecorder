package cn.daniellee.plugin.lr.core;

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
}
