package cn.daniellee.plugin.lr.storage;

import cn.daniellee.plugin.lr.model.PlayerData;

public class StorageConverter {

    private MysqlStorage mysqlStorage;

    private YamlStorage yamlStorage;

    public StorageConverter(MysqlStorage mysqlStorage, YamlStorage yamlStorage) {
        this.mysqlStorage = mysqlStorage;
        this.yamlStorage = yamlStorage;
    }

    public void execute() {
        for (PlayerData playerData : yamlStorage.allPlayerData.values()) {
            mysqlStorage.addPlayerData(playerData);
        }
    }

}
