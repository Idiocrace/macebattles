package net.pixelateddream.macebattles;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class MatchDeathListener implements Listener {
    private final Macebattles plugin;

    public MatchDeathListener(Macebattles plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        Player killer = deadPlayer.getKiller();

        MatchmakingListener listener = plugin.getMatchmakingListener();
        if (listener == null) return;

        // Check if player is in an active match
        ActiveMatch match = listener.getMatchByPlayer(deadPlayer.getUniqueId());
        if (match == null) return;

        // Check if round is already being processed
        if (match.isProcessingRoundEnd()) {
            plugin.getLogger().info("Round already being processed for match " + match.getMatchId() + ", ignoring duplicate death event");
            return;
        }

        // Set flag to prevent duplicate processing
        match.setProcessingRoundEnd(true);

        // Player is in a match - handle round death
        event.setDeathMessage(null); // Suppress default death message
        event.getDrops().clear(); // Prevent item drops
        event.setDroppedExp(0); // Prevent XP drops

        // Get both players
        Player player1 = Bukkit.getPlayer(match.getPlayer1UUID());
        Player player2 = Bukkit.getPlayer(match.getPlayer2UUID());

        if (player1 == null || player2 == null) {
            match.setProcessingRoundEnd(false);
            return;
        }

        // Determine winner of this round
        UUID winner;
        UUID loser;
        if (deadPlayer.equals(player1)) {
            winner = match.getPlayer2UUID();
            loser = match.getPlayer1UUID();
        } else {
            winner = match.getPlayer1UUID();
            loser = match.getPlayer2UUID();
        }

        Player winnerPlayer = Bukkit.getPlayer(winner);
        Player loserPlayer = Bukkit.getPlayer(loser);

        if (winnerPlayer == null || loserPlayer == null) {
            match.setProcessingRoundEnd(false);
            return;
        }

        // Award round win
        match.addRoundWin(winner);

        // Cancel any pending round timer
        listener.cancelRoundTimer(match.getMatchId());

        // Broadcast round result
        int currentRound = match.getCurrentRound();
        int winnerScore = match.getScore(winner);
        int loserScore = match.getScore(loser);

        // Send messages to both players
        String winnerName = winnerPlayer.getName();
        String loserName = loserPlayer.getName();

        winnerPlayer.sendMessage("§e§l========== ROUND " + currentRound + " ENDED ==========");
        winnerPlayer.sendMessage("§a§l" + winnerName + " §7eliminated §c§l" + loserName);
        winnerPlayer.sendMessage("§7Score: §a" + winnerScore + " §7- §c" + loserScore);
        winnerPlayer.sendMessage("");

        loserPlayer.sendMessage("§e§l========== ROUND " + currentRound + " ENDED ==========");
        loserPlayer.sendMessage("§a§l" + winnerName + " §7eliminated §c§l" + loserName);
        loserPlayer.sendMessage("§7Score: §a" + loserScore + " §7- §c" + winnerScore);
        loserPlayer.sendMessage("");

        plugin.getLogger().info("Round " + currentRound + " ended in match " + match.getMatchId() +
                                ". Winner: " + winnerName + " (Score: " + winnerScore + "-" + loserScore + ")");

        // Respawn dead player immediately
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            deadPlayer.spigot().respawn();

            // Check if match is complete (first to 3 wins)
            if (winnerScore >= 3) {
                listener.endMatchEarly(match);
            } else {
                // Continue to next round after delay
                listener.startNextRound(match);
            }
        }, 1L);
    }
}

