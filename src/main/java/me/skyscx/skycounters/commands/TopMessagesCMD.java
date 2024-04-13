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
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        List<String> topPlayers = datebase.getTopPlayers();

        if (topPlayers.isEmpty()) {
            sender.sendMessage("No players found.");
            return true;
        }

        sender.sendMessage("Top 10 players by message count:");
        for (String player : topPlayers) {
            sender.sendMessage(player);
        }

        return true;
    }
}
