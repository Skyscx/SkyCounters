package me.skyscx.skycounters.commands;

import me.skyscx.skycounters.Datebase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TopMessagesCMD implements CommandExecutor {
    private final Datebase datebase;

    public TopMessagesCMD(Datebase datebase) {
        this.datebase = datebase;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> topPlayers = datebase.getTopPlayers();

        if (topPlayers.isEmpty()) {
            sender.sendMessage("ПУсто, как так то...");
            return true;
        }

        sender.sendMessage("§3Топ игроков по отправленным сообщениям:");
        for (String player : topPlayers) {
            sender.sendMessage(player);
        }
        if (sender instanceof Player player){
            int placeTopMessages = datebase.getPlayerPosition(player.getName());
            String message = "§3Вы занимаете §7" + placeTopMessages + "§3 место в рейтинге.";
            sender.sendMessage(message);
        }
        return true;
    }
}
