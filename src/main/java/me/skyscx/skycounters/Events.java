package me.skyscx.skycounters;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {
    private final Datebase datebase;


    public Events(Datebase datebase) {
        this.datebase = datebase;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String name = event.getPlayer().getName();
        datebase.checkPlayerTask(name);
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String name = event.getPlayer().getName();
        datebase.updatePlayerCountMessages(name);

    }

}
