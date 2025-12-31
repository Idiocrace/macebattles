package net.pixelateddream.macebattles.commands;

import net.pixelateddream.macebattles.Macebattles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record MbBuildCommand(Macebattles plugin) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player) || sender.isOp()) {
            sender.sendMessage("§7Macebattles Build: §e" + plugin.getBuildNumber());
        } else {
            sender.sendMessage("§cYou do not have permission to use this command.");
        }
        return true;
    }
}

