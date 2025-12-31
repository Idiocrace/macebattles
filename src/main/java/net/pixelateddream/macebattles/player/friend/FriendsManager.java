package net.pixelateddream.macebattles.player.friend;

import net.pixelateddream.macebattles.Macebattles;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.*;

// Class for mostly just helper functions related to managing friend
// Everything is static/stateless just due to the simple mechanics needed
public class FriendsManager {
    private static Macebattles plugin = new Macebattles();

    public FriendsManager(Macebattles plugin) { FriendsManager.plugin = plugin; }

    // Tidy up function later (organize, simplify, and document)
    public static void addFriend(Player playerOne, Player playerTwo) {
        UUID playerOneUuid = playerOne.getUniqueId();
        UUID playerTwoUuid = playerTwo.getUniqueId();
        plugin.addFriend(playerOneUuid, playerTwoUuid);
    }
    // Tidy up function later (organize, simplify, and document)
    public static void removeFriend(Player playerOne, Player playerTwo) {
        UUID playerOneUuid = playerOne.getUniqueId();
        UUID playerTwoUuid = playerTwo.getUniqueId();
        plugin.removeFriend(playerOneUuid, playerTwoUuid);
    }
    // Tidy up function later (organize, simplify, and document)
    public static List<Player> getFriendsList(Player player) {
        UUID targetPlayerUuid = player.getUniqueId();
        Map<UUID, List<UUID>> globalFriendsData = plugin.getFriendsData();
        List<UUID> rawTargetFriendsList = globalFriendsData.get(targetPlayerUuid);
        List<Player> targetFriendsList = new ArrayList<>(List.of());
        for (UUID friendUuid : rawTargetFriendsList) {
            Player friend = Bukkit.getPlayer(friendUuid);
            targetFriendsList.add(friend);
        }
        return targetFriendsList;
    }

    // Tidy up function later (organize, simplify, and document)
    public static void sendFriendRequest(Player sender, Player receiver) {
        UUID senderUuid = sender.getUniqueId();
        UUID receiverUuid = receiver.getUniqueId();
        plugin.addFriendRequest(senderUuid, receiverUuid);
    }
    // Tidy up function later (organize, simplify, and document)
    public static void removeFriendRequest(Player sender, Player receiver) {
        UUID senderUuid = sender.getUniqueId();
        UUID receiverUuid = receiver.getUniqueId();
        plugin.removeFriendRequest(senderUuid, receiverUuid);
    }
    // Tidy up function later (organize, simplify, and document)
    public static List<Player> getPendingFriendRequests(Player player) {
        UUID targetPlayerUuid = player.getUniqueId();
        Map<UUID, List<UUID>> globalFriendRequestsData = plugin.getFriendRequests();
        List<UUID> rawPendingRequestsList = globalFriendRequestsData.get(targetPlayerUuid);
        List<Player> pendingRequestsList = new ArrayList<>(List.of());
        for (UUID requesterUuid : rawPendingRequestsList) {
            Player requester = Bukkit.getPlayer(requesterUuid);
            pendingRequestsList.add(requester);
        }
        return pendingRequestsList;
    }
    // Tidy up function later (organize, simplify, and document)
    public static void acceptFriendRequest(Player receiver, Player sender) {
        addFriend(receiver, sender);
        removeFriendRequest(sender, receiver);
    }
    // Tidy up function later (organize, simplify, and document)
    public static void denyFriendRequest(Player receiver, Player sender) {
        removeFriendRequest(sender, receiver);
    }

}
