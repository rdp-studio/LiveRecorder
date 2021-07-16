package cn.daniellee.plugin.lr.listener;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.ActivePlayer;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

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
					// 更新活跃玩家列表
					String players = msgIn.readUTF();
					String[] playerArray = players.split(",");
					for (String playerString : playerArray) {
						String[] playerInfo = playerString.split(";");
						ActivePlayer activePlayer = new ActivePlayer(playerInfo[0], Long.valueOf(playerInfo[1]));
						activePlayer.setExternal(true);
						activePlayer.setServer(playerInfo[2]);
						LiveCore.activePlayers.put(playerInfo[0], activePlayer);
					}
				} else if ("Target".equals(msg)) {
					LiveCore.nextPlayer = msgIn.readUTF();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
