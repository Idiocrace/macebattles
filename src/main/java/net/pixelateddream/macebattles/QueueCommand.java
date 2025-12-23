package net.pixelateddream.macebattles;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {
    private final Macebattles plugin;

    public QueueCommand(Macebattles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        MatchmakingListener listener = plugin.getMatchmakingListener();

        if (listener == null || !listener.isConnected()) {
            player.sendMessage("§cMatchmaking server is not connected!");
            return true;
        }

        // Check if player is already in a match
        if (listener.getMatchByPlayer(player.getUniqueId()) != null) {
            player.sendMessage("§cYou are already in a match!");
            return true;
        }

        // Determine queue type from arguments (default to CASUAL)
        MatchmakingListener.QueueType queueType = MatchmakingListener.QueueType.CASUAL;

        if (args.length > 0) {
            String queueArg = args[0].toUpperCase();
            if (queueArg.equals("RANKED") || queueArg.equals("COMPETITIVE") || queueArg.equals("COMP")) {
                queueType = MatchmakingListener.QueueType.RANKED;
            } else if (queueArg.equals("CASUAL") || queueArg.equals("CAS")) {
                queueType = MatchmakingListener.QueueType.CASUAL;
            } else {
                player.sendMessage("§cUsage: /queue [casual|ranked]");
                return true;
            }
        }

        // Queue the player with specified queue type
        listener.queuePlayer(player, queueType);
        return true;
    }
}

