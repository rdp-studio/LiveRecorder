package cn.daniellee.plugin.lr.model;

import org.bukkit.Location;

public class ActivePlayer {

    private String name;

    private long lastActive;

    private Location beginLocation;

    private String server;

    private boolean external;

    public ActivePlayer(String name, long lastActive) {
        this.name = name;
        this.lastActive = lastActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public Location getBeginLocation() {
        return beginLocation;
    }

    public void setBeginLocation(Location beginLocation) {
        this.beginLocation = beginLocation;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }
}
