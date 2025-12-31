package net.pixelateddream.macebattles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles saving and loading entity command links to/from disk
 */
public class EntityCommandStorage {
    private final File dataFile;
    private final File leaderboardFile;
    private final Gson gson;
    private final Logger logger;

    public EntityCommandStorage(File dataFolder, Logger logger) {
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // Create plugin data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.dataFile = new File(dataFolder, "entity_commands.json");
        this.leaderboardFile = new File(dataFolder, "leaderboard_links.json");
    }

    /**
     * Saves entity command links to disk
     */
    public void saveLinks(Map<UUID, EntityCommandLink> links) {
        try (Writer writer = new FileWriter(dataFile)) {
            // Convert to serializable format
            Map<String, SerializableLink> serializableLinks = new HashMap<>();
            for (Map.Entry<UUID, EntityCommandLink> entry : links.entrySet()) {
                EntityCommandLink link = entry.getValue();
                SerializableLink sLink = new SerializableLink(
                    entry.getKey().toString(),
                    link.getCommand(),
                    link.isConsoleExecutor()
                );
                serializableLinks.put(entry.getKey().toString(), sLink);
            }

            gson.toJson(serializableLinks, writer);
            logger.info("Saved " + links.size() + " entity command links to disk");
        } catch (IOException e) {
            logger.severe("Failed to save entity command links: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads entity command links from disk
     */
    public Map<UUID, EntityCommandLink> loadLinks() {
        Map<UUID, EntityCommandLink> links = new HashMap<>();

        if (!dataFile.exists()) {
            logger.info("No entity command links file found (this is normal on first run)");
            return links;
        }

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, SerializableLink>>(){}.getType();
            Map<String, SerializableLink> serializableLinks = gson.fromJson(reader, type);

            if (serializableLinks != null) {
                for (Map.Entry<String, SerializableLink> entry : serializableLinks.entrySet()) {
                    try {
                        UUID entityUUID = UUID.fromString(entry.getValue().entityUUID);
                        EntityCommandLink link = new EntityCommandLink(
                            entityUUID,
                            entry.getValue().command,
                            entry.getValue().consoleExecutor
                        );
                        links.put(entityUUID, link);
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in entity command link: " + entry.getKey());
                    }
                }
                logger.info("Loaded " + links.size() + " entity command links from disk");
            }
        } catch (IOException e) {
            logger.severe("Failed to load entity command links: " + e.getMessage());
            e.printStackTrace();
        }

        return links;
    }

    /**
     * Saves leaderboard links to disk
     */
    public void saveLeaderboardLinks(Map<UUID, LeaderboardLink> links) {
        try (Writer writer = new FileWriter(leaderboardFile)) {
            Map<String, Integer> serializableLinks = new HashMap<>();
            for (Map.Entry<UUID, LeaderboardLink> entry : links.entrySet()) {
                serializableLinks.put(entry.getKey().toString(), entry.getValue().getPlace());
            }
            gson.toJson(serializableLinks, writer);
            logger.info("Saved " + links.size() + " leaderboard links to disk");
        } catch (IOException e) {
            logger.severe("Failed to save leaderboard links: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads leaderboard links from disk
     */
    public Map<UUID, LeaderboardLink> loadLeaderboardLinks() {
        Map<UUID, LeaderboardLink> links = new HashMap<>();
        if (!leaderboardFile.exists()) {
            logger.info("No leaderboard links file found (this is normal on first run)");
            return links;
        }
        try (Reader reader = new FileReader(leaderboardFile)) {
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            Map<String, Integer> serializableLinks = gson.fromJson(reader, type);
            if (serializableLinks != null) {
                for (Map.Entry<String, Integer> entry : serializableLinks.entrySet()) {
                    try {
                        UUID entityUUID = UUID.fromString(entry.getKey());
                        LeaderboardLink link = new LeaderboardLink(entityUUID, entry.getValue());
                        links.put(entityUUID, link);
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in leaderboard link: " + entry.getKey());
                    }
                }
                logger.info("Loaded " + links.size() + " leaderboard links from disk");
            }
        } catch (IOException e) {
            logger.severe("Failed to load leaderboard links: " + e.getMessage());
            e.printStackTrace();
        }
        return links;
    }

    /**
     * Serializable representation of an entity command link
     */
    private static class SerializableLink {
        String entityUUID;
        String command;
        boolean consoleExecutor;

        SerializableLink(String entityUUID, String command, boolean consoleExecutor) {
            this.entityUUID = entityUUID;
            this.command = command;
            this.consoleExecutor = consoleExecutor;
        }
    }
}
