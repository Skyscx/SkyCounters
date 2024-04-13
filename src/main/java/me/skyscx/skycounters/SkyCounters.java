package me.skyscx.skycounters;

import me.skyscx.skycounters.commands.TopMessagesCMD;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SkyCounters extends JavaPlugin {

    private Datebase datebase;

    @Override
    public void onEnable() {
        datebase = new Datebase("plugin.db");
        datebase.createTable();

        Objects.requireNonNull(getCommand("topmessages")).setExecutor(new TopMessagesCMD(datebase));

        Bukkit.getPluginManager().registerEvents(new Events(datebase), this);
    }

    @Override
    public void onDisable() {
        datebase.closeConnection();
    }
}
