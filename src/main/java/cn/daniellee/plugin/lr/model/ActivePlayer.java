package cn.daniellee.plugin.lr.model;

public class ActivePlayer {

    private String name;

    private long lastActive;

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
}
