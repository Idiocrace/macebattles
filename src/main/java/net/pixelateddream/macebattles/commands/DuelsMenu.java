package net.pixelateddream.macebattles.commands;

import net.pixelateddream.macebattles.Macebattles;
import net.pixelateddream.macebattles.misc.MatchmakingListener;
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
        Inventory menu = Bukkit.createInventory(null, 27, "§6§lDUELS MENU");
        MatchmakingListener listener = plugin.getMatchmakingListener();
        boolean isConnected = listener != null && listener.isConnected();

        if (isConnected) {
            listener.requestPlayerRating(player.getUniqueId());
        }

        int rating = getPlayerRating(player.getUniqueId());
        String rankGroup = getRankGroup(rating);
        String rankColor = getRankColor(rating);

        // Rank display (slot 10)
        Material statsMaterial = switch (rankGroup.toLowerCase()) {
            case "withered" -> Material.WITHER_SKELETON_SKULL;
            case "netherite" -> Material.NETHERITE_INGOT;
            case "diamond" -> Material.DIAMOND;
            case "gold" -> Material.GOLD_INGOT;
            case "iron" -> Material.IRON_INGOT;
            case "stone" -> Material.STONE;
            case "dirt" -> Material.DIRT;
            case "unranked" -> Material.BOOK;
            default -> Material.BARRIER;
        };
        ItemStack rankItem = new ItemStack(statsMaterial);
        ItemMeta rankMeta = rankItem.getItemMeta();
        assert rankMeta != null;
        rankMeta.setDisplayName("§6§lYour Ranked Stats");
        rankMeta.setLore(Arrays.asList(
            "§7Rating: §e" + rating,
            "§7Rank: " + rankColor + rankGroup,
            "",
            "§7Play ranked matches to",
            "§7improve your rating!"
        ));
        rankItem.setItemMeta(rankMeta);
        menu.setItem(10, rankItem);

        // Friends menu (slot 12)
        ItemStack friendsItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta friendsMeta = friendsItem.getItemMeta();
        assert friendsMeta != null;
        friendsMeta.setDisplayName("§b§lFriends Menu");
        friendsMeta.setLore(Arrays.asList(
            "§7Manage your friends list",
            "§7and see who's online!",
            "",
            "§b§cFeature coming soon"
        ));
        friendsItem.setItemMeta(friendsMeta);
        menu.setItem(12, friendsItem);

        // Casual match (slot 14)
        ItemStack casualItem = new ItemStack(isConnected ? Material.STONE_SWORD : Material.GRAY_DYE);
        ItemMeta casualMeta = casualItem.getItemMeta();
        assert casualMeta != null;
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
        menu.setItem(14, casualItem);

        // Ranked match (slot 16)
        ItemStack rankedItem = new ItemStack(isConnected ? Material.NETHERITE_SWORD : Material.GRAY_DYE);
        ItemMeta rankedMeta = rankedItem.getItemMeta();
        assert rankedMeta != null;
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
        menu.setItem(16, rankedItem);

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
        if (rating == -1) return "Unranked";
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
        if (getRankGroup(rating).equals("Unranked")) return "§c"; // Red
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
