package me.skyscx.skycounters;

import me.skyscx.skycounters.commands.TopMessagesCMD;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;


// =============================================================================
// SkyCounters
// =============================================================================
// [RU] Плагин разработан для игрового сервера майнкрафт - ZenPower RP.
// [EN] Plugin developed for the Minecraft game server - ZenPower RP.
// -----------------------------------------------------------------------------
// Author:
// - Skyscx (https://github.com/Skyscx)
// -----------------------------------------------------------------------------


public final class SkyCounters extends JavaPlugin {
    private Datebase datebase;
    @Override
    public void onEnable() {
        try {
            datebase = new Datebase(getDataFolder(), this);
        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("topmessages").setExecutor(new TopMessagesCMD(datebase));

        Bukkit.getPluginManager().registerEvents(new Events(datebase), this);


    }

    @Override
    public void onDisable() {
        datebase.closeConnection();
    }

}
