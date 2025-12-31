package net.pixelateddream.macebattles;

import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;

public class ArenaInstance {
    private final String id;
    private final Location baseLocation;
    private final Map<Integer, Location> spawnPoints;

    public ArenaInstance(String id, Location baseLocation) {
        this.id = id;
        this.baseLocation = baseLocation;
        this.spawnPoints = new HashMap<>();
    }

    public void addSpawnPoint(int index, Location location) {
        spawnPoints.put(index, location);
    }

    public Location getSpawnPoint(int index) {
        return spawnPoints.get(index);
    }

    public String getId() {
        return id;
    }

    public Location getBaseLocation() {
        return baseLocation;
    }

    public Map<Integer, Location> getAllSpawnPoints() {
        return new HashMap<>(spawnPoints);
    }
}
