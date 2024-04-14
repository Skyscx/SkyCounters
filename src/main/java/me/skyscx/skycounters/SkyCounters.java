package me.skyscx.skycounters;

import me.skyscx.skycounters.commands.TopMessagesCMD;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Objects;

public final class SkyCounters extends JavaPlugin {

    private Datebase datebase;

    @Override
    public void onEnable() {
        try {
            datebase = new Datebase(getDataFolder(), this);
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Failed to create database.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginCommand command = getCommand("topmessages");
        if (command != null) {
            command.setExecutor(new TopMessagesCMD(datebase, this));
        }

        Bukkit.getPluginManager().registerEvents(new Events(datebase, this), this);


    }

    @Override
    public void onDisable() {
        datebase.closeConnection();
    }

}
