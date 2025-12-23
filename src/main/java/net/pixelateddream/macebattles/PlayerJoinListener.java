package net.pixelateddream.macebattles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final Macebattles plugin;

    public PlayerJoinListener(Macebattles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Send welcome message to the joining player
        player.sendMessage("");
        player.sendMessage("§8§m                                                    ");
        player.sendMessage("");
        player.sendMessage("     §6§lWelcome to MaceBattles!");
        player.sendMessage("");
        player.sendMessage("     §7Use §e/duels §7to start dueling!");
        player.sendMessage("     §7• §e/duels queue casual §7- Join casual matches");
        player.sendMessage("     §7• §e/duels queue ranked §7- Compete for rank");
        player.sendMessage("");
        player.sendMessage("§8§m                                                    ");
        player.sendMessage("");

        // Log join
        plugin.getLogger().info(player.getName() + " joined the server");
    }
}

