package ru.zunowskie.zchatpers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZChatPers extends JavaPlugin {

    public static ZChatPers instance;

    public static ZChatPers getInstance(){
        return instance;
    }
    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new Heandler(), this);
        saveDefaultConfig();
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
