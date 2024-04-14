package me.skyscx.skycounters.commands;

import me.skyscx.skycounters.Datebase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TopMessagesCMD implements CommandExecutor {
    private final Datebase datebase;


    public TopMessagesCMD(Datebase datebase) {
        this.datebase = datebase;

    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            if (args[0].equalsIgnoreCase("messages")){
                CompletableFuture<List<String>> topPlayersMessageTop = datebase.getTopPlayersMessageTop();
                topPlayersMessageTop.thenAccept(list -> {
                    if (list.isEmpty()) {
                        sender.sendMessage("Пусто, как так то...");
                        return;
                    }

                    sender.sendMessage("§3Топ игроков по отправленным сообщениям:");
                    for (String player : list) {
                        sender.sendMessage(player);
                    }
                    if (sender instanceof Player player) {
                        int place = datebase.getPlayerPositionMessageTop(player.getName());
                        String message = "§3Вы занимаете §7" + place + "§3 место в рейтинге.";
                        sender.sendMessage(message);
                    }
                });
                return true;
            }
            if (args[0].equalsIgnoreCase("deaths")){
                CompletableFuture<List<String>> topPlayersDeathsTop = datebase.getTopPlayersDeathTop();
                topPlayersDeathsTop.thenAccept(list -> {
                    if (list.isEmpty()) {
                        sender.sendMessage("Пусто, как так то...");
                        return;
                    }
                    sender.sendMessage("§3Топ игроков по смертям:");
                    for (String player : list) {
                        sender.sendMessage(player);
                    }
                    if (sender instanceof Player player) {
                        int place = datebase.getPlayerPositionDeathsTop(player.getName());
                        String message = "§3Вы занимаете §7" + place + "§3 место в рейтинге.";
                        sender.sendMessage(message);
                    }
                });
                return true;
            }
            if (args[0].equalsIgnoreCase("kills")){
                CompletableFuture<List<String>> topPlayersKillsTop = datebase.getTopPlayersKillsTop();
                topPlayersKillsTop.thenAccept(list -> {
                    if (list.isEmpty()) {
                        sender.sendMessage("Пусто, как так то...");
                        return;
                    }
                    sender.sendMessage("§3Топ игроков по убийствам:");
                    for (String player : list) {
                        sender.sendMessage(player);
                    }
                    if (sender instanceof Player player) {
                        int place = datebase.getPlayerPositionKillsTop(player.getName());
                        String message = "§3Вы занимаете §7" + place + "§3 место в рейтинге.";
                        sender.sendMessage(message);
                    }
                });
                return true;
            }
        }else{
            return false;
        }
        return true;
    }

}
