package cn.daniellee.plugin.lr.storage;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.model.PlayerData;

import java.sql.*;

public class MysqlStorage extends Storage {

	private Connection connection;

	private String tablePrefix;

	@Override
	public boolean initialize() {
		// 初始化Mysql驱动
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]An error occurred while getting the Mysql database driver.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
			return false;
		}
		// 初始化连接
		String url = "jdbc:mysql://" + LiveRecorder.getInstance().getConfig().getString("storage.mysql.host", "localhost") + ":" + LiveRecorder.getInstance().getConfig().getInt("storage.mysql.port", 3306) + "/" + LiveRecorder.getInstance().getConfig().getString("storage.mysql.database", "minecraft") + "?" + LiveRecorder.getInstance().getConfig().getString("storage.mysql.parameter", "characterEncoding=utf-8&useSSL=false");
		try {
			connection = DriverManager.getConnection(url, LiveRecorder.getInstance().getConfig().getString("storage.mysql.username", "username"), LiveRecorder.getInstance().getConfig().getString("storage.mysql.password", "password"));
		} catch (SQLException e) {
			e.printStackTrace();
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]Mysql connection information is incorrect.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
			return false;
		}
		// 初始化数据表
		tablePrefix = LiveRecorder.getInstance().getConfig().getString("storage.mysql.table_perfix", "lr_");
		String sql = "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "player` (" +
				"`name` varchar(48) NOT NULL," +
				"`denied` tinyint(1) DEFAULT '0'," +
				"`times` int(11) DEFAULT '0'," +
				"PRIMARY KEY (`name`)," +
				"KEY `name_UNIQUE` (`name`)" +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(sql);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]An error occurred while initializing the Mysql data table.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
			return false;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
		refreshCache();
		return true;
	}

	@Override
	public void refreshCache() {
		allPlayerData.clear();
		PreparedStatement statement = null;
		try {
			String sql = "select * from " + tablePrefix + "player";
			statement = connection.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				PlayerData playerData = new PlayerData(name);
				playerData.setDenied(resultSet.getBoolean("denied"));
				playerData.setTimes(resultSet.getInt("times"));
				allPlayerData.put(name, playerData);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]An error occurred while reading building data from Mysql.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	@Override
	public PlayerData refreshPlayerCache(String name) {
		PlayerData playerData = getPlayerDataByName(name);
		PreparedStatement statement = null;
		try {
			String sql = "select * from " + tablePrefix + "player where `name` = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, name);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.first()) {
				playerData.setDenied(resultSet.getBoolean("denied"));
				playerData.setTimes(resultSet.getInt("times"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]An error occurred while reading building data from Mysql.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
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
		PreparedStatement statement = null;
		try {
			String sql = "insert into " + tablePrefix + "player (`name`,`denied`,`times`)values(?,?,?)";
			statement = connection.prepareStatement(sql);
			statement.setString(1, playerData.getName());
			statement.setBoolean(2, playerData.isDenied());
			statement.setInt(3, playerData.getTimes());
			statement.executeUpdate();
			allPlayerData.put(playerData.getName(), playerData);
		} catch (SQLException e) {
			e.printStackTrace();
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]An error occurred while writing building data to Mysql.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	@Override
	public void updatePlayerData(String name, String column, String value) {
		PlayerData playerData = getPlayerDataByName(name);
		if (playerData == null) return;
		PreparedStatement statement = null;
		try {
			String sql = "update " + tablePrefix + "player set " + column + " = ? where `name` = ?";
			statement = connection.prepareStatement(sql);
			switch (column) {
				case "denied":
					boolean denied = Boolean.parseBoolean(value);
					playerData.setDenied(denied);
					statement.setBoolean(1, denied);
					break;
				case "times":
					int times = Integer.parseInt(value);
					playerData.setTimes(times);
					statement.setInt(1, times);
					break;
				default:
					return;
			}
			statement.setString(2, name);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			LiveRecorder.getInstance().getLogger().info(" ");
			LiveRecorder.getInstance().getLogger().info("[LiveRecorder]An error occurred while writing building data to Mysql.".replace("&", "§"));
			LiveRecorder.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException ignored) { }
	}
}
