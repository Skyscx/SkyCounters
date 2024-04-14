package me.skyscx.skycounters.commands;

import me.skyscx.skycounters.Datebase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DeletePlayerInDatebase implements CommandExecutor {
    private final Datebase datebase;


    public DeletePlayerInDatebase(Datebase datebase) {
        this.datebase = datebase;

    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("skycounters.admin") || sender.isOp()){
            if (args.length == 1){
                if (args[0] != null){
                    String name = args[0];
                    datebase.deletePlayerLogyc(name, sender);
                }
            }
        }
        return true;
    }
}
