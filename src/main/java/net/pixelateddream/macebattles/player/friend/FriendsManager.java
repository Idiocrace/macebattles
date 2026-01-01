package net.pixelateddream.macebattles.player.friend;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsManager {
    private static final Map<UUID, Set<UUID>> friends = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> pendingRequests = new ConcurrentHashMap<>();

    public FriendsManager() {
    }

    public void sendFriendRequest(Player from, Player to) {
        if (from == null || to == null) return;
        pendingRequests.computeIfAbsent(to.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
                .add(from.getUniqueId());
    }

    public void acceptFriendRequest(Player accepter, Player requester) {
        if (accepter == null || requester == null) return;
        // remove pending
        Set<UUID> raw = pendingRequests.get(accepter.getUniqueId());
        if (raw != null) raw.remove(requester.getUniqueId());

        // add to friends both ways
        friends.computeIfAbsent(accepter.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
                .add(requester.getUniqueId());
        friends.computeIfAbsent(requester.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
                .add(accepter.getUniqueId());
    }

    public void denyFriendRequest(Player accepter, Player requester) {
        if (accepter == null || requester == null) return;
        Set<UUID> raw = pendingRequests.get(accepter.getUniqueId());
        if (raw != null) raw.remove(requester.getUniqueId());
    }

    public void removeFriend(Player remover, Player target) {
        if (remover == null || target == null) return;
        Set<UUID> a = friends.get(remover.getUniqueId());
        if (a != null) a.remove(target.getUniqueId());
        Set<UUID> b = friends.get(target.getUniqueId());
        if (b != null) b.remove(remover.getUniqueId());
    }

    public static List<Player> getFriendsList(Player player) {
        if (player == null) return Collections.emptyList();
        Set<UUID> raw = friends.get(player.getUniqueId());
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        List<Player> result = new ArrayList<>();
        for (UUID id : raw) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) result.add(p);
        }
        return result;
    }

    public List<Player> getPendingFriendRequests(Player player) {
        if (player == null) return Collections.emptyList();
        Set<UUID> rawPending = pendingRequests.get(player.getUniqueId());
        if (rawPending == null || rawPending.isEmpty()) return Collections.emptyList();
        List<Player> result = new ArrayList<>();
        for (UUID id : rawPending) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) result.add(p);
        }
        return result;
    }
}