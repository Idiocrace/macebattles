package net.pixelateddream.macebattles;

import net.pixelateddream.macebattles.commands.*;
import net.pixelateddream.macebattles.entity.*;
import net.pixelateddream.macebattles.match.KitManager;
import net.pixelateddream.macebattles.match.MapManager;
import net.pixelateddream.macebattles.match.MatchDeathListener;
import net.pixelateddream.macebattles.match.PlayerDisconnectListener;
import net.pixelateddream.macebattles.misc.JoinMessage;
import net.pixelateddream.macebattles.misc.MatchmakingListener;
import net.pixelateddream.macebattles.player.NoCacheFileException;
import net.pixelateddream.macebattles.player.Notification;
import net.pixelateddream.macebattles.player.NotificationCacheManager;
import net.pixelateddream.macebattles.player.friend.FriendsStorageManager;
import net.pixelateddream.macebattles.util.PlayerJoinEventHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Macebattles extends JavaPlugin implements Listener {

    private MapManager mapManager;
    private MatchmakingListener matchmakingListener;
    private DuelsMenu duelsMenu;
    private final Map<UUID, Integer> playerRatings = new HashMap<>();

    // Entity command link system
    private final Map<UUID, EntityCommandLink> entityCommandLinks = new HashMap<>();
    private final Map<UUID, String> playersInLinkingMode = new HashMap<>();
    private final Map<UUID, Boolean> linkingModeConsoleExecutor = new HashMap<>();
    private final Set<UUID> playersInUnlinkingMode = new HashSet<>();
    private EntityCommandStorage entityCommandStorage;
    private FriendsStorageManager friendsStorageManager;

    private int buildNumber = 0;
    private KitManager kitManager;

    @Override
    public void onEnable() {
        // Initialize entity command storage
        this.entityCommandStorage = new EntityCommandStorage(getDataFolder(), getLogger());

        // Initialize friends storage
        this.friendsStorageManager = new FriendsStorageManager(getDataFolder(), getLogger());

        // Initialize notification cache manager
        new NotificationCacheManager(getDataFolder(), getLogger());
        Notification.setPlugin(this); // Set static plugin reference in Notification class

        // Load entity command links from disk
        Map<UUID, EntityCommandLink> loadedLinks = entityCommandStorage.loadLinks();
        entityCommandLinks.putAll(loadedLinks);
        getLogger().info("Loaded " + loadedLinks.size() + " entity command links");

        // Initialize MapManager
        this.mapManager = new MapManager(this);

        // Initialize MatchmakingListener
        String websocketUri = "ws://localhost:8000/ws"; // Matchmaking server endpoint
        this.matchmakingListener = new MatchmakingListener(this, websocketUri);

        // Initialize DuelsMenu
        this.duelsMenu = new DuelsMenu(this);
        kitManager = new KitManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MenuListener(duelsMenu), this);
        getServer().getPluginManager().registerEvents(new PlayerDisconnectListener(this), this);
        getServer().getPluginManager().registerEvents(new MatchDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityCommandListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityLinkingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinEventHook(), this);

        // Register commands
        DuelsCommand duelsCommand = new DuelsCommand(this);
        Objects.requireNonNull(this.getCommand("duels")).setExecutor(duelsCommand);
        Objects.requireNonNull(this.getCommand("duels")).setTabCompleter(duelsCommand);

        EntityCommandCommand entityCommandCommand = new EntityCommandCommand(this);
        Objects.requireNonNull(this.getCommand("entitycmd")).setExecutor(entityCommandCommand);
        Objects.requireNonNull(this.getCommand("entitycmd")).setTabCompleter(entityCommandCommand);

        MbBuildCommand mbBuildCommand = new MbBuildCommand(this);
        Objects.requireNonNull(this.getCommand("mb")).setExecutor(mbBuildCommand);

        // Register BugReportCommand
        BugReportCommand bugReportCommand = new BugReportCommand(getDataFolder());
        Objects.requireNonNull(this.getCommand("bugreport")).setExecutor(bugReportCommand);

        // Register FriendsCommand
        FriendsCommand friendsCommand = new FriendsCommand(this);
        Objects.requireNonNull(this.getCommand("friends")).setExecutor(friendsCommand);
        Objects.requireNonNull(this.getCommand("friends")).setTabCompleter(friendsCommand);

        // Register PlayerJoinEvents
        new JoinMessage(this);

        loadBuildNumber();

        getLogger().info("Macebattles plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Save entity command links to disk
        if (entityCommandStorage != null) {
            entityCommandStorage.saveLinks(entityCommandLinks);
            getLogger().info("Saved " + entityCommandLinks.size() + " entity command links");
        }

        // Clear all queues on shutdown
        if (matchmakingListener != null) {
            matchmakingListener.clearAllQueues();
            matchmakingListener.disconnect();
        }

        getLogger().info("Macebattles plugin disabled!");
    }

    private void loadBuildNumber() {
        try {
            // Read buildNumber from plugin.yml inside the jar
            InputStream in = getResource("plugin.yml");
            if (in != null) {
                // Convert YAML to properties (simple parse for buildNumber)
                Scanner scanner = new Scanner(in, StandardCharsets.UTF_8);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.trim().startsWith("buildNumber:")) {
                        String num = line.split(":")[1].trim();
                        buildNumber = Integer.parseInt(num);
                        getLogger().info("Build number loaded from plugin.yml: " + buildNumber);
                        break;
                    }
                }
                scanner.close();
            } else {
                buildNumber = 1;
                getLogger().warning("Could not find buildNumber in plugin.yml; defaulting to 1");
            }
        } catch (Exception e) {
            buildNumber = 1;
            getLogger().warning("Could not load build number from plugin.yml: " + e.getMessage());
        }
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public MatchmakingListener getMatchmakingListener() {
        return matchmakingListener;
    }

    public DuelsMenu getDuelsMenu() {
        return duelsMenu;
    }

    public KitManager getKitManager() { return kitManager; }

    /**
     * Gets a player's cached rating
     */
    public Integer getPlayerRating(UUID playerUUID) {
        return playerRatings.get(playerUUID);
    }

    /**
     * Updates a player's cached rating
     */
    public void setPlayerRating(UUID playerUUID, int rating) {
        playerRatings.put(playerUUID, rating);
    }

    // ========== Notification Cache Methods ==========
    /**
     * Adds a notification to the cache
     */
    public void addNotification(Notification notification) {
        NotificationCacheManager.storeNotification(notification);
    }
    /**
     * Removes a notification from the cache
     */
    public void removeNotification(Notification notification) throws NoCacheFileException {
        NotificationCacheManager.removeNotification(notification);
    }
    /**
     * Gets all notifications cached (should not be necessary for most situations)
     */
    public Map<UUID, Notification> getAllNotifications() {
        return NotificationCacheManager.getAllCachedNotifications();
    }
    /**
     * Gets a cached notification by its ID
     */
    public Notification getNotification(UUID notificationId) throws NoCacheFileException {
        return NotificationCacheManager.getCachedNotification(notificationId);
    }

    // ========== Friends Storage Manager Methods ==========
    /**
     * Gets the friends list for every player
     */
    public Map<UUID, List<UUID>> getFriendsData() {
        return friendsStorageManager.loadFriends();
    }

    /**
     * Adds a friend atomically
     */
    public void addFriend(UUID partyOneUuid, UUID partyTwoUuid) {
        friendsStorageManager.saveFriend(partyOneUuid, partyTwoUuid);
    }
    /**
     * Removes a friend atomically
     */
    public void removeFriend(UUID partyOneUuid, UUID partyTwoUuid) {
        friendsStorageManager.removeFriend(partyOneUuid, partyTwoUuid);
    }
    /**
     * Adds a friend request to the cache
     */
    public void addFriendRequest(UUID fromPlayer, UUID toPlayer) {
        friendsStorageManager.addFriendRequest(fromPlayer, toPlayer);
    }
    /**
     * Removes a friend request from the cache
     */
    public void removeFriendRequest(UUID fromPlayer, UUID toPlayer) {
        friendsStorageManager.removeFriendRequest(fromPlayer, toPlayer);
    }
    /**
     * Gets all friend requests in the cache
     */
    public Map<UUID, List<UUID>> getFriendRequests() {
        return friendsStorageManager.loadFriendRequests();
    }

    // ========== Entity Command Link Methods ==========

    /**
     * Gets an entity command link by entity UUID
     */
    public EntityCommandLink getEntityCommandLink(UUID entityUUID) {
        return entityCommandLinks.get(entityUUID);
    }

    /**
     * Adds a new entity command link
     */
    public void addEntityCommandLink(UUID entityUUID, String command, boolean consoleExecutor) {
        EntityCommandLink link = new EntityCommandLink(entityUUID, command, consoleExecutor);
        entityCommandLinks.put(entityUUID, link);
        getLogger().info("Added entity command link: " + entityUUID + " -> " + command);

        // Auto-save to disk
        if (entityCommandStorage != null) {
            entityCommandStorage.saveLinks(entityCommandLinks);
        }
    }

    /**
     * Removes an entity command link
     */
    public void removeEntityCommandLink(UUID entityUUID) {
        entityCommandLinks.remove(entityUUID);
        getLogger().info("Removed entity command link: " + entityUUID);

        // Auto-save to disk
        if (entityCommandStorage != null) {
            entityCommandStorage.saveLinks(entityCommandLinks);
        }
    }

    /**
     * Gets all entity command links
     */
    public List<EntityCommandLink> getAllEntityCommandLinks() {
        return new ArrayList<>(entityCommandLinks.values());
    }

    /**
     * Puts a player in linking mode
     */
    public void setPlayerLinkingMode(UUID playerUUID, String command, boolean consoleExecutor) {
        playersInLinkingMode.put(playerUUID, command);
        linkingModeConsoleExecutor.put(playerUUID, consoleExecutor);
    }

    /**
     * Checks if a player is in linking mode
     */
    public boolean isPlayerInLinkingMode(UUID playerUUID) {
        return playersInLinkingMode.containsKey(playerUUID);
    }

    /**
     * Gets the command a player is linking
     */
    public String getLinkingCommand(UUID playerUUID) {
        return playersInLinkingMode.get(playerUUID);
    }

    /**
     * Gets whether the linking is console executor
     */
    public boolean isLinkingConsoleExecutor(UUID playerUUID) {
        return linkingModeConsoleExecutor.getOrDefault(playerUUID, false);
    }

    /**
     * Clears a player's linking mode
     */
    public void clearPlayerLinkingMode(UUID playerUUID) {
        playersInLinkingMode.remove(playerUUID);
        linkingModeConsoleExecutor.remove(playerUUID);
    }

    /**
     * Puts a player in unlinking mode
     */
    public void setPlayerUnlinkingMode(UUID playerUUID) {
        playersInUnlinkingMode.add(playerUUID);
    }

    /**
     * Checks if a player is in unlinking mode
     */
    public boolean isPlayerInUnlinkingMode(UUID playerUUID) {
        return playersInUnlinkingMode.contains(playerUUID);
    }

    /**
     * Clears a player's unlinking mode
     */
    public void clearPlayerUnlinkingMode(UUID playerUUID) {
        playersInUnlinkingMode.remove(playerUUID);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        duelsMenu.handlePlayerJoin(event.getPlayer());
    }
}
