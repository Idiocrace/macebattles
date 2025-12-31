package net.pixelateddream.macebattles.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Used for when a notification cannot be delivered immediately (e.g., player offline).
 */
public class NotificationCacheManager {
    private static File cacheFile = null;
    private static Logger logger = null;
    private static Gson gson = null;

    public NotificationCacheManager(File dataFolder, Logger logger) {
        NotificationCacheManager.logger = logger;
        cacheFile = new File(dataFolder, "notifications.json");
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Stores a notification to the cache
     */
    public static void storeNotification(Notification notification) {
        UUID messageId = notification.uuid();
        String message = notification.message();
        ArrayList<UUID> recipients = notification.recipients();

        try {
            Map<String, Map<String, ArrayList<String>>> serializableNotifications = new HashMap<>();

            if (cacheFile.exists()) {
                try (Reader reader = new FileReader(cacheFile)) {
                    serializableNotifications = gson.fromJson(reader, new HashMap<String, Map<String, ArrayList<String>>>() {}.getClass());
                } catch (JsonIOException | JsonSyntaxException | IOException e) {
                    logger.warning("Failed to read existing notifications: " + e.getMessage());
                }
            }

            Map<String, ArrayList<String>> notificationData = new HashMap<>();
            notificationData.put("message", new ArrayList<>(Collections.singletonList(message)));
            notificationData.put("recipients", new ArrayList<>(recipients.stream().map(UUID::toString).toList()));

            serializableNotifications.put(messageId.toString(), notificationData);

            try (Writer writer = new FileWriter(cacheFile)) {
                gson.toJson(serializableNotifications, writer);
            }
        } catch (IOException e) {
            logger.warning("Failed to store notification: " + e.getMessage());
        }
    }

    /**
     * Removes a notification from the cache
     */
    public static void removeNotification(Notification notification) throws NoCacheFileException {
        UUID messageId = notification.uuid();

        if (!cacheFile.exists()) {
            throw new NoCacheFileException("Cache file does not exist.");
        }

        try (Reader reader = new FileReader(cacheFile)) {
            Map<String, Map<String, ArrayList<String>>> serializableNotifications = gson.fromJson(reader, new HashMap<String, Map<String, ArrayList<String>>>() {}.getClass());

            if (serializableNotifications != null) {
                serializableNotifications.remove(messageId.toString());

                try (Writer writer = new FileWriter(cacheFile)) {
                    gson.toJson(serializableNotifications, writer);
                }
            }
        } catch (IOException e) {
            logger.warning("Failed to remove notification: " + e.getMessage());
        }
    }

    /**
     * Gets all cached notifications
     */
    public static Map<UUID, Notification> getAllCachedNotifications() {
        Map<UUID, Notification> notifications = new HashMap<>();

        if (!cacheFile.exists()) {
            return notifications;
        }

        try (Reader reader = new FileReader(cacheFile)) {
            Map<String, Map<String, ArrayList<String>>> serializableNotifications = gson.fromJson(reader, new HashMap<String, Map<String, ArrayList<String>>>() {}.getClass());

            if (serializableNotifications != null) {
                for (Map.Entry<String, Map<String, ArrayList<String>>> entry : serializableNotifications.entrySet()) {
                    UUID messageId = UUID.fromString(entry.getKey());
                    String message = entry.getValue().get("message").getFirst();
                    ArrayList<UUID> recipients = new ArrayList<>();

                    for (String recipient : entry.getValue().get("recipients")) {
                        recipients.add(UUID.fromString(recipient));
                    }

                    notifications.put(messageId, new Notification(messageId, recipients, message));
                }
            }
        } catch (IOException e) {
            logger.warning("Failed to load cached notifications: " + e.getMessage());
        }

        return notifications;
    }

    /**
     * Get one cached notification by its UUID
     */
    public static Notification getCachedNotification(UUID notificationUuid) throws NoCacheFileException {
        if (!cacheFile.exists()) {
            throw new NoCacheFileException("Cache file does not exist.");
        }

        try (Reader reader = new FileReader(cacheFile)) {
            Map<String, Map<String, ArrayList<String>>> serializableNotifications = gson.fromJson(reader, new HashMap<String, Map<String, ArrayList<String>>>() {
            }.getClass());

            if (serializableNotifications != null && serializableNotifications.containsKey(notificationUuid.toString())) {
                Map<String, ArrayList<String>> data = serializableNotifications.get(notificationUuid.toString());
                String message = data.get("message").getFirst();
                ArrayList<UUID> recipients = new ArrayList<>();

                for (String recipient : data.get("recipients")) {
                    recipients.add(UUID.fromString(recipient));
                }

                return new Notification(notificationUuid, recipients, message);
            }
        } catch (IOException e) {
            logger.warning("Failed to load cached notification: " + e.getMessage());
        }

        return null;
    }
}
