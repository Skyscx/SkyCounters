package me.skyscx.skycounters.commands;

import me.skyscx.skycounters.Datebase;
import me.skyscx.skycounters.SkyCounters;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopMessagesCMD implements CommandExecutor {
    private final Datebase datebase;
    private final SkyCounters plugin;

    public TopMessagesCMD(Datebase datebase, SkyCounters plugin) {
        this.datebase = datebase;
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        datebase.getTopPlayers().thenAcceptAsync((topPlayers) -> {
            if (topPlayers.isEmpty()) {
                sender.sendMessage("Пусто, как так то...");
                return;
            }

            sender.sendMessage("§3Топ игроков по отправленным сообщениям:");
            for (String player : topPlayers) {
                sender.sendMessage(player);
            }
            if (sender instanceof Player player) {
                datebase.getPlayerPosition(player.getName()).thenAcceptAsync((position) -> {
                    if (position == -1) {
                        sender.sendMessage("Игрок не найден.");
                    } else {
                        String message = "§3Вы занимаете §7" + position + "§3 место в рейтинге.";
                        sender.sendMessage(message);
                    }
                }, Bukkit.getScheduler().runTask(plugin));
            }
        }, Bukkit.getScheduler().runTask(plugin));

        return true;
    }

}
