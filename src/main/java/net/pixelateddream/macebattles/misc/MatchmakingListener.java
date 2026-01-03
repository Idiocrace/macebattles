package net.pixelateddream.macebattles.misc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.pixelateddream.macebattles.Macebattles;
import net.pixelateddream.macebattles.match.ActiveMatch;
import net.pixelateddream.macebattles.match.ArenaInstance;
import net.pixelateddream.macebattles.match.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class MatchmakingListener {

    public enum QueueType {
        CASUAL,
        RANKED
    }

    private final Macebattles plugin;
    private WebSocketClient webSocketClient;
    private final Gson gson;
    private final Map<String, ActiveMatch> activeMatches;
    private UUID lastQueuedPlayerUUID; // Track last player who queued to associate rating response
    private final Set<UUID> queuedPlayers = new HashSet<>(); // Track players currently in queue
    private final Map<UUID, Long> queueTimestamps = new HashMap<>(); // Track when players queued
    private static final long QUEUE_TIMEOUT = 300000; // 5 minutes in milliseconds
    private final Map<String, Integer> roundTimers = new HashMap<>(); // Track round timer task IDs
    private final String serverUri; // Store URI for reconnection attempts
    private int reconnectTaskId = -1; // Track reconnection task

    public MatchmakingListener(Macebattles plugin, String serverUri) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.activeMatches = new HashMap<>();
        this.serverUri = serverUri;

        // Initial connection attempt
        connectToServer();

        // Start automatic reconnection task (runs every minute)
        startReconnectionTask();
    }

    /**
     * Attempts to connect to the matchmaking server
     */
    private void connectToServer() {
        try {
            webSocketClient = new WebSocketClient(new URI(serverUri)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    plugin.getLogger().info("✓ Connected to matchmaking server at " + serverUri);
                }

                @Override
                public void onMessage(String message) {
                    plugin.getLogger().info("Received message: " + message);

                    // Process on main thread for thread safety
                    plugin.getServer().getScheduler().runTask(plugin, () -> handleMatchmakingMessage(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    String source = remote ? "server" : "client";
                    plugin.getLogger().warning("✗ Disconnected from matchmaking server (" + source + "): " + reason + " (code: " + code + ")");

                    // Clear all queues on disconnect
                    clearAllQueues();

                    // Notify all online players who were queued
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage("§c§lMatchmaking Server Disconnected");
                        player.sendMessage("§cYou have been removed from any active queues.");
                        player.sendMessage("§7Please wait for reconnection or try again later.");
                    }
                }

                @Override
                public void onError(Exception ex) {
                    plugin.getLogger().severe("✗ WebSocket error: " + ex.getMessage());
                    plugin.getLogger().severe("  This may affect matchmaking functionality.");
                }
            };

            plugin.getLogger().info("Connecting to matchmaking server at " + serverUri + "...");
            webSocketClient.connect();

        } catch (URISyntaxException e) {
            plugin.getLogger().severe("✗ Invalid WebSocket URI: " + serverUri);
            plugin.getLogger().severe("  Matchmaking will not be available!");
        } catch (Exception e) {
            plugin.getLogger().warning("✗ Failed to connect to matchmaking server: " + e.getMessage());
            plugin.getLogger().warning("  Will retry in 60 seconds...");
        }
    }

    /**
     * Starts the automatic reconnection task
     * Checks connection every minute and attempts reconnection if disconnected
     */
    private void startReconnectionTask() {
        reconnectTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isConnected()) {
                plugin.getLogger().info("Matchmaking server not connected. Attempting reconnection...");
                connectToServer();
            }
        }, 1200L, 1200L).getTaskId(); // Start after 60 seconds, repeat every 60 seconds (1200 ticks)

        plugin.getLogger().info("Automatic reconnection task started (checks every 60 seconds)");
    }

    /**
     * Stops the automatic reconnection task
     */
    private void stopReconnectionTask() {
        if (reconnectTaskId != -1) {
            Bukkit.getScheduler().cancelTask(reconnectTaskId);
            reconnectTaskId = -1;
            plugin.getLogger().info("Automatic reconnection task stopped");
        }
    }

    /**
     * Handles incoming matchmaking messages
     */
    private void handleMatchmakingMessage(String message) {
        try {
            JsonObject data = gson.fromJson(message, JsonObject.class);
            String type = data.get("type").getAsString();

            switch (type) {
                case "queued":
                    handleQueued(data);
                    break;
                case "match_found":
                    handleMatchFound(data);
                    break;
                case "result_processed":
                    handleResultProcessed(data);
                    break;
                case "queue_cancelled":
                    handleQueueCancelled(data);
                    break;
                case "rating_response":
                    handleRatingResponse(data);
                    break;
                case "error":
                    handleError(data);
                    break;
                default:
                    plugin.getLogger().warning("Unknown message type: " + type);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error parsing matchmaking message: " + e.getMessage());
        }
    }

    /**
     * Handles rating response from server
     */
    private void handleRatingResponse(JsonObject data) {
        if (!data.has("player_uuid") || !data.has("rating")) {
            plugin.getLogger().warning("Received incomplete rating response");
            return;
        }

        String playerUuidStr = data.get("player_uuid").getAsString();
        int rating = data.get("rating").getAsInt();

        try {
            UUID playerUUID = UUID.fromString(playerUuidStr);
            plugin.setPlayerRating(playerUUID, rating);
            plugin.getLogger().info("Cached rating for player " + playerUUID + ": " + rating);

            // If player has duels menu open, refresh it
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.getOpenInventory().getTitle().equals("§6§lDUELS MENU")) {
                // Refresh the menu to show updated rating
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getDuelsMenu().openMainMenu(player));
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid UUID in rating response: " + playerUuidStr);
        }
    }

    /**
     * Handles queued confirmation from server
     */
    private void handleQueued(JsonObject data) {
        String mode = data.has("mode") ? data.get("mode").getAsString() : "casual";
        int rating = data.has("rating") ? data.get("rating").getAsInt() : 0;
        if (data.has("message")) {
            data.get("message").getAsString();
        }

        plugin.getLogger().info("Queue confirmation: " + mode + " (rating: " + rating + ")");

        // Cache the rating for the player who just queued
        if (lastQueuedPlayerUUID != null && mode.equals("ranked")) {
            plugin.setPlayerRating(lastQueuedPlayerUUID, rating);
            plugin.getLogger().info("Cached rating " + rating + " for player " + lastQueuedPlayerUUID);

            // Notify player of successful queue
            Player player = Bukkit.getPlayer(lastQueuedPlayerUUID);
            if (player != null && player.isOnline()) {
                player.sendMessage("§7Queue confirmed! Searching for opponents...");
                player.sendMessage("§7Your rating: §e" + rating);
            }
        } else if (lastQueuedPlayerUUID != null && mode.equals("casual")) {
            // Notify casual player
            Player player = Bukkit.getPlayer(lastQueuedPlayerUUID);
            if (player != null && player.isOnline()) {
                player.sendMessage("§7Queue confirmed! Searching for opponents...");
            }
        }
    }

    /**
     * Handles queue cancelled confirmation from server
     */
    private void handleQueueCancelled(JsonObject data) {
        String messageText = data.has("message") ? data.get("message").getAsString() : "Removed from queue";
        plugin.getLogger().info("Queue cancelled: " + messageText);

        // Notify the player if we can identify them
        if (lastQueuedPlayerUUID != null) {
            Player player = Bukkit.getPlayer(lastQueuedPlayerUUID);
            if (player != null && player.isOnline()) {
                player.sendMessage("§7" + messageText);
            }
        }
    }

    /**
     * Handles error messages from server
     */
    private void handleError(JsonObject data) {
        String errorMessage = data.has("message") ? data.get("message").getAsString() : "Unknown error";
        plugin.getLogger().warning("Matchmaking server error: " + errorMessage);

        // Notify the player who likely triggered this error
        if (lastQueuedPlayerUUID != null) {
            Player player = Bukkit.getPlayer(lastQueuedPlayerUUID);
            if (player != null && player.isOnline()) {
                // Remove from queued set on error
                queuedPlayers.remove(lastQueuedPlayerUUID);
                queueTimestamps.remove(lastQueuedPlayerUUID);

                // Provide user-friendly error messages based on error type
                if (errorMessage.contains("Ranked queue is currently disabled")) {
                    player.sendMessage("§c§lRanked Queue Disabled");
                    player.sendMessage("§cRanked matchmaking is currently unavailable.");
                    player.sendMessage("§7The server administrator has temporarily disabled ranked matches.");
                    player.sendMessage("§7Please try §eCasual §7mode or check back later.");
                } else if (errorMessage.contains("Casual queue is currently disabled")) {
                    player.sendMessage("§c§lCasual Queue Disabled");
                    player.sendMessage("§cCasual matchmaking is currently unavailable.");
                    player.sendMessage("§7The server administrator has temporarily disabled casual matches.");
                    player.sendMessage("§7Please try §6Ranked §7mode or check back later.");
                } else if (errorMessage.contains("player_uuid required")) {
                    player.sendMessage("§c§lQueue Failed!");
                    player.sendMessage("§cInternal error: Player identification missing.");
                    player.sendMessage("§7Please contact a server administrator.");
                } else if (errorMessage.contains("Missing match result data")) {
                    player.sendMessage("§c§lMatch Result Error");
                    player.sendMessage("§cFailed to submit match results.");
                    player.sendMessage("§7Your rating may not have been updated.");
                } else {
                    // Generic error message
                    player.sendMessage("§c§lQueue Failed!");
                    player.sendMessage("§cServer error: " + errorMessage);
                    player.sendMessage("§7Please try again or contact an administrator if the issue persists.");
                }
            }
        }
    }

    /**
     * Handles result processed confirmation from server
     */
    private void handleResultProcessed(JsonObject data) {
        String matchUuid = data.get("match_uuid").getAsString();

        if (data.has("player1_new_rating")) {
            // Ranked match - cache updated ratings
            int player1Rating = data.get("player1_new_rating").getAsInt();
            int player2Rating = data.get("player2_new_rating").getAsInt();
            plugin.getLogger().info("Ranked match " + matchUuid + " results processed. New ratings: " + player1Rating + " / " + player2Rating);

            // Get the active match to find player UUIDs
            ActiveMatch match = activeMatches.get(matchUuid);
            if (match != null) {
                plugin.setPlayerRating(match.getPlayer1UUID(), player1Rating);
                plugin.setPlayerRating(match.getPlayer2UUID(), player2Rating);

                // Notify players of their new ratings
                Player player1 = Bukkit.getPlayer(match.getPlayer1UUID());
                Player player2 = Bukkit.getPlayer(match.getPlayer2UUID());

                if (player1 != null) {
                    player1.sendMessage("§7Your new rating: §e" + player1Rating);
                }
                if (player2 != null) {
                    player2.sendMessage("§7Your new rating: §e" + player2Rating);
                }
            }
        } else {
            // Casual match
            String messageText = data.has("message") ? data.get("message").getAsString() : "Result recorded";
            plugin.getLogger().info("Casual match " + matchUuid + " results processed: " + messageText);
        }
    }

    private void handleMatchFound(JsonObject data) {
        String matchUuid = data.get("match_uuid").getAsString();
        String player1UuidStr = data.get("player1_uuid").getAsString();
        String player2UuidStr = data.get("player2_uuid").getAsString();

        QueueType queueType; // Default to casual, will be set by startMatch
        plugin.getLogger().info("Match found: " + matchUuid);

        // Parse player UUIDs
        UUID player1Uuid = UUID.fromString(player1UuidStr);
        UUID player2Uuid = UUID.fromString(player2UuidStr);

        // Prevent ghost matching: check if either player is already in an active match
        if (getMatchByPlayer(player1Uuid) != null || getMatchByPlayer(player2Uuid) != null) {
            plugin.getLogger().warning("[GhostMatch] Attempted to start a match for player(s) already in an active match: " + player1Uuid + ", " + player2Uuid + ". Ignoring match_found for " + matchUuid);
            return;
        }

        // Remove players from queue and clean up timestamps
        queuedPlayers.remove(player1Uuid);
        queuedPlayers.remove(player2Uuid);
        queueTimestamps.remove(player1Uuid);
        queueTimestamps.remove(player2Uuid);

        // Get online players by UUID
        Player player1 = Bukkit.getPlayer(player1Uuid);
        Player player2 = Bukkit.getPlayer(player2Uuid);

        if (player1 == null || player2 == null) {
            plugin.getLogger().warning("One or more players are not online for match: " + matchUuid);
            if (player1 != null) {
                player1.sendMessage("§c§lMatch Failed!");
                player1.sendMessage("§cYour opponent is no longer online.");
                player1.sendMessage("§7You have been removed from the queue.");
            }
            if (player2 != null) {
                player2.sendMessage("§c§lMatch Failed!");
                player2.sendMessage("§cYour opponent is no longer online.");
                player2.sendMessage("§7You have been removed from the queue.");
            }
            return;
        }

        // Determine queue type based on which player initiated (we'll default to RANKED for server matches)
        queueType = QueueType.RANKED;

        // Start the match (this will send the match found message ONCE)
        startMatch(matchUuid, player1, player2, queueType);
    }

    /**
     * Requests a player's current rating from the matchmaking server
     * The rating will be cached when the server responds
     */
    public void requestPlayerRating(UUID playerUUID) {
        if (!isConnected()) {
            plugin.getLogger().warning("Cannot request rating for " + playerUUID + " - WebSocket not connected");
            return;
        }

        try {
            JsonObject message = new JsonObject();
            message.addProperty("type", "get_rating");
            message.addProperty("player_uuid", playerUUID.toString());

            sendJson(message);
            plugin.getLogger().info("Requested rating for player: " + playerUUID);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to request rating for player " + playerUUID + ": " + e.getMessage());
        }
    }

    /**
     * Queues a player for matchmaking
     * @param player The player to queue
     * @param queueType The type of queue (CASUAL or RANKED)
     */
    public void queuePlayer(Player player, QueueType queueType) {
        if (!isConnected()) {
            player.sendMessage("§c§lQueue Failed!");
            player.sendMessage("§cMatchmaking server is not connected.");
            player.sendMessage("§7Please wait a moment and try again.");
            plugin.getLogger().warning("Cannot queue player " + player.getName() + " - WebSocket not connected");
            return;
        }

        try {
            JsonObject message = new JsonObject();
            message.addProperty("type", "queue");
            message.addProperty("player_uuid", player.getUniqueId().toString());
            message.addProperty("mode", queueType.name().toLowerCase());

            // Track this player for rating response
            lastQueuedPlayerUUID = player.getUniqueId();

            // Add player to queued set and track timestamp
            queuedPlayers.add(player.getUniqueId());
            queueTimestamps.put(player.getUniqueId(), System.currentTimeMillis());

            sendJson(message);

            String queueName = queueType == QueueType.CASUAL ? "§eCasual" : "§6Ranked";
            player.sendMessage("§aYou have been added to the " + queueName + " §amatchmaking queue!");
            plugin.getLogger().info("Queued player: " + player.getName() + " (" + player.getUniqueId() + ") for " + queueType.name());

            // Schedule timeout check
            scheduleQueueTimeout(player.getUniqueId());

        } catch (Exception e) {
            // Remove from queue on error
            queuedPlayers.remove(player.getUniqueId());
            queueTimestamps.remove(player.getUniqueId());

            player.sendMessage("§c§lQueue Failed!");
            player.sendMessage("§cAn error occurred while joining the queue.");
            player.sendMessage("§7Error: " + e.getMessage());
            plugin.getLogger().severe("Failed to queue player " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Schedules a timeout check for a queued player
     */
    private void scheduleQueueTimeout(UUID playerUUID) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check if player is still in queue after timeout period
            if (queuedPlayers.contains(playerUUID)) {
                Long queueTime = queueTimestamps.get(playerUUID);
                if (queueTime != null && System.currentTimeMillis() - queueTime >= QUEUE_TIMEOUT) {
                    // Player has been in queue too long
                    queuedPlayers.remove(playerUUID);
                    queueTimestamps.remove(playerUUID);

                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null && player.isOnline()) {
                        player.sendMessage("§c§lQueue Timeout");
                        player.sendMessage("§cYou have been removed from the queue after 5 minutes.");
                        player.sendMessage("§7The matchmaking server may be experiencing issues.");
                        player.sendMessage("§7Please try again later or contact an administrator.");
                    }

                    plugin.getLogger().warning("Player " + playerUUID + " timed out in queue after " + QUEUE_TIMEOUT + "ms");

                    // Try to send cancel to server
                    try {
                        JsonObject message = new JsonObject();
                        message.addProperty("type", "cancel_queue");
                        sendJson(message);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to send cancel message for timed out player: " + e.getMessage());
                    }
                }
            }
        }, QUEUE_TIMEOUT / 50); // Convert milliseconds to ticks (20 ticks per second)
    }

    /**
     * Removes a player from matchmaking queue
     */
    public void dequeuePlayer(Player player) {
        try {
            JsonObject message = new JsonObject();
            message.addProperty("type", "cancel_queue");

            // Remove player from queued set and timestamps
            queuedPlayers.remove(player.getUniqueId());
            queueTimestamps.remove(player.getUniqueId());

            sendJson(message);
            player.sendMessage("§cYou have been removed from the matchmaking queue!");
            plugin.getLogger().info("Player " + player.getName() + " removed from queue");

        } catch (Exception e) {
            player.sendMessage("§c§lError!");
            player.sendMessage("§cFailed to leave queue: " + e.getMessage());
            plugin.getLogger().severe("Failed to dequeue player " + player.getName() + ": " + e.getMessage());

            // Still remove locally even if server communication fails
            queuedPlayers.remove(player.getUniqueId());
            queueTimestamps.remove(player.getUniqueId());
        }
    }

    /**
     * Starts a direct duel between two players (casual, no matchmaking server involved)
     * @param matchId The match ID
     * @param player1 First player
     * @param player2 Second player
     */
    public void startDirectDuel(String matchId, Player player1, Player player2) {
        plugin.getLogger().info("Starting direct duel between " + player1.getName() + " and " + player2.getName());
        startMatch(matchId, player1, player2, QueueType.CASUAL);
    }

    /**
     * Checks if a player is currently in the matchmaking queue
     * @param playerUUID The player's UUID
     * @return true if the player is queued, false otherwise
     */
    public boolean isPlayerQueued(UUID playerUUID) {
        return queuedPlayers.contains(playerUUID);
    }

    /**
     * Removes a player from the queue (called when player disconnects)
     * @param playerUUID The player's UUID
     */
    public void removePlayerFromQueue(UUID playerUUID) {
        if (queuedPlayers.remove(playerUUID)) {
            queueTimestamps.remove(playerUUID);
            plugin.getLogger().info("Removed disconnected player from queue: " + playerUUID);

            // Try to notify server
            try {
                JsonObject message = new JsonObject();
                message.addProperty("type", "cancel_queue");
                sendJson(message);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to notify server of player disconnect from queue: " + e.getMessage());
            }
        }
    }

    /**
     * Clears all queue state (used on plugin disable or server errors)
     */
    public void clearAllQueues() {
        int count = queuedPlayers.size();
        queuedPlayers.clear();
        queueTimestamps.clear();
        if (count > 0) {
            plugin.getLogger().info("Cleared " + count + " player(s) from queue");
        }
    }

    /**
     * Starts a match between two players
     */
    private void startMatch(String matchId, Player player1, Player player2, QueueType queueType) {
        // Create arena instance with automatically selected random map
        ArenaInstance arena = plugin.getMapManager().createRandomArena();

        if (arena == null) {
            plugin.getLogger().severe("Failed to create arena for match: " + matchId);
            player1.sendMessage("§cFailed to create arena! Make sure map files exist in plugins/macebattles/structures/");
            player2.sendMessage("§cFailed to create arena! Make sure map files exist in plugins/macebattles/structures/");
            return;
        }

        // Get the map name from the arena ID (format: mapname_timestamp)
        String selectedMap = arena.getId().split("_")[0];

        // Create active match with queue type
        ActiveMatch match = new ActiveMatch(matchId, player1.getUniqueId(), player2.getUniqueId(), arena, queueType);
        activeMatches.put(matchId, match);

        // Store original spawn locations
        match.setOriginalLocation(player1.getUniqueId(), player1.getLocation());
        match.setOriginalLocation(player2.getUniqueId(), player2.getLocation());

        // Teleport players to arena
        Map<Integer, Player> players = new HashMap<>();
        players.put(0, player1);
        players.put(1, player2);
        plugin.getMapManager().teleportPlayersToArena(players, arena);

        // Give starting gear
        giveStartingGear(player1, queueType);
        giveStartingGear(player2, queueType);

        // Notify players with queue type (send only ONCE here, not in round start)
        String matchType = queueType == QueueType.CASUAL ? "§eCasual" : "§6Ranked";
        player1.sendMessage("§a" + matchType + " §amatch found! Arena: " + selectedMap);
        player2.sendMessage("§a" + matchType + " §amatch found! Arena: " + selectedMap);

        plugin.getLogger().info("Started " + queueType + " match " + matchId + " with " + player1.getName() + " vs " + player2.getName() + " on map: " + selectedMap);

        // Start the 3 round match (round start message will be sent only in startRounds, not here)
        startRounds(match);
    }

    /**
     * Starts the 3 round match system
     */
    private void startRounds(ActiveMatch match) {
        // Safety check: Make sure match is still active
        if (!activeMatches.containsKey(match.getMatchId())) {
            plugin.getLogger().info("Attempted to start round for completed match " + match.getMatchId() + ", ignoring");
            return;
        }

        // Reset processing flag for new round
        match.setProcessingRoundEnd(false);

        match.startNextRound();

        Player player1 = Bukkit.getPlayer(match.getPlayer1UUID());
        Player player2 = Bukkit.getPlayer(match.getPlayer2UUID());

        if (player1 != null && player2 != null) {
            // Clear inventory and give fresh gear
            player1.getInventory().clear();
            player1.getInventory().setArmorContents(null);
            player2.getInventory().clear();
            player2.getInventory().setArmorContents(null);

            giveStartingGear(player1, match.getQueueType());
            giveStartingGear(player2, match.getQueueType());

            // Reset health, hunger, and other stats
            player1.setHealth(20.0);
            player1.setFoodLevel(20);
            player1.setSaturation(20.0f);
            player1.setFireTicks(0);
            player1.setFallDistance(0);

            player2.setHealth(20.0);
            player2.setFoodLevel(20);
            player2.setSaturation(20.0f);
            player2.setFireTicks(0);
            player2.setFallDistance(0);

            // Teleport back to spawn points in case they moved
            ArenaInstance arena = match.getArena();
            player1.teleport(arena.getSpawnPoint(0));
            player2.teleport(arena.getSpawnPoint(1));

            // Show round start message (send only ONCE per round)
            int currentRound = match.getCurrentRound();
            int player1Score = match.getScore(match.getPlayer1UUID());
            int player2Score = match.getScore(match.getPlayer2UUID());
            player1.sendMessage("§e§l========================================");
            player1.sendMessage("§6§l             ROUND " + currentRound);
            player1.sendMessage("§7Score: §a" + player1Score + " §7- §c" + player2Score + " §8(First to 3)");
            player1.sendMessage("§e§l========================================");

            player2.sendMessage("§e§l========================================");
            player2.sendMessage("§6§l             ROUND " + currentRound);
            player2.sendMessage("§7Score: §a" + player2Score + " §7- §c" + player1Score + " §8(First to 3)");
            player2.sendMessage("§e§l========================================");
        }

        // Schedule round timeout after 3 minutes (in case nobody dies)
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> endRoundByTimeout(match), 180 * 20L).getTaskId(); // 3 minutes

        roundTimers.put(match.getMatchId(), taskId);
    }

    /**
     * Ends round by timeout (no death occurred)
     */
    private void endRoundByTimeout(ActiveMatch match) {
        // Safety check: Make sure match is still active
        if (!activeMatches.containsKey(match.getMatchId())) {
            plugin.getLogger().info("Timeout triggered for completed match " + match.getMatchId() + ", ignoring");
            return;
        }

        Player player1 = Bukkit.getPlayer(match.getPlayer1UUID());
        Player player2 = Bukkit.getPlayer(match.getPlayer2UUID());

        if (player1 == null || player2 == null) {
            endMatch(match);
            return;
        }

        // Determine winner by health
        UUID roundWinner = null;
        if (player1.getHealth() > player2.getHealth()) {
            roundWinner = match.getPlayer1UUID();
        } else if (player2.getHealth() > player1.getHealth()) {
            roundWinner = match.getPlayer2UUID();
        }
        // If health is equal, it's a draw and no points awarded

        if (roundWinner != null) {
            match.addRoundWin(roundWinner);
            Player winner = Bukkit.getPlayer(roundWinner);
            player1.sendMessage("§e§l========== ROUND " + match.getCurrentRound() + " ENDED ==========");
            player2.sendMessage("§e§l========== ROUND " + match.getCurrentRound() + " ENDED ==========");
            player1.sendMessage("§7Time limit reached! Winner: §a" + (winner != null ? winner.getName() : "Unknown"));
            player2.sendMessage("§7Time limit reached! Winner: §a" + (winner != null ? winner.getName() : "Unknown"));
        } else {
            player1.sendMessage("§e§l========== ROUND " + match.getCurrentRound() + " ENDED ==========");
            player2.sendMessage("§e§l========== ROUND " + match.getCurrentRound() + " ENDED ==========");
            player1.sendMessage("§7Time limit reached! Round ended in a §edraw§7!");
            player2.sendMessage("§7Time limit reached! Round ended in a §edraw§7!");
        }

        int player1Score = match.getScore(match.getPlayer1UUID());
        int player2Score = match.getScore(match.getPlayer2UUID());

        player1.sendMessage("§7Score: §a" + player1Score + " §7- §c" + player2Score);
        player2.sendMessage("§7Score: §a" + player2Score + " §7- §c" + player1Score);
        player1.sendMessage("");
        player2.sendMessage("");

        // Check if match is complete (first to 3 wins)
        if (player1Score >= 3 || player2Score >= 3) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> endMatch(match), 3 * 20L); // 3-second delay before ending match
        } else {
            // Start next round
            Bukkit.getScheduler().runTaskLater(plugin, () -> startRounds(match), 5 * 20L); // 5-second delay between rounds
        }
    }

    /**
     * Cancels the round timer for a match
     */
    public void cancelRoundTimer(String matchId) {
        Integer taskId = roundTimers.remove(matchId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Starts the next round (called by death listener)
     */
    public void startNextRound(ActiveMatch match) {
        // Safety check: Make sure match is still active
        if (!activeMatches.containsKey(match.getMatchId())) {
            plugin.getLogger().info("Attempted to start next round for completed match " + match.getMatchId() + ", ignoring");
            return;
        }

        // Small delay before starting next round
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Double-check match is still active after delay
            if (activeMatches.containsKey(match.getMatchId())) {
                startRounds(match);
            } else {
                plugin.getLogger().info("Match " + match.getMatchId() + " ended during round delay");
            }
        }, 5 * 20L); // 5-second delay
    }

    /**
     * Ends the match early when someone wins 2 rounds
     */
    public void endMatchEarly(ActiveMatch match) {
        // Safety check: Make sure match is still active
        if (!activeMatches.containsKey(match.getMatchId())) {
            plugin.getLogger().info("Attempted to end already completed match " + match.getMatchId() + ", ignoring");
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Double-check match is still active after delay
            if (activeMatches.containsKey(match.getMatchId())) {
                endMatch(match);
            } else {
                plugin.getLogger().info("Match " + match.getMatchId() + " already ended");
            }
        }, 3 * 20L); // 3-second delay
    }

    /**
     * Ends the match and broadcasts results
     */
    private void endMatch(ActiveMatch match) {
        // Cancel any active round timer
        cancelRoundTimer(match.getMatchId());

        Player player1 = Bukkit.getPlayer(match.getPlayer1UUID());
        Player player2 = Bukkit.getPlayer(match.getPlayer2UUID());

        int player1Score = match.getScore(match.getPlayer1UUID());
        int player2Score = match.getScore(match.getPlayer2UUID());

        // Determine winner
        UUID winner = player1Score > player2Score ? match.getPlayer1UUID() :
                      (player2Score > player1Score ? match.getPlayer2UUID() : null);

        // Notify players
        if (player1 != null) {
            player1.sendMessage("§e§l========================================");
            player1.sendMessage("§6§l          MATCH ENDED");
            player1.sendMessage("§7Final Score: §a" + player1Score + " §7- §c" + player2Score);
            if (winner != null) {
                player1.sendMessage(winner.equals(player1.getUniqueId()) ? "§a§l         YOU WIN!" : "§c§l         YOU LOSE!");
            } else {
                player1.sendMessage("§e§l          DRAW!");
            }
            player1.sendMessage("§e§l========================================");
        }

        if (player2 != null) {
            player2.sendMessage("§e§l========================================");
            player2.sendMessage("§6§l          MATCH ENDED");
            player2.sendMessage("§7Final Score: §a" + player2Score + " §7- §c" + player1Score);
            if (winner != null) {
                player2.sendMessage(winner.equals(player2.getUniqueId()) ? "§a§l         YOU WIN!" : "§c§l         YOU LOSE!");
            } else {
                player2.sendMessage("§e§l          DRAW!");
            }
            player2.sendMessage("§e§l========================================");
        }

        // Only send results to matchmaking server for RANKED matches
        if (match.getQueueType() == QueueType.RANKED) {
            // Server now supports first-to-3 format directly (3-5 total rounds)
            // Send actual scores: 3-0, 3-1, 3-2, 2-3, 1-3, 0-3

            JsonObject results = new JsonObject();
            results.addProperty("type", "match_result");
            results.addProperty("match_uuid", match.getMatchId());
            results.addProperty("player1_uuid", match.getPlayer1UUID().toString());
            results.addProperty("player2_uuid", match.getPlayer2UUID().toString());
            results.addProperty("player1_rounds", player1Score);
            results.addProperty("player2_rounds", player2Score);

            try {
                sendJson(results);
                plugin.getLogger().info("Ranked match " + match.getMatchId() + " completed. " +
                    "Score: " + player1Score + "-" + player2Score + " - Results sent to server");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to send match results to server: " + e.getMessage());

                // Notify players of rating update failure
                if (player1 != null) {
                    player1.sendMessage("§c§lWarning: Rating update may have failed!");
                    player1.sendMessage("§7Contact an administrator if your rating doesn't update.");
                }
                if (player2 != null) {
                    player2.sendMessage("§c§lWarning: Rating update may have failed!");
                    player2.sendMessage("§7Contact an administrator if your rating doesn't update.");
                }
            }
        } else {
            plugin.getLogger().info("Casual match " + match.getMatchId() + " completed. Winner: " + winner + " - No results sent");
        }

        // IMPORTANT: Remove match from active matches IMMEDIATELY to prevent any further round processing
        String matchId = match.getMatchId();
        activeMatches.remove(matchId);
        cancelRoundTimer(matchId);
        plugin.getLogger().info("Match " + matchId + " removed from active matches");

        // Teleport players back to spawn
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Handle player 1
            if (player1 != null) {
                // Clear inventory
                player1.getInventory().clear();
                player1.getInventory().setArmorContents(null);

                Location spawn1 = match.getOriginalLocation(match.getPlayer1UUID());
                if (spawn1 != null) {
                    player1.teleport(spawn1);
                    plugin.getLogger().info("Teleported " + player1.getName() + " to original spawn after match.");
                } else {
                    player1.teleport(player1.getWorld().getSpawnLocation());
                    plugin.getLogger().info("Teleported " + player1.getName() + " to world spawn after match.");
                }
                player1.sendMessage("§aYou have been returned to spawn!");
            } else {
                // Player is offline, store pending teleport
                plugin.getLogger().info("Player " + match.getPlayer1UUID() + " is offline, storing pending teleport after match.");
                plugin.getDuelsMenu().addPendingTeleport(match.getPlayer1UUID(), match.getOriginalLocation(match.getPlayer1UUID()));
            }

            // Handle player 2
            if (player2 != null) {
                player2.getInventory().clear();
                player2.getInventory().setArmorContents(null);

                Location spawn2 = match.getOriginalLocation(match.getPlayer2UUID());
                if (spawn2 != null) {
                    player2.teleport(spawn2);
                    plugin.getLogger().info("Teleported " + player2.getName() + " to original spawn after match.");
                } else {
                    player2.teleport(player2.getWorld().getSpawnLocation());
                    plugin.getLogger().info("Teleported " + player2.getName() + " to world spawn after match.");
                }
                player2.sendMessage("§aYou have been returned to spawn!");
            } else {
                plugin.getLogger().info("Player " + match.getPlayer2UUID() + " is offline, storing pending teleport after match.");
                plugin.getDuelsMenu().addPendingTeleport(match.getPlayer2UUID(), match.getOriginalLocation(match.getPlayer2UUID()));
            }

            // Clean up arena
            plugin.getMapManager().removeArenaInstance(match.getArena().getId());
        }, 5 * 20L); // 5-second delay
    }

    /**
     * Gets an active match by player UUID
     */
    public ActiveMatch getMatchByPlayer(UUID playerUUID) {
        for (ActiveMatch match : activeMatches.values()) {
            if (match.getPlayer1UUID().equals(playerUUID) || match.getPlayer2UUID().equals(playerUUID)) {
                return match;
            }
        }
        return null;
    }

    /**
     * Gives a player their starting gear for the match
     */
    private void giveStartingGear(Player player, QueueType queueType) {
        KitManager kitManager = plugin.getKitManager();
        String kitName = queueType == QueueType.RANKED ? "ranked" : "casual";
        if (kitManager != null && kitManager.kitExists(kitName)) {
            kitManager.applyKit(player, kitName);
        } else {
            // Fallback to default gear
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            // Diamond Helmet (Unbreakable)
            ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
            ItemMeta helmetMeta = helmet.getItemMeta();
            assert helmetMeta != null;
            helmetMeta.setUnbreakable(true);
            helmet.setItemMeta(helmetMeta);
            player.getInventory().setHelmet(helmet);

            // Diamond Chestplate (Unbreakable)
            ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
            ItemMeta chestplateMeta = chestplate.getItemMeta();
            assert chestplateMeta != null;
            chestplateMeta.setUnbreakable(true);
            chestplate.setItemMeta(chestplateMeta);
            player.getInventory().setChestplate(chestplate);

            // Diamond Leggings (Unbreakable)
            ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
            ItemMeta leggingsMeta = leggings.getItemMeta();
            assert leggingsMeta != null;
            leggingsMeta.setUnbreakable(true);
            leggings.setItemMeta(leggingsMeta);
            player.getInventory().setLeggings(leggings);

            // Diamond Boots (Unbreakable + Feather Falling 4)
            ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
            ItemMeta bootsMeta = boots.getItemMeta();
            assert bootsMeta != null;
            bootsMeta.setUnbreakable(true);
            bootsMeta.addEnchant(Enchantment.FEATHER_FALLING, 3, true);
            boots.setItemMeta(bootsMeta);
            player.getInventory().setBoots(boots);

            // Mace (Unbreakable + Wind Burst 1)
            ItemStack mace = new ItemStack(Material.MACE);
            ItemMeta maceMeta = mace.getItemMeta();
            assert maceMeta != null;
            maceMeta.setUnbreakable(true);
            maceMeta.addEnchant(Enchantment.WIND_BURST, 1, true);
            mace.setItemMeta(maceMeta);
            player.getInventory().addItem(mace);

            // Stone Sword (Unbreakable)
            ItemStack sword = new ItemStack(Material.STONE_SWORD);
            ItemMeta swordMeta = sword.getItemMeta();
            assert swordMeta != null;
            swordMeta.setUnbreakable(true);
            sword.setItemMeta(swordMeta);
            player.getInventory().addItem(sword);

            // Shield (Unbreakable)
            ItemStack shield = new ItemStack(Material.SHIELD);
            ItemMeta shieldMeta = shield.getItemMeta();
            assert shieldMeta != null;
            shieldMeta.setUnbreakable(true);
            shield.setItemMeta(shieldMeta);
            player.getInventory().addItem(shield);

            // 16 Golden Apples
            ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 16);
            player.getInventory().addItem(goldenApples);

            // 128 Wind Charges
            ItemStack windCharges = new ItemStack(Material.WIND_CHARGE, 64);
            player.getInventory().addItem(windCharges);
            ItemStack windCharges2 = new ItemStack(Material.WIND_CHARGE, 64);
            player.getInventory().addItem(windCharges2);
        }
    }


    /**
     * Sends data to the matchmaking server
     */
    public void sendData(String data) {
        try {
            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.send(data);
            } else {
                plugin.getLogger().warning("Cannot send data: WebSocket not connected");
                throw new IllegalStateException("WebSocket is not connected");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send data to matchmaking server: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Sends JSON data to the matchmaking server
     */
    public void sendJson(JsonObject jsonData) {
        try {
            String jsonString = gson.toJson(jsonData);
            sendData(jsonString);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send JSON to matchmaking server: " + e.getMessage());
            plugin.getLogger().severe("  JSON data: " + jsonData.toString());
            throw e;
        }
    }

    /**
     * Checks if the WebSocket is connected
     */
    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    /**
     * Closes the WebSocket connection
     */
    public void disconnect() {
        try {
            // Stop automatic reconnection
            stopReconnectionTask();

            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.close();
                plugin.getLogger().info("Disconnected from matchmaking server");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error while disconnecting from matchmaking server: " + e.getMessage());
        }
    }

}
