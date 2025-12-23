package net.pixelateddream.macebattles;

import java.util.UUID;

/**
 * Represents a link between an entity and a leaderboard place (e.g., 1st, 2nd, 3rd)
 */
public class LeaderboardLink {
    private final UUID entityUUID;
    private final int place; // 1 for first, 2 for second, etc.

    public LeaderboardLink(UUID entityUUID, int place) {
        this.entityUUID = entityUUID;
        this.place = place;
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }

    public int getPlace() {
        return place;
    }
}

