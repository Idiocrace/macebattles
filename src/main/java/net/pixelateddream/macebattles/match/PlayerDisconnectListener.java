package net.pixelateddream.macebattles.match;

import net.pixelateddream.macebattles.Macebattles;
import net.pixelateddream.macebattles.misc.MatchmakingListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDisconnectListener implements Listener {
    private final Macebattles plugin;

    public PlayerDisconnectListener(Macebattles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MatchmakingListener listener = plugin.getMatchmakingListener();

        if (listener != null) {
            // Remove player from queue if they're queued
            if (listener.isPlayerQueued(player.getUniqueId())) {
                listener.removePlayerFromQueue(player.getUniqueId());
                plugin.getLogger().info("Removed " + player.getName() + " from matchmaking queue due to disconnect");
            }

            // Check if player is in an active match
            if (listener.getMatchByPlayer(player.getUniqueId()) != null) {
                plugin.getLogger().warning(player.getName() + " disconnected during an active match");
                // Match system should handle player disconnects during matches
                // Could implement forfeit logic here if needed
            }
        }
    }
}

