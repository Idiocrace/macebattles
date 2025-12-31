package net.pixelateddream.macebattles.player.friend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles saving and loading entity command links to/from disk
 */
public class FriendsStorageManager {
    private final File dataFile;
    private final File reqDataFile;
    private final Gson gson;
    private final Logger logger;

    public FriendsStorageManager(File dataFolder, Logger logger) {
        
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // Create plugin data folder if it doesn't exist
        if (!dataFolder.exists()) {
            
            dataFolder.mkdirs();
            
        }

        // Player friend file
        this.dataFile = new File(dataFolder, "player_friends.json");
        // Player friend requests file
        this.reqDataFile = new File(dataFolder, "player_requests.json");
        
    }

    /**
     * Saves a friend to disk (atomic for robustness)
     */
    public void saveFriend(UUID partyOneUuid, UUID partyTwoUuid) {
        
        Map<UUID, List<UUID>> friends = loadFriends();

        // Add friend relationship both ways
        friends.putIfAbsent(partyOneUuid, new java.util.ArrayList<>());
        friends.putIfAbsent(partyTwoUuid, new java.util.ArrayList<>());

        if (!friends.get(partyOneUuid).contains(partyTwoUuid)) {
            
            friends.get(partyOneUuid).add(partyTwoUuid);
            
        }
        if (!friends.get(partyTwoUuid).contains(partyOneUuid)) {
            
            friends.get(partyTwoUuid).add(partyOneUuid);
            
        }

        // Save back to disk
        try (Writer writer = new FileWriter(dataFile)) {
            
            gson.toJson(friends, writer);
            logger.info("Saved friends to disk");
            
        } catch (IOException e) {
            
            logger.severe("Failed to save friends file: " + e.getMessage());
            
        }
    }

    /**
     * Loads friends from disk
     */
    public Map<UUID, List<UUID>> loadFriends() {
        
        Map<UUID, List<UUID>> links = new HashMap<>();

        if (!dataFile.exists()) {
            logger.info("No friends file found (normal for first run)");
            return links;
        }

        try (Reader reader = new FileReader(dataFile)) {

            // Loading up the global friends map
            Map<UUID, List<UUID>> globalFriends;
            Type type = new TypeToken<Map<UUID, List<UUID>>>() {}.getType();
            globalFriends = gson.fromJson(reader, type);

            return globalFriends;

        } catch (IOException e) {

            logger.severe("Failed to load friends file: " + e.getMessage());

        }

        return links;
    }
    /**
     * Removes a friend from disk (atomic for robustness)
     */
    public void removeFriend(UUID partyOneUuid, UUID partyTwoUuid) {
        Map<UUID, List<UUID>> friends = loadFriends();

        // Remove friend relationship both ways
        if (friends.containsKey(partyOneUuid)) {
            friends.get(partyOneUuid).remove(partyTwoUuid);
        }
        if (friends.containsKey(partyTwoUuid)) {
            friends.get(partyTwoUuid).remove(partyOneUuid);
        }

        // Save back to disk
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(friends, writer);
            logger.info("Removed friend relationship from disk");
        } catch (IOException e) {
            logger.severe("Failed to remove friend relationship: " + e.getMessage());
        }
    }
    /**
     * Adds a friend request to the cache
     */
    public void addFriendRequest(UUID fromUuid, UUID toUuid) {
        Map<UUID, List<UUID>> friendRequests = loadFriendRequests();

        friendRequests.putIfAbsent(toUuid, new java.util.ArrayList<>());
        if (!friendRequests.get(toUuid).contains(fromUuid)) {
            friendRequests.get(toUuid).add(fromUuid);
        }

        // Save back to disk
        try (Writer writer = new FileWriter(reqDataFile)) {
            gson.toJson(friendRequests, writer);
            logger.info("Saved friend requests to disk");
        } catch (IOException e) {
            logger.severe("Failed to save friend requests file: " + e.getMessage());
        }
    }
    /**
     * Loads friend requests from disk
     */
    public Map<UUID, List<UUID>> loadFriendRequests() {
        Map<UUID, List<UUID>> requests = new HashMap<>();

        if (!reqDataFile.exists()) {
            logger.info("No friend requests file found (normal for first run)");
            return requests;
        }

        try (Reader reader = new FileReader(reqDataFile)) {
            // Loading up the global friend requests map
            Map<UUID, List<UUID>> globalRequests;
            Type type = new TypeToken<Map<UUID, List<UUID>>>() {
            }.getType();
            globalRequests = gson.fromJson(reader, type);

            return globalRequests;

        } catch (IOException e) {
            logger.severe("Failed to load friend requests file: " + e.getMessage());
        }

        return requests;
    }
    /**
     * Removes a friend request from the cache
     */
    public void removeFriendRequest(UUID fromUuid, UUID toUuid) {
        Map<UUID, List<UUID>> friendRequests = loadFriendRequests();
        if (friendRequests.containsKey(toUuid)) {
            friendRequests.get(toUuid).remove(fromUuid);
        }
        // Save back to disk
        try (Writer writer = new FileWriter(reqDataFile)) {
            gson.toJson(friendRequests, writer);
            logger.info("Removed friend request from disk");
        } catch (IOException e) {
            logger.severe("Failed to remove friend request: " + e.getMessage());
        }
    }
}
