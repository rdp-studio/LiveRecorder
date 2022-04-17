package cn.daniellee.plugin.lr.storage;

import cn.daniellee.plugin.lr.model.PlayerData;

import java.util.HashMap;
import java.util.Map;

public abstract class Storage {

	public Map<String, PlayerData> allPlayerData = new HashMap<>();

	public abstract PlayerData getPlayerDataByName(String name);

	public abstract boolean initialize();

	public abstract void refreshCache();

	public abstract PlayerData refreshPlayerCache(String name);

	public abstract void addPlayerData(PlayerData playerData);

	public abstract void updatePlayerData(String name, String column, String value);
}
