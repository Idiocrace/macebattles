package net.pixelateddream.macebattles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private final Map<UUID, LeaderboardLink> leaderboardLinks = new HashMap<>();

    private int buildNumber = 1;
    private KitManager kitManager;

    @Override
    public void onEnable() {
        // Initialize entity command storage
        this.entityCommandStorage = new EntityCommandStorage(getDataFolder(), getLogger());

        // Load entity command links from disk
        Map<UUID, EntityCommandLink> loadedLinks = entityCommandStorage.loadLinks();
        entityCommandLinks.putAll(loadedLinks);
        getLogger().info("Loaded " + loadedLinks.size() + " entity command links");

        // Load leaderboard links from disk
        Map<UUID, LeaderboardLink> loadedLeaderboardLinks = entityCommandStorage.loadLeaderboardLinks();
        leaderboardLinks.putAll(loadedLeaderboardLinks);
        getLogger().info("Loaded " + loadedLeaderboardLinks.size() + " leaderboard links");

        // Initialize MapManager
        this.mapManager = new MapManager(this);

        // Initialize MatchmakingListener
        String websocketUri = "ws://localhost:8000/ws"; // Matchmaking server endpoint
        this.matchmakingListener = new MatchmakingListener(this, websocketUri);

        // Initialize DuelsMenu
        this.duelsMenu = new DuelsMenu(this);
        kitManager = new KitManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MenuListener(this, duelsMenu), this);
        getServer().getPluginManager().registerEvents(new PlayerDisconnectListener(this), this);
        getServer().getPluginManager().registerEvents(new MatchDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityCommandListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityLinkingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        DuelsCommand duelsCommand = new DuelsCommand(this);
        this.getCommand("duels").setExecutor(duelsCommand);
        this.getCommand("duels").setTabCompleter(duelsCommand);

        EntityCommandCommand entityCommandCommand = new EntityCommandCommand(this);
        this.getCommand("entitycmd").setExecutor(entityCommandCommand);
        this.getCommand("entitycmd").setTabCompleter(entityCommandCommand);

        MbBuildCommand mbBuildCommand = new MbBuildCommand(this);
        this.getCommand("mb").setExecutor(mbBuildCommand);

        // Register BugReportCommand
        BugReportCommand bugReportCommand = new BugReportCommand(getDataFolder());
        this.getCommand("bugreport").setExecutor(bugReportCommand);

        this.getCommand("duels").setExecutor((sender, command, label, args) -> {
            if (args.length == 2 && args[0].equalsIgnoreCase("setkit")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                Player player = (Player) sender;
                if (!player.isOp()) {
                    player.sendMessage("§cOnly operators can use this command.");
                    return true;
                }
                String kitType = args[1].toLowerCase();
                if (!kitType.equals("ranked") && !kitType.equals("casual")) {
                    player.sendMessage("§cUsage: /duels setkit <ranked|casual>");
                    return true;
                }
                kitManager.saveKit(player, kitType);
                return true;
            }
            return false;
        });

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

        // Save leaderboard links to disk
        if (entityCommandStorage != null) {
            entityCommandStorage.saveLeaderboardLinks(leaderboardLinks);
            getLogger().info("Saved " + leaderboardLinks.size() + " leaderboard links");
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
                Properties props = new Properties();
                // Convert YAML to properties (simple parse for buildNumber)
                Scanner scanner = new Scanner(in, "UTF-8");
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

    /**
     * Adds a leaderboard link
     */
    public void addLeaderboardLink(UUID entityUUID, int place) {
        leaderboardLinks.put(entityUUID, new LeaderboardLink(entityUUID, place));
        if (entityCommandStorage != null) entityCommandStorage.saveLeaderboardLinks(leaderboardLinks);
    }

    /**
     * Removes a leaderboard link
     */
    public void removeLeaderboardLink(UUID entityUUID) {
        leaderboardLinks.remove(entityUUID);
        if (entityCommandStorage != null) entityCommandStorage.saveLeaderboardLinks(leaderboardLinks);
    }

    /**
     * Gets a leaderboard link
     */
    public LeaderboardLink getLeaderboardLink(UUID entityUUID) {
        return leaderboardLinks.get(entityUUID);
    }

    /**
     * Gets all leaderboard links
     */
    public List<LeaderboardLink> getAllLeaderboardLinks() {
        return new ArrayList<>(leaderboardLinks.values());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        duelsMenu.handlePlayerJoin(event.getPlayer());
    }
}
