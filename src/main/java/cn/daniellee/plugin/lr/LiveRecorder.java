package cn.daniellee.plugin.lr;

import cn.daniellee.plugin.lr.command.RecorderCommand;
import cn.daniellee.plugin.lr.command.RecorderCompleter;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.listener.BungeeListener;
import cn.daniellee.plugin.lr.listener.PlayerListener;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import cn.daniellee.plugin.lr.storage.MysqlStorage;
import cn.daniellee.plugin.lr.storage.Storage;
import cn.daniellee.plugin.lr.storage.StorageConverter;
import cn.daniellee.plugin.lr.storage.YamlStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class LiveRecorder extends JavaPlugin {

	private static final String PACKAGE_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    private static LiveRecorder instance;

    private String prefix;

    private Storage storage;

    private boolean bungeecord;

    private BukkitTask liveTask;

    private boolean hideCamera;

    private boolean firstPerspective;

    public void onEnable(){
        instance = this;

        if (loadConfig()) {
            getLogger().info(" ");
            getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>> LiveRecorder Loaded <<<<<<<<<<<<<<<<<<<<<<<<");
            getLogger().info(">>>>> If you encounter any bugs, please contact author's QQ: 768318841 <<<<<");
            getLogger().info(" ");

            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

            Bukkit.getPluginCommand("liverecorder").setExecutor(new RecorderCommand());
            Bukkit.getPluginCommand("liverecorder").setTabCompleter(new RecorderCompleter());

            bungeecord = Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);

            if (bungeecord) {
                this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener());
                LiveCore.fetchServerName();
            }

            liveTask = new LiveRunnable().runTaskTimer(this, 0, 20);
        }
    }

    public boolean loadConfig() {
        getLogger().info("[LiveRecorder] Loading config...");
        if(!getDataFolder().exists()) getDataFolder().mkdirs();
        if (storage instanceof MysqlStorage) {
            ((MysqlStorage) storage).close();
        }
        storage = getConfig().getBoolean("storage.mysql.use", false) ? new MysqlStorage() : new YamlStorage();
        if (storage.initialize()) {
            getLogger().info("[LiveRecorder]Storage initialized.");
        } else {
            getLogger().info(" ");
            getLogger().info("[LiveRecorder]Initializing data store failed, please edit the config and reload the plugin.".replace("&", "§"));
            getLogger().info(" ");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
        // 转换存储
        if (getConfig().getBoolean("storage.mysql.use", false) && getConfig().getBoolean("storage.mysql.convert", false)) {
            Storage yamlStorage = new YamlStorage();
            if (yamlStorage.initialize()) {
                StorageConverter converter = new StorageConverter((MysqlStorage) storage, (YamlStorage) yamlStorage);
                converter.execute();
                getConfig().set("storage.mysql.convert", false);
                saveConfig();
                getLogger().info("[LiveRecorder]Successfully transferred Yaml data to Mysql.");
            } else {
                getLogger().info(" ");
                getLogger().info("[LiveRecorder]Yaml data store initialization failed, data conversion canceled.".replace("&", "§"));
                getLogger().info(" ");
            }
        }
        prefix = "&7[&b" + getConfig().get("prompt-prefix", "LiveRecorder") + "&7] &3: &r";
        hideCamera = getConfig().getBoolean("setting.hide-camera", false);
        firstPerspective = getConfig().getBoolean("setting.first-perspective", false);
        saveDefaultConfig();
        return true;
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

    public boolean showCamera() {
        return !hideCamera;
    }

    public boolean isFirstPerspective() {
        return firstPerspective;
    }

    public Storage getStorage() {
        return storage;
    }

    public boolean isBungeecord() {
        return bungeecord;
    }

    public static Class<?> getNMSClass(String nmsClassName) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + PACKAGE_VERSION + "." + nmsClassName);
	}
}
