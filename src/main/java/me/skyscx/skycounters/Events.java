package me.skyscx.skycounters;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.parser.Entity;

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
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) { // проверяем, является ли жертва игроком
            Player victim = (Player) event.getEntity(); // преобразуем сущность-жертву в игрока
            if (victim.getLastDamageCause().getEntity() instanceof Player) { // проверяем, является ли последняя причина смерти игроком
                Player killer = (Player) victim.getLastDamageCause().getEntity(); // преобразуем сущность-убийцу в игрока
                String killer_name = killer.getName();
                datebase.updatePlayerCountKills(killer_name);
                String victim_name = victim.getName();
                datebase.updatePlayerCountDeath(victim_name);
            } else { // если игрок просто погиб по какой-либо причине
                String victim_name = victim.getName();
                datebase.updatePlayerCountDeath(victim_name);
            }
        } else { // если жертва не является игроком
            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
                if (damageEvent.getDamager() instanceof Player) {
                    Player killer = (Player) damageEvent.getDamager();
                    String killer_name = killer.getName();
                    datebase.updatePlayerCountKillsMobs(killer_name);
                }
            }

        }
    }



}
