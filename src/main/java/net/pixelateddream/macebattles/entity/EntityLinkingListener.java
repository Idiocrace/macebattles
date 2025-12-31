package net.pixelateddream.macebattles.entity;

import net.pixelateddream.macebattles.Macebattles;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.UUID;

/**
 * Handles the linking/unlinking mode for operators
 */
public class EntityLinkingListener implements Listener {
    private final Macebattles plugin;

    public EntityLinkingListener(Macebattles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Check if player is in linking mode
        if (plugin.isPlayerInLinkingMode(playerUUID)) {
            event.setCancelled(true);

            String command = plugin.getLinkingCommand(playerUUID);
            boolean consoleExecutor = plugin.isLinkingConsoleExecutor(playerUUID);

            // Create the link
            UUID entityUUID = event.getRightClicked().getUniqueId();
            plugin.addEntityCommandLink(entityUUID, command, consoleExecutor);

            player.sendMessage("§a§lEntity Linked!");
            player.sendMessage("§7Entity: §e" + event.getRightClicked().getType());
            player.sendMessage("§7Command: §b" + command);
            player.sendMessage("§7Executor: §e" + (consoleExecutor ? "Console" : "Player"));
            player.sendMessage("§7Players can now right-click this entity to execute the command!");

            // Exit linking mode
            plugin.clearPlayerLinkingMode(playerUUID);
            return;
        }

        // Check if player is in unlinking mode
        if (plugin.isPlayerInUnlinkingMode(playerUUID)) {
            event.setCancelled(true);

            UUID entityUUID = event.getRightClicked().getUniqueId();
            EntityCommandLink link = plugin.getEntityCommandLink(entityUUID);

            if (link == null) {
                player.sendMessage("§cThis entity has no command linked!");
            } else {
                plugin.removeEntityCommandLink(entityUUID);
                player.sendMessage("§a§lEntity Unlinked!");
                player.sendMessage("§7Entity: §e" + event.getRightClicked().getType());
                player.sendMessage("§7Removed command: §b" + link.getCommand());
            }

            // Exit unlinking mode
            plugin.clearPlayerUnlinkingMode(playerUUID);
        }
    }
}

