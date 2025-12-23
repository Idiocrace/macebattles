package net.pixelateddream.macebattles;

public class LeaderboardPlayer {
    private final String name;
    private final int rating;
    private final String skin; // Optional: URL or value for skin

    public LeaderboardPlayer(String name, int rating, String skin) {
        this.name = name;
        this.rating = rating;
        this.skin = skin;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public String getSkin() {
        return skin;
    }
}

