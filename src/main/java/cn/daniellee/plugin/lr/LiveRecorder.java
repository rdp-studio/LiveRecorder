package cn.daniellee.plugin.lr;

import cn.daniellee.plugin.lr.command.RecorderCommand;
import cn.daniellee.plugin.lr.listener.PlayerListener;
import cn.daniellee.plugin.lr.model.PlayerData;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LiveRecorder extends JavaPlugin {

	private static final String PACKAGE_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    private static LiveRecorder instance;

    private String prefix;

    private BukkitTask liveTask;

    private boolean hideCamera;

    private File playerDataFile = new File(this.getDataFolder(), "player.yml");

    public FileConfiguration playerDataYaml = new YamlConfiguration();

    public Map<String, PlayerData> playerData = new HashMap<>();

    public void onEnable(){
        instance = this;

        loadConfig();
        getLogger().info(" ");
        getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>> LiveRecorder Loaded <<<<<<<<<<<<<<<<<<<<<<<<");
        getLogger().info(">>>>> If you encounter any bugs, please contact author's QQ: 768318841 <<<<<");
        getLogger().info(" ");

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        Bukkit.getPluginCommand("liverecorder").setExecutor(new RecorderCommand());

        liveTask = new LiveRunnable().runTaskTimer(this, 0, 20);
    }

    public void loadConfig() {
        getLogger().info("[LiveRecorder] Loading config...");
        if(!getDataFolder().exists()) getDataFolder().mkdirs();
        prefix = "&7[&b" + getConfig().get("prompt-prefix", "LiveRecorder") + "&7] &3: &r";
        hideCamera = getConfig().getBoolean("setting.hide-camera", false);
        saveDefaultConfig();
        try {
            if (!playerDataFile.exists()) playerDataFile.createNewFile();
            playerDataYaml.load(playerDataFile);
        } catch (Exception e) {
            this.getLogger().info(" ");
            this.getLogger().info("[LiveRecorder]An error occurred in player file load.".replace("&", "§"));
            this.getLogger().info(" ");
            e.printStackTrace();
        }
        Set<String> names = playerDataYaml.getKeys(false);
        for (String name : names) {
            PlayerData data = new PlayerData(name);
            data.setDenied(playerDataYaml.getBoolean(name + ".denied"));
            data.setTimes(playerDataYaml.getInt(name + "times", 0));
            playerData.put(name, data);
        }
    }

    @Override
    public void onDisable() {
        if (liveTask != null) liveTask.cancel();

        getLogger().info(" ");
        getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>> LiveRecorder Unloaded <<<<<<<<<<<<<<<<<<<<<<<<");
        getLogger().info(" ");
    }

    public static LiveRecorder getInstance() {
        return instance;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isHideCamera() {
        return hideCamera;
    }

    public void updatePlayerDataYaml(String key, Object value) {
        playerDataYaml.set(key, value);
        try {
            playerDataYaml.save(playerDataFile);
        } catch (IOException e) {
            this.getLogger().info(" ");
            this.getLogger().info("[LiveRecorder]An error occurred in player file save.".replace("&", "§"));
            this.getLogger().info(" ");
            e.printStackTrace();
        }
    }

    public static Class<?> getNMSClass(String nmsClassName) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + PACKAGE_VERSION + "." + nmsClassName);
	}
}
