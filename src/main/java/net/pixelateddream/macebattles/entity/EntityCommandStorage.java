package net.pixelateddream.macebattles.entity;

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
