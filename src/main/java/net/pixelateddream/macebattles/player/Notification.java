package net.pixelateddream.macebattles.player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import net.pixelateddream.macebattles.Macebattles;
import net.pixelateddream.macebattles.util.PlayerJoinEventHook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public record Notification(UUID uuid, ArrayList<UUID> recipients, String message) {
    private static Macebattles plugin; // Make plugin static

    public Notification {
        PlayerJoinEventHook.addJoinEvent(Notification::checkIfPlayerNotifiedOfAll);
    }

    public static void setPlugin(Macebattles pluginInstance) {
        plugin = pluginInstance;
    }

    public void addRecipients(ArrayList<UUID> newRecipients) {
        recipients.addAll(newRecipients);
    }

    public void addRecipient(UUID recipient) {
        recipients.add(recipient);
    }

    public void recipientNotified(UUID recipient) {
        recipients.remove(recipient);
    }

    public void sendAll() {
        List<Player> onlineRecipients = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : onlineRecipients) {
            if (recipients.contains(player.getUniqueId())) {
                displayText(message, player);
                recipientNotified(player.getUniqueId());
            }
        }
        if (plugin != null) {
            plugin.addNotification(this);
        }
    }

    public void send(UUID player) {
        Player targetPlayer = Bukkit.getPlayer(player);
        boolean playerOnline = targetPlayer != null && targetPlayer.isOnline();
        if (playerOnline) {
            displayText(message, targetPlayer);
            recipientNotified(player);
        } else {
            // Only add the notification for the sole player to avoid sending to unintended recipients
            plugin.addNotification(new Notification(this.uuid, new ArrayList<>(Collections.singletonList(player)), this.message));
        }
    }

    private static final Map<UUID, Long> notificationCooldowns = new HashMap<>();
    private static final long COOLDOWN_PERIOD = 5000; // 5 seconds in milliseconds
    private static final Set<UUID> notifiedPlayers = new HashSet<>(); // Track players who have been notified

    private static void checkIfPlayerNotifiedOfAll(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check if the player has already been notified
        if (notifiedPlayers.contains(playerId)) {
            return; // Exit if the player has already been notified
        }

        Map<UUID, Notification> pendingNotifications = plugin.getAllNotifications();

        if (pendingNotifications.containsKey(playerId)) {
            long currentTime = System.currentTimeMillis();
            long lastNotificationTime = notificationCooldowns.getOrDefault(playerId, 0L);

            // Iterate through notifications and handle cooldown
            for (Notification notification : pendingNotifications.values()) {
                // Check if the notification includes the target player as a recipient
                if (!notification.recipients.contains(playerId)) {
                    continue; // Skip this notification if the player is not a recipient
                }

                if (currentTime - lastNotificationTime >= COOLDOWN_PERIOD) {
                    notification.send(playerId);
                    notificationCooldowns.put(playerId, currentTime);
                    currentTime = System.currentTimeMillis(); // Update current time after sending
                } else {
                    break; // Exit if cooldown has not passed
                }
            }

            // Mark the player as notified after processing all notifications
            notifiedPlayers.add(playerId);
        }
    }

    private void displayText(String message, Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
    }
}
