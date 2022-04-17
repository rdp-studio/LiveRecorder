package cn.daniellee.plugin.lr.listener;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import cn.daniellee.plugin.lr.model.PlayerData;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;

public class BungeeListener implements PluginMessageListener {

	@Override
	public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
		ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
		String subChannel = in.readUTF();
		if ("GetServer".equals(subChannel)) {
			String serverName = in.readUTF();
			LiveCore.serverName = serverName;
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]Successfully fetched the server name: " + serverName);
		} else if ("LiveRecorder".equals(subChannel)) {
			short length = in.readShort();
			byte[] msgBytes = new byte[length];
			in.readFully(msgBytes);
			DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgBytes));
			try {
				String msg = msgIn.readUTF();
				if ("Player".equals(msg)) {
                    String[] info = msgIn.readUTF().split(";");
                    LiveCore.otherOnline.put(info[0], Integer.valueOf(info[1]));
                    if (!LiveCore.otherRecording && Boolean.parseBoolean(info[2])) {
                        LiveCore.otherRecording = true;
                    }
					// 更新活跃玩家列表
					String players = msgIn.readUTF();
					if (players.length() > 0) {
						String[] playerArray = players.split(",");
						for (String playerString : playerArray) {
							String[] playerInfo = playerString.split(";");
							ActivePlayer activePlayer = new ActivePlayer(playerInfo[0], Long.valueOf(playerInfo[1]));
							activePlayer.setExternal(true);
							activePlayer.setServer(playerInfo[2]);
							LiveCore.activePlayers.put(playerInfo[0], activePlayer);
						}
					}
				} else if ("Target".equals(msg)) {
					String name = msgIn.readUTF();
					new BukkitRunnable() {
						@Override
						public void run() {
							ActivePlayer targetPlayer = LiveCore.activePlayers.get(name);
							if (LiveCore.recorder != null && targetPlayer != null && !targetPlayer.isExternal()) {
								LiveCore.nextPlayer = name;
								this.cancel();
							}
						}
					}.runTaskTimerAsynchronously(LiveRecorder.getInstance(), 0, 10);
                } else if ("Change".equals(msg)) {
                    String name = msgIn.readUTF();
                    if (LiveCore.recorder != null) {
                        ActivePlayer activePlayer = LiveCore.activePlayers.get(name);
                        LiveCore.recordPlayer(activePlayer);
                    }
				} else if ("Refresh".equals(msg)) {
					String name = msgIn.readUTF();
					PlayerData playerData = LiveRecorder.getInstance().getStorage().refreshPlayerCache(name);
					if (playerData.isDenied()) LiveCore.activePlayers.remove(name);
				} else if ("Join".equals(msg)) {
                    Bukkit.broadcastMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.boardcast.online", "&eThe live recording started, all ready for the mirror~")).replace("&", "§"));
                } else if ("Leave".equals(msg)) {
                    LiveCore.otherRecording = false;
                    Bukkit.broadcastMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.boardcast.offline", "&eThe live recording is over, thanks to the support of the friends~")).replace("&", "§"));
                }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
