package cn.daniellee.plugin.lr.storage;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.model.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class YamlStorage extends Storage {

	private File playerDataFile = new File(LiveRecorder.getInstance().getDataFolder(), "player.yml");

	private FileConfiguration playerDataYaml = new YamlConfiguration();

	@Override
	public boolean initialize() {
		try {
			if (!playerDataFile.exists()) playerDataFile.createNewFile();
			playerDataYaml.load(playerDataFile);
		} catch (Exception e) {
			e.printStackTrace();
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("&5[LiveRecorder]An error occurred in player data file load.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
			return false;
		}
		refreshCache();
		return true;
	}

	@Override
	public void refreshCache() {
		allPlayerData.clear();
		Set<String> names = playerDataYaml.getKeys(false);
		for (String name : names) {
			PlayerData playerData = new PlayerData(name);
            playerData.setDenied(playerDataYaml.getBoolean(name + ".denied"));
			playerData.setTimes(playerDataYaml.getInt(name + ".times", 0));
			allPlayerData.put(name, playerData);
		}
	}

	@Override
	public PlayerData refreshPlayerCache(String name) {
		PlayerData playerData = getPlayerDataByName(name);
		playerData.setDenied(playerDataYaml.getBoolean(name + ".denied"));
		playerData.setTimes(playerDataYaml.getInt(name + ".times", 0));
		return playerData;
	}

	@Override
	public PlayerData getPlayerDataByName(String name) {
		PlayerData playerData = allPlayerData.get(name);
		if (playerData == null) {
			playerData = new PlayerData(name);
			addPlayerData(playerData);
		}
		return playerData;
	}

	@Override
	public void addPlayerData(PlayerData playerData) {
		// 写配置
		playerDataYaml.set(playerData.getName() + ".denied", playerData.isDenied());
		playerDataYaml.set(playerData.getName() + ".times", playerData.getTimes());
		savePlayerData();
		allPlayerData.put(playerData.getName(), playerData);
	}

	@Override
	public void updatePlayerData(String name, String column, String value) {
		PlayerData playerData = getPlayerDataByName(name);
		if (playerData == null) return;
		switch (column) {
			case "denied":
				boolean denied = Boolean.parseBoolean(value);
				playerData.setDenied(denied);
				playerDataYaml.set(playerData.getName() + ".denied", denied);
				break;
			case "likes":
				int times = Integer.parseInt(value);
				playerData.setTimes(times);
				playerDataYaml.set(playerData.getName() + ".times", times);
				break;
			default:
				return;
		}
		savePlayerData();
	}

	private void savePlayerData() {
		try {
			playerDataYaml.save(playerDataFile);
		} catch (IOException e) {
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]An error occurred in player data file save.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
			e.printStackTrace();
		}
	}

}
