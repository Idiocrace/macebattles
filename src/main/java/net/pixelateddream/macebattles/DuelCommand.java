package net.pixelateddream.macebattles;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelCommand implements CommandExecutor {
    private final Macebattles plugin;
    private final Map<UUID, DuelRequest> pendingDuels;

    public DuelCommand(Macebattles plugin) {
        this.plugin = plugin;
        this.pendingDuels = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // /duel <player> - Send duel request
        // /duel accept - Accept pending duel request
        // /duel deny - Deny pending duel request

        if (args.length == 0) {
            player.sendMessage("§cUsage: /duel <player> or /duel accept/deny");
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("accept")) {
            return handleAccept(player);
        } else if (action.equals("deny") || action.equals("decline")) {
            return handleDeny(player);
        } else {
            // Send duel request to another player
            return handleDuelRequest(player, args[0]);
        }
    }

    private boolean handleDuelRequest(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (target.equals(sender)) {
            sender.sendMessage("§cYou cannot duel yourself!");
            return true;
        }

        MatchmakingListener listener = plugin.getMatchmakingListener();

        // Check if sender is already in a match
        if (listener != null && listener.getMatchByPlayer(sender.getUniqueId()) != null) {
            sender.sendMessage("§cYou are already in a match!");
            return true;
        }

        // Check if target is already in a match
        if (listener != null && listener.getMatchByPlayer(target.getUniqueId()) != null) {
            sender.sendMessage("§c" + target.getName() + " is already in a match!");
            return true;
        }

        // Check if target has a pending request
        if (pendingDuels.containsKey(target.getUniqueId())) {
            sender.sendMessage("§c" + target.getName() + " already has a pending duel request!");
            return true;
        }

        // Create duel request
        DuelRequest request = new DuelRequest(sender.getUniqueId(), target.getUniqueId());
        pendingDuels.put(target.getUniqueId(), request);

        // Notify both players
        sender.sendMessage("§aDuel request sent to §e" + target.getName() + "§a!");
        target.sendMessage("§e" + sender.getName() + " §ahas challenged you to a duel!");
        target.sendMessage("§aType §e/duel accept §ato accept or §c/duel deny §ato decline");

        // Auto-expire after 60 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingDuels.remove(target.getUniqueId()) != null) {
                Player senderNow = Bukkit.getPlayer(request.getSenderUUID());
                Player targetNow = Bukkit.getPlayer(request.getTargetUUID());

                if (senderNow != null) {
                    senderNow.sendMessage("§cDuel request to " + target.getName() + " expired");
                }
                if (targetNow != null) {
                    targetNow.sendMessage("§cDuel request from " + sender.getName() + " expired");
                }
            }
        }, 60 * 20L); // 60 seconds

        return true;
    }

    private boolean handleAccept(Player accepter) {
        DuelRequest request = pendingDuels.remove(accepter.getUniqueId());

        if (request == null) {
            accepter.sendMessage("§cYou don't have any pending duel requests!");
            return true;
        }

        Player challenger = Bukkit.getPlayer(request.getSenderUUID());

        if (challenger == null) {
            accepter.sendMessage("§cThe player who challenged you is no longer online!");
            return true;
        }

        MatchmakingListener listener = plugin.getMatchmakingListener();

        // Double-check neither player is in a match
        if (listener != null) {
            if (listener.getMatchByPlayer(challenger.getUniqueId()) != null) {
                accepter.sendMessage("§c" + challenger.getName() + " is now in a match!");
                return true;
            }
            if (listener.getMatchByPlayer(accepter.getUniqueId()) != null) {
                accepter.sendMessage("§cYou are already in a match!");
                return true;
            }
        }

        // Start the duel
        accepter.sendMessage("§aYou accepted the duel with §e" + challenger.getName() + "§a!");
        challenger.sendMessage("§e" + accepter.getName() + " §aaccepted your duel request!");

        // Start a casual match between them
        if (listener != null) {
            String matchId = "duel-" + System.currentTimeMillis();
            listener.startDirectDuel(matchId, challenger, accepter);
        } else {
            accepter.sendMessage("§cMatchmaking system is not available!");
            challenger.sendMessage("§cMatchmaking system is not available!");
        }

        return true;
    }

    private boolean handleDeny(Player denier) {
        DuelRequest request = pendingDuels.remove(denier.getUniqueId());

        if (request == null) {
            denier.sendMessage("§cYou don't have any pending duel requests!");
            return true;
        }

        Player challenger = Bukkit.getPlayer(request.getSenderUUID());

        denier.sendMessage("§cYou declined the duel request");
        if (challenger != null) {
            challenger.sendMessage("§c" + denier.getName() + " declined your duel request");
        }

        return true;
    }

    /**
     * Inner class to represent a duel request
     */
    private static class DuelRequest {
        private final UUID senderUUID;
        private final UUID targetUUID;

        public DuelRequest(UUID senderUUID, UUID targetUUID) {
            this.senderUUID = senderUUID;
            this.targetUUID = targetUUID;
        }

        public UUID getSenderUUID() {
            return senderUUID;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }
    }
}

