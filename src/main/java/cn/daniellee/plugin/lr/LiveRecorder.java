package cn.daniellee.plugin.lr;

import cn.daniellee.plugin.lr.listener.PlayerListener;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LiveRecorder extends JavaPlugin {

    private static LiveRecorder instance;

    private String prefix;

    private ProtocolManager protocolManager;

    public void onLoad() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void onEnable(){
        instance = this;

        loadConfig();
        getLogger().info(" ");
        getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>> LiveRecorder Loaded <<<<<<<<<<<<<<<<<<<<<<<<");
        getLogger().info(">>>>> If you encounter any bugs, please contact author's QQ: 768318841 <<<<<");
        getLogger().info(" ");

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

//        Bukkit.getPluginCommand("liverecorder").setExecutor(new ExpertCommand());

//        protocolManager.addPacketListener(new PacketAdapter(this, ConnectionSide.SERVER_SIDE,
//                Packets.Server.ENTITY_EQUIPMENT,
//                Packets.Server.SET_SLOT,
//                Packets.Server.WINDOW_ITEMS) {
//            @Override
//            public void onPacketSending(PacketEvent event) {
//                PacketContainer packet = event.getPacket();
//
//                try {
//                    // Player equipment
//                    switch (event.getPacketID()) {
//                        case Packets.Server.ENTITY_EQUIPMENT:
//                            modifyItemStack(packet.getItemModifier().read(0));
//                            break;
//
//                        case Packets.Server.SET_SLOT:
//                            int slotID = packet.getSpecificModifier(int.class).read(1);
//
//                            // Modify equipment slots
//                            if (slotID >= 5 && slotID < 9)
//                                modifyItemStack(packet.getItemModifier().read(0));
//                            break;
//
//                        case Packets.Server.WINDOW_ITEMS:
//                            ItemStack[] stack = packet.getItemArrayModifier().read(0);
//
//                            for (int i = 5; i < 9; i++) {
//                                modifyItemStack(stack[i]);
//                            }
//                            break;
//                    }
//
//                } catch (FieldAccessException e) {
//                    System.out.println("Couldn't access field.");
//                }
//            }
//        });
    }

    public void loadConfig() {
        getLogger().info("[LiveRecorder] Loading config...");
        if(!getDataFolder().exists()) getDataFolder().mkdirs();
        prefix = "&7[&b" + getConfig().get("prompt-prefix", "LiveRecorder") + "&7] &3: &r";
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
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

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
