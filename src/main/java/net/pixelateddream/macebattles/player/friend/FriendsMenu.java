package net.pixelateddream.macebattles.player.friend;

import net.pixelateddream.macebattles.Macebattles;
import net.pixelateddream.macebattles.misc.MatchmakingListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FriendsMenu {
    private final Macebattles plugin;
    // Pending teleports for offline players
    private final HashMap<UUID, Location> pendingTeleports = new HashMap<>();

    public FriendsMenu(Macebattles plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main duels menu for a player
     */
    public void friendsListMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 72, "§6§lFRIENDS LIST");

        // Buttons for back, next page, and switch view
        // (switch view being switching to

        // Back button (slot 83)
        ItemStack backItem = new ItemStack(Material.DISC_FRAGMENT_5);
        ItemMeta backMeta = backItem.getItemMeta();
        assert backMeta != null;
        backMeta.setDisplayName("§c§lBack");
        backMeta.setItemModel(NamespacedKey.fromString("arrow_left"));
        backItem.setItemMeta(backMeta);
        menu.setItem(84, backItem);

        // Page indicator (slot 85)
        ItemStack pageItem = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageItem.getItemMeta();
        assert pageMeta != null;

    }
    public void friendsMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6§lFRIENDS MENU");


    }
    /**
     * Gets the players current friend list
     */
    private List<Player> getFriendsList(Player player) {
        return FriendsManager.getFriendsList(player);
    }

    /**
     * Handles click events in the friends menu
     */
    public void handleMenuClick(Player player, ItemStack clickedItem, int slot) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        Material type = clickedItem.getType();
        MatchmakingListener listener = plugin.getMatchmakingListener();
        boolean isConnected = listener != null && listener.isConnected();

        // Casual queue (slot 14)
        if (slot == 14) {
            player.closeInventory();

            if (!isConnected) {
                player.sendMessage("§c§lMatchmaking Not Available");
                player.sendMessage("§cThe matchmaking server is not connected.");
                player.sendMessage("§7Please wait a moment and try again.");
                return;
            }

            if (type == Material.STONE_SWORD) {
                // Check if already in queue
                if (listener.isPlayerQueued(player.getUniqueId())) {
                    player.sendMessage("§cYou are already in the matchmaking queue!");
                    return;
                }
                listener.queuePlayer(player, MatchmakingListener.QueueType.CASUAL);
            }
        }
        // Ranked queue (slot 16)
        else if (slot == 16) {
            player.closeInventory();

            if (!isConnected) {
                player.sendMessage("§c§lMatchmaking Not Available");
                player.sendMessage("§cThe matchmaking server is not connected.");
                player.sendMessage("§7Please wait a moment and try again.");
                return;
            }

            if (type == Material.NETHERITE_SWORD) {
                // Check if already in queue
                if (listener.isPlayerQueued(player.getUniqueId())) {
                    player.sendMessage("§cYou are already in the matchmaking queue!");
                    return;
                }
                listener.queuePlayer(player, MatchmakingListener.QueueType.RANKED);
            }
        }
        // Friends menu (slot 12)
        else if (slot == 12) {
            player.closeInventory();
            player.sendMessage("§b§lFriends Menu");
            player.sendMessage("§7This feature is coming soon.");
        }
    }

    /**
     * Add a pending teleport for an offline player
     */
    public void addPendingTeleport(UUID playerUUID, Location location) {
        if (playerUUID != null && location != null) {
            pendingTeleports.put(playerUUID, location.clone());
        }
    }

    /**
     * Call this on player join to check and teleport if needed
     */
    public void handlePlayerJoin(Player player) {
        Location loc = pendingTeleports.remove(player.getUniqueId());
        if (loc != null) {
            player.teleport(loc);
            player.sendMessage("§aYou have been returned to spawn (offline match cleanup).");
            plugin.getLogger().info("Teleported offline player " + player.getName() + " to spawn after match.");
        }
    }
}
