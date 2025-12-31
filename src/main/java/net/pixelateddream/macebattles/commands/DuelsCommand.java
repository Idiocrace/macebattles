package net.pixelateddream.macebattles;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelsCommand implements CommandExecutor, TabCompleter {
    private final Macebattles plugin;
    private final Map<UUID, DuelRequest> pendingDuels;

    public DuelsCommand(Macebattles plugin) {
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

        if (args.length == 0) {
            // Open the duels menu GUI
            plugin.getDuelsMenu().openMainMenu(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "invite":
            case "challenge":
                return handleInvite(player, args);
            
            case "accept":
                return handleAccept(player);
            
            case "deny":
            case "decline":
                return handleDeny(player);
            
            case "queue":
            case "q":
                return handleQueue(player, args);
            
            case "cancel":
            case "leave":
                return handleCancelQueue(player);

            default:
                sendHelp(player);
                return true;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§e§l===== DUELS COMMANDS =====");
        player.sendMessage("§e/duels invite <player> §7- Challenge a player to a duel");
        player.sendMessage("§e/duels accept §7- Accept a pending duel");
        player.sendMessage("§e/duels deny §7- Decline a pending duel");
        player.sendMessage("§e/duels queue <casual|ranked> §7- Join matchmaking");
        player.sendMessage("§e/duels cancel §7- Leave matchmaking queue");
        player.sendMessage("§7Aliases: §e/duel, /d");
    }

    private boolean handleInvite(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /duels invite <player>");
            return true;
        }

        String targetName = args[1];
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
        target.sendMessage("§aType §e/duels accept §ato accept or §c/duels deny §ato decline");

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

    private boolean handleQueue(Player player, String[] args) {
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

        // Check if player is already in queue
        if (listener.isPlayerQueued(player.getUniqueId())) {
            player.sendMessage("§cYou are already in the matchmaking queue!");
            return true;
        }

        // Determine queue type from arguments (default to CASUAL)
        MatchmakingListener.QueueType queueType = MatchmakingListener.QueueType.CASUAL;

        if (args.length > 1) {
            String queueArg = args[1].toUpperCase();
            if (queueArg.equals("RANKED") || queueArg.equals("COMPETITIVE") || queueArg.equals("COMP")) {
                queueType = MatchmakingListener.QueueType.RANKED;
            } else if (queueArg.equals("CASUAL") || queueArg.equals("CAS")) {
                queueType = MatchmakingListener.QueueType.CASUAL;
            } else {
                player.sendMessage("§cUsage: /duels queue <casual|ranked>");
                return true;
            }
        }

        // Queue the player with specified queue type
        listener.queuePlayer(player, queueType);
        return true;
    }

    private boolean handleCancelQueue(Player player) {
        MatchmakingListener listener = plugin.getMatchmakingListener();

        if (listener == null || !listener.isConnected()) {
            player.sendMessage("§cMatchmaking server is not connected!");
            return true;
        }

        // Cancel the queue
        listener.dequeuePlayer(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            List<String> subcommands = Arrays.asList("invite", "accept", "deny", "queue", "cancel");
            String input = args[0].toLowerCase();
            for (String sub : subcommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("invite") || subcommand.equals("challenge")) {
                // Second argument for invite - online player names
                String input = args[1].toLowerCase();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(input) && !p.equals(sender)) {
                        completions.add(p.getName());
                    }
                }
            } else if (subcommand.equals("queue") || subcommand.equals("q")) {
                // Second argument for queue - casual/ranked
                List<String> queueTypes = Arrays.asList("casual", "ranked");
                String input = args[1].toLowerCase();
                for (String type : queueTypes) {
                    if (type.startsWith(input)) {
                        completions.add(type);
                    }
                }
            }
        }

        return completions;
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

