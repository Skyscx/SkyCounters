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
            System.getLogger("TRUE has permisions");
            if (args.length > 1){
                System.getLogger("TRUE args.lengh > 1");
                if (args[0] != null){
                    System.getLogger("TRUE args[0] != null");
                    String name = args[0];
                    datebase.deletePlayerLogyc(name, sender);
                }
                System.getLogger("FALSE args[0] != null");
            }
            System.getLogger("FALSE args.lengs >1");
        }
        System.getLogger("FALSE has perms");
        return true;
    }
}
