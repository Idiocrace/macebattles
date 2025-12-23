package net.pixelateddream.macebattles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.UUID;

/**
 * Handles entity interactions for command execution
 */
public class EntityCommandListener implements Listener {
    private final Macebattles plugin;

    public EntityCommandListener(Macebattles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        UUID entityUUID = entity.getUniqueId();

        // Leaderboard linking mode
        if (plugin instanceof Macebattles) {
            Macebattles mb = (Macebattles) plugin;
            EntityCommandCommand cmdCmd = (EntityCommandCommand) mb.getCommand("entitycmd").getExecutor();
            if (cmdCmd != null && cmdCmd.isLeaderboardLinking(player.getUniqueId())) {
                int place = cmdCmd.getLeaderboardLinkingPlace(player.getUniqueId());
                if (entity.getType().name().equalsIgnoreCase("MANNEQUIN")) {
                    mb.addLeaderboardLink(entityUUID, place);
                    player.sendMessage("§aLinked mannequin to leaderboard place " + place + ".");
                    cmdCmd.clearLeaderboardLinking(player.getUniqueId());
                    event.setCancelled(true);
                    return;
                } else {
                    player.sendMessage("§cYou must right-click a minecraft:mannequin mob.");
                    event.setCancelled(true);
                    return;
                }
            }
            if (mb.isPlayerInUnlinkingMode(player.getUniqueId())) {
                if (entity.getType().name().equalsIgnoreCase("MANNEQUIN")) {
                    mb.removeLeaderboardLink(entityUUID);
                    player.sendMessage("§aUnlinked mannequin from leaderboard.");
                    mb.clearPlayerUnlinkingMode(player.getUniqueId());
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Leaderboard mannequin display
        LeaderboardLink leaderboardLink = plugin.getLeaderboardLink(entityUUID);
        if (leaderboardLink != null) {
            event.setCancelled(true);
            // Show leaderboard info (top player for place)
            // This should be updated periodically, but for now, show info on click
            LeaderboardPlayer topPlayer = plugin.getMapManager().getLeaderboardPlayer(leaderboardLink.getPlace());
            if (topPlayer != null) {
                entity.setCustomName(topPlayer.getName() + " (" + topPlayer.getRating() + ")");
                entity.setCustomNameVisible(true);
                // Set mannequin skin to topPlayer.getSkin() (requires NMS or API)
                // TODO: Implement skin update for mannequin
                player.sendMessage("§eLeaderboard Place " + leaderboardLink.getPlace() + ": §b" + topPlayer.getName() + " §7Rating: §e" + topPlayer.getRating());
            } else {
                entity.setCustomName("No player");
                entity.setCustomNameVisible(true);
                player.sendMessage("§cNo player found for leaderboard place " + leaderboardLink.getPlace());
            }
            return;
        }

        // Check if this entity has a command linked
        EntityCommandLink link = plugin.getEntityCommandLink(entity.getUniqueId());
        if (link == null) {
            return;
        }

        // Cancel the default interaction
        event.setCancelled(true);

        // Get the command with placeholders replaced
        String command = link.getReplacedCommand(player.getName(), player.getUniqueId().toString());

        // Execute the command
        if (link.isConsoleExecutor()) {
            // Execute as console
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                plugin.getLogger().info("Executed command as console from entity " + entity.getUniqueId() + ": " + command);
            });
        } else {
            // Execute as player
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.performCommand(command);
                plugin.getLogger().info("Player " + player.getName() + " executed command from entity " + entity.getUniqueId() + ": " + command);
            });
        }
    }
}
