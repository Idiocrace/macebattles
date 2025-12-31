package net.pixelateddream.macebattles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapManager {
    private final Macebattles plugin;
    private final StructureManager structureManager;
    private final Map<String, ArenaInstance> activeArenas;

    public MapManager(Macebattles plugin) {
        this.plugin = plugin;
        this.structureManager = Bukkit.getStructureManager();
        this.activeArenas = new HashMap<>();

        // Create structures directory if it doesn't exist
        File structuresDir = new File(plugin.getDataFolder(), "structures");
        if (!structuresDir.exists()) {
            structuresDir.mkdirs();
            plugin.getLogger().info("Created structures directory at: " + structuresDir.getPath());
        }
    }

    /**
     * Gets all available map files from the structures directory
     * @return List of map names (without .nbt extension)
     */
    public List<String> getAvailableMaps() {
        List<String> maps = new ArrayList<>();
        File structuresDir = new File(plugin.getDataFolder(), "structures");

        if (!structuresDir.exists()) {
            plugin.getLogger().warning("Structures directory does not exist!");
            return maps;
        }

        File[] files = structuresDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".nbt"));
        if (files != null) {
            for (File file : files) {
                String mapName = file.getName().replaceAll("\\.nbt$", "");
                maps.add(mapName);
            }
        }

        plugin.getLogger().info("Found " + maps.size() + " map(s): " + maps);
        return maps;
    }

    /**
     * Gets a random map from available maps
     * @return Random map name, or null if no maps available
     */
    public String getRandomMap() {
        List<String> maps = getAvailableMaps();
        if (maps.isEmpty()) {
            plugin.getLogger().warning("No maps found in structures directory!");
            return null;
        }
        Random random = new Random();
        return maps.get(random.nextInt(maps.size()));
    }

    /**
     * Loads a structure from an NBT file
     * @param structureName The name of the structure file (without .nbt extension)
     * @return The loaded structure, or null if failed
     */
    public Structure loadStructure(String structureName) throws IOException {
        File structureFile = new File(plugin.getDataFolder(), "structures/" + structureName + ".nbt");

        if (!structureFile.exists()) {
            plugin.getLogger().warning("Structure file not found: " + structureFile.getPath());
            return null;
        }

        Structure structure = structureManager.loadStructure(structureFile);
        return structure;
    }

    /**
     * Creates an arena instance at a specific location
     * @param world The world to create the arena in
     * @param baseLocation The base location for the arena
     * @param structureName The structure to load
     * @param spawnPoints Relative spawn points from the base location
     * @return ArenaInstance with spawn locations
     */
    public ArenaInstance createArenaInstance(World world, Location baseLocation,
                                             String structureName, Map<Integer, BlockVector> spawnPoints) {
        try {
            Structure structure = loadStructure(structureName);
            if (structure == null) {
                return null;
            }

            // Place the structure at the base location
            structure.place(baseLocation, true, StructureRotation.NONE,
                    Mirror.NONE, 0, 1.0f, new Random());

            // Calculate absolute spawn locations
            String arenaId = structureName + "_" + System.currentTimeMillis();
            ArenaInstance instance = new ArenaInstance(arenaId, baseLocation);

            for (Map.Entry<Integer, BlockVector> entry : spawnPoints.entrySet()) {
                BlockVector relative = entry.getValue();
                Location spawnLoc = baseLocation.clone().add(relative.getX(), relative.getY(), relative.getZ());
                instance.addSpawnPoint(entry.getKey(), spawnLoc);
            }

            activeArenas.put(arenaId, instance);
            return instance;

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load structure: " + e.getMessage());
            return null;
        }
    }

    /**
     * Teleports players to their designated spawn points in an arena
     * @param players Map of spawn index to player
     * @param arenaInstance The arena instance
     */
    public void teleportPlayersToArena(Map<Integer, Player> players, ArenaInstance arenaInstance) {
        for (Map.Entry<Integer, Player> entry : players.entrySet()) {
            int spawnIndex = entry.getKey();
            Player player = entry.getValue();
            Location spawnPoint = arenaInstance.getSpawnPoint(spawnIndex);

            if (spawnPoint != null) {
                player.teleport(spawnPoint);
                plugin.getLogger().info("Teleported " + player.getName() + " to spawn point " + spawnIndex);
            } else {
                plugin.getLogger().warning("No spawn point " + spawnIndex + " for arena " + arenaInstance.getId());
            }
        }
    }

    /**
     * Gets an active arena instance by ID
     * @param arenaId The arena ID
     * @return The arena instance, or null if not found
     */
    public ArenaInstance getArenaInstance(String arenaId) {
        return activeArenas.get(arenaId);
    }

    /**
     * Removes an arena instance from tracking and cleans up the structure in the world
     * @param arenaId The arena ID to remove
     */
    public void removeArenaInstance(String arenaId) {
        ArenaInstance instance = activeArenas.remove(arenaId);
        if (instance != null) {
            // Attempt to clear the structure area (replace with air)
            Location base = instance.getBaseLocation();
            // For now, clear a 32x32x32 cube centered on base (customize as needed)
            int radius = 16;
            World world = base.getWorld();
            int bx = base.getBlockX();
            int by = base.getBlockY();
            int bz = base.getBlockZ();
            for (int x = bx - radius; x <= bx + radius; x++) {
                for (int y = by - 1; y <= by + 31; y++) {
                    for (int z = bz - radius; z <= bz + radius; z++) {
                        world.getBlockAt(x, y, z).setType(Material.AIR, false);
                    }
                }
            }
            plugin.getLogger().info("Arena instance " + arenaId + " removed and area cleared.");
        } else {
            plugin.getLogger().warning("Tried to remove non-existent arena instance: " + arenaId);
        }
    }

    /**
     * Gets all active arena instances
     * @return Map of arena IDs to instances
     */
    public Map<String, ArenaInstance> getActiveArenas() {
        return new HashMap<>(activeArenas);
    }

    /**
     * Scans the structure area for vault blocks and uses them as spawn points
     * @param baseLocation The base location where the structure was placed
     * @param structure The structure that was placed
     * @return Map of spawn point index to location
     */
    private Map<Integer, Location> detectSpawnPoints(Location baseLocation, Structure structure) {
        Map<Integer, Location> spawnPoints = new HashMap<>();

        // Get structure size
        BlockVector size = structure.getSize();
        plugin.getLogger().info("Scanning structure of size: " + size.getX() + "x" + size.getY() + "x" + size.getZ() + " for vault blocks");

        int spawnIndex = 0;

        // Scan the entire structure area for vault blocks
        for (int x = 0; x <= size.getX(); x++) {
            for (int y = 0; y <= size.getY(); y++) {
                for (int z = 0; z <= size.getZ(); z++) {
                    Location checkLoc = baseLocation.clone().add(x, y, z);
                    Block block = checkLoc.getBlock();

                    if (block.getType() == Material.VAULT) {
                        // Found a vault block - this is a spawn point
                        // Spawn player 1 block above the vault
                        Location spawnLoc = checkLoc.clone().add(0.5, 1, 0.5); // Center of block, 1 block up
                        spawnPoints.put(spawnIndex, spawnLoc);
                        plugin.getLogger().info("Found spawn point #" + spawnIndex + " at relative position (" + x + ", " + y + ", " + z + ")");
                        spawnIndex++;
                    }
                }
            }
        }

        if (spawnPoints.isEmpty()) {
            plugin.getLogger().warning("No vault blocks found in structure! Using default spawn points.");
            // Fallback to default positions if no vaults found
            spawnPoints.put(0, baseLocation.clone().add(5.5, 1, 5.5));
            spawnPoints.put(1, baseLocation.clone().add(-4.5, 1, -4.5));
        } else {
            plugin.getLogger().info("Detected " + spawnPoints.size() + " spawn point(s) from vault blocks");
        }

        return spawnPoints;
    }

    /**
     * Creates an arena instance with automatic spawn point detection
     * @param world The world to create the arena in
     * @param baseLocation The base location for the arena
     * @param structureName The structure to load
     * @return ArenaInstance with spawn locations
     */
    public ArenaInstance createArenaInstanceWithDetection(World world, Location baseLocation, String structureName) {
        try {
            Structure structure = loadStructure(structureName);
            if (structure == null) {
                return null;
            }

            // Place the structure at the base location
            structure.place(baseLocation, true, StructureRotation.NONE,
                    Mirror.NONE, 0, 1.0f, new Random());

            plugin.getLogger().info("Placed structure '" + structureName + "' at " +
                baseLocation.getBlockX() + ", " + baseLocation.getBlockY() + ", " + baseLocation.getBlockZ());

            // Detect spawn points from vault blocks
            Map<Integer, Location> spawnPoints = detectSpawnPoints(baseLocation, structure);

            // Create arena instance
            String arenaId = structureName + "_" + System.currentTimeMillis();
            ArenaInstance instance = new ArenaInstance(arenaId, baseLocation);

            for (Map.Entry<Integer, Location> entry : spawnPoints.entrySet()) {
                instance.addSpawnPoint(entry.getKey(), entry.getValue());
            }

            activeArenas.put(arenaId, instance);
            return instance;

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load structure: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a random arena instance for matchmaking
     * Automatically picks a random map from the structures directory
     * @return ArenaInstance with spawn locations
     */
    public ArenaInstance createRandomArena() {
        // Get a random map from available maps
        String structureName = getRandomMap();
        if (structureName == null) {
            plugin.getLogger().severe("No maps available to create arena!");
            return null;
        }

        plugin.getLogger().info("Selected random map: " + structureName);
        return createRandomArena(structureName);
    }

    /**
     * Creates a random arena instance for a specific map
     * @param structureName The structure to load
     * @return ArenaInstance with spawn locations
     */
    public ArenaInstance createRandomArena(String structureName) {
        World world = Bukkit.getWorld("world"); // Use default world or configure this
        if (world == null) {
            plugin.getLogger().severe("World not found for arena creation!");
            return null;
        }

        // Generate random location (adjust coordinates as needed)
        Random random = new Random();
        int x = random.nextInt(10000) + 10000; // Between 10000 and 20000
        int z = random.nextInt(10000) + 10000;
        int y = 100; // Fixed height or use world.getHighestBlockYAt(x, z)

        Location baseLocation = new Location(world, x, y, z);

        // Use the new detection method instead of hardcoded spawn points
        return createArenaInstanceWithDetection(world, baseLocation, structureName);
    }

    /**
     * Gets the leaderboard player for a given place (1-based)
     * @param place Leaderboard place (1 = top)
     * @return LeaderboardPlayer or null if not available
     */
    public LeaderboardPlayer getLeaderboardPlayer(int place) {
        List<LeaderboardPlayer> leaderboard = plugin.getMatchmakingListener().getLeaderboard();
        if (leaderboard != null && place > 0 && place <= leaderboard.size()) {
            return leaderboard.get(place - 1);
        }
        return null;
    }
}
