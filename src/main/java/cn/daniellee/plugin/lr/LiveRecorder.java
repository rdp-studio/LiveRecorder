package cn.daniellee.plugin.lr;

import cn.daniellee.plugin.lr.command.RecorderCommand;
import cn.daniellee.plugin.lr.listener.PlayerListener;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class LiveRecorder extends JavaPlugin {

    private static LiveRecorder instance;

    private String prefix;

    BukkitTask liveTask;

    public void onEnable(){
        instance = this;

        loadConfig();
        getLogger().info(" ");
        getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>> LiveRecorder Loaded <<<<<<<<<<<<<<<<<<<<<<<<");
        getLogger().info(">>>>> If you encounter any bugs, please contact author's QQ: 768318841 <<<<<");
        getLogger().info(" ");

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        Bukkit.getPluginCommand("liverecorder").setExecutor(new RecorderCommand());

        liveTask = new LiveRunnable().runTaskTimerAsynchronously(this, 0, 20);
    }

    public void loadConfig() {
        getLogger().info("[LiveRecorder] Loading config...");
        if(!getDataFolder().exists()) getDataFolder().mkdirs();
        prefix = "&7[&b" + getConfig().get("prompt-prefix", "LiveRecorder") + "&7] &3: &r";
        saveDefaultConfig();
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

}
