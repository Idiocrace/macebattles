package net.pixelateddream.macebattles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class DuelsMenu {
    private final Macebattles plugin;
    // Pending teleports for offline players
    private final HashMap<UUID, Location> pendingTeleports = new HashMap<>();

    public DuelsMenu(Macebattles plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main duels menu for a player
     */
    public void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, "§6§lDUELS MENU");
        MatchmakingListener listener = plugin.getMatchmakingListener();
        boolean isConnected = listener != null && listener.isConnected();

        // Request fresh rating from matchmaking server (only if connected)
        if (isConnected) {
            listener.requestPlayerRating(player.getUniqueId());
            // Note: Rating will be cached asynchronously when server responds
        }

        // Get player's rating (may be cached from previous request or default)
        int rating = getPlayerRating(player.getUniqueId());
        String rankGroup = getRankGroup(rating);
        String rankColor = getRankColor(rating);

        // Get leaderboard place 1 (cached, triggers re-request if expired)
        String topPlayerName = "Loading...";
        int topPlayerRating = 0;
        boolean leaderboardLoaded = false;
        LeaderboardPlayer top = isConnected ? listener.getLeaderboardPlace(1) : null;
        if (top != null && top.getName() != null && !top.getName().isEmpty()) {
            topPlayerName = listener.getPlayerNameFromUUID(top.getName());
            topPlayerRating = top.getRating();
            leaderboardLoaded = true;
        }

        // If leaderboard not loaded, schedule a refresh when the response arrives
        if (isConnected && !leaderboardLoaded) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Only refresh if player still has the menu open
                if (player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals("§6§lDUELS MENU")) {
                    openMainMenu(player);
                }
            }, 20L); // 1 second delay
        }

        // Rating display (icon changes by rank)
        Material statsMaterial;
        switch (rankGroup.toLowerCase()) {
            case "withered":
                statsMaterial = Material.WITHER_SKELETON_SKULL;
                break;
            case "netherite":
                statsMaterial = Material.NETHERITE_INGOT;
                break;
            case "diamond":
                statsMaterial = Material.DIAMOND;
                break;
            case "gold":
                statsMaterial = Material.GOLD_INGOT;
                break;
            case "iron":
                statsMaterial = Material.IRON_INGOT;
                break;
            case "stone":
                statsMaterial = Material.STONE;
                break;
            case "dirt":
            default:
                statsMaterial = Material.DIRT;
                break;
        }

        // Rating display (center) - or barrier if not connected
        ItemStack ratingItem;
        ItemMeta ratingMeta;

        if (isConnected) {
            ratingItem = new ItemStack(statsMaterial);
            ratingMeta = ratingItem.getItemMeta();
            ratingMeta.setDisplayName("§6§lYour Ranked Stats");
            ratingMeta.setLore(Arrays.asList(
                "§7Rating: §e" + rating,
                "§7Rank: " + rankColor + rankGroup,
                "",
                "§7Top Player:",
                "§e" + topPlayerName + " §7(" + topPlayerRating + ")",
                "",
                "§7Play ranked matches to",
                "§7improve your rating!"
            ));
        } else {
            ratingItem = new ItemStack(Material.BARRIER);
            ratingMeta = ratingItem.getItemMeta();
            ratingMeta.setDisplayName("§c§lMatchmaking Not Connected");
            ratingMeta.setLore(Arrays.asList(
                "§7The matchmaking server is",
                "§7currently unavailable.",
                "",
                "§7Duels are not available",
                "§7at this time.",
                "",
                "§cRetrying connection..."
            ));
        }
        ratingItem.setItemMeta(ratingMeta);
        menu.setItem(4, ratingItem);

        // Casual queue button (Stone Sword - left)
        ItemStack casualItem = new ItemStack(isConnected ? Material.STONE_SWORD : Material.GRAY_DYE);
        ItemMeta casualMeta = casualItem.getItemMeta();

        if (isConnected) {
            casualMeta.setDisplayName("§e§lCasual Match");
            casualMeta.setLore(Arrays.asList(
                "§7Play for fun!",
                "",
                "§7• No rating changes",
                "§7• Quick matchmaking",
                "§7• Same great gameplay",
                "",
                "§eClick to queue!"
            ));
        } else {
            casualMeta.setDisplayName("§c§lCasual Match");
            casualMeta.setLore(Arrays.asList(
                "§7Play for fun!",
                "",
                "§c§lUnavailable",
                "§7Matchmaking server not connected",
                "",
                "§7Please wait for reconnection..."
            ));
        }
        casualItem.setItemMeta(casualMeta);
        menu.setItem(2, casualItem);

        // Ranked queue button (Netherite Sword - right)
        ItemStack rankedItem = new ItemStack(isConnected ? Material.NETHERITE_SWORD : Material.GRAY_DYE);
        ItemMeta rankedMeta = rankedItem.getItemMeta();

        if (isConnected) {
            rankedMeta.setDisplayName("§6§lRanked Match");
            rankedMeta.setLore(Arrays.asList(
                "§7Competitive play!",
                "",
                "§7• Rating-based matchmaking",
                "§7• Gain or lose rating",
                "§7• Climb the ranks",
                "",
                "§6Click to queue!"
            ));
        } else {
            rankedMeta.setDisplayName("§c§lRanked Match");
            rankedMeta.setLore(Arrays.asList(
                "§7Competitive play!",
                "",
                "§c§lUnavailable",
                "§7Matchmaking server not connected",
                "",
                "§7Please wait for reconnection..."
            ));
        }
        rankedItem.setItemMeta(rankedMeta);
        menu.setItem(6, rankedItem);

        player.openInventory(menu);
    }


    /**
     * Gets the player's current rating
     * Returns cached rating or default 1000 if not yet fetched from server
     * Rating is requested from server when menu opens and cached asynchronously
     */
    private int getPlayerRating(UUID playerUUID) {
        Integer cachedRating = plugin.getPlayerRating(playerUUID);
        return cachedRating != null ? cachedRating : 1000;
    }

    /**
     * Determines rank group based on rating
     */
    private String getRankGroup(int rating) {
        if (rating < 1100) return "Dirt";
        if (rating < 1250) return "Stone";
        if (rating < 1400) return "Iron";
        if (rating < 1550) return "Gold";
        if (rating < 1700) return "Diamond";
        if (rating < 1850) return "Netherite";
        return "Withered";
    }

    /**
     * Gets the color code for a rank group
     */
    private String getRankColor(int rating) {
        if (rating < 1100) return "§7"; // Gray
        if (rating < 1250) return "§7"; // Gray
        if (rating < 1400) return "§f"; // White
        if (rating < 1550) return "§6"; // Gold
        if (rating < 1700) return "§b"; // Aqua
        if (rating < 1850) return "§8"; // Dark Gray
        return "§5"; // Dark Purple
    }

    /**
     * Handles click events in the duels menu
     */
    public void handleMenuClick(Player player, ItemStack clickedItem, int slot) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        Material type = clickedItem.getType();
        MatchmakingListener listener = plugin.getMatchmakingListener();
        boolean isConnected = listener != null && listener.isConnected();

        // Casual queue (slot 2)
        if (slot == 2) {
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

        // Ranked queue (slot 6)
        else if (slot == 6) {
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
