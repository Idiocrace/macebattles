package net.pixelateddream.macebattles.entity;

import net.pixelateddream.macebattles.Macebattles;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

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
        entity.getUniqueId();

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
