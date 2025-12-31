package net.pixelateddream.macebattles.match;

import net.pixelateddream.macebattles.misc.MatchmakingListener;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActiveMatch {
    private final String matchId;
    private final UUID player1UUID;
    private final UUID player2UUID;
    private final ArenaInstance arena;
    private final Map<UUID, Integer> scores;
    private final Map<UUID, Location> originalLocations;
    private int currentRound;
    private final MatchmakingListener.QueueType queueType;
    private boolean processingRoundEnd = false; // Flag to prevent duplicate round endings

    public ActiveMatch(String matchId, UUID player1UUID, UUID player2UUID, ArenaInstance arena, MatchmakingListener.QueueType queueType) {
        this.matchId = matchId;
        this.player1UUID = player1UUID;
        this.player2UUID = player2UUID;
        this.arena = arena;
        this.queueType = queueType;
        this.scores = new HashMap<>();
        this.originalLocations = new HashMap<>();
        this.currentRound = 0;

        // Initialize scores
        scores.put(player1UUID, 0);
        scores.put(player2UUID, 0);
    }

    public String getMatchId() {
        return matchId;
    }

    public UUID getPlayer1UUID() {
        return player1UUID;
    }

    public UUID getPlayer2UUID() {
        return player2UUID;
    }

    public ArenaInstance getArena() {
        return arena;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void startNextRound() {
        currentRound++;
    }

    public void addRoundWin(UUID playerUUID) {
        scores.put(playerUUID, scores.getOrDefault(playerUUID, 0) + 1);
    }

    public int getScore(UUID playerUUID) {
        return scores.getOrDefault(playerUUID, 0);
    }

    public void setOriginalLocation(UUID playerUUID, Location location) {
        originalLocations.put(playerUUID, location);
    }

    public Location getOriginalLocation(UUID playerUUID) {
        return originalLocations.get(playerUUID);
    }

    public MatchmakingListener.QueueType getQueueType() {
        return queueType;
    }

    public boolean isProcessingRoundEnd() {
        return processingRoundEnd;
    }

    public void setProcessingRoundEnd(boolean processing) {
        this.processingRoundEnd = processing;
    }
}

