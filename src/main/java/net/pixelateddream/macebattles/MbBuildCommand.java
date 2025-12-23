package net.pixelateddream.macebattles;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MbBuildCommand implements CommandExecutor {
    private final Macebattles plugin;

    public MbBuildCommand(Macebattles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || sender.isOp()) {
            sender.sendMessage("§7Macebattles Build: §e" + plugin.getBuildNumber());
            return true;
        } else {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
    }
}

