package net.pixelateddream.macebattles;

import java.util.UUID;

/**
 * Represents a link between an entity and a command to execute
 */
public class EntityCommandLink {
    private final UUID entityUUID;
    private final String command;
    private final boolean consoleExecutor; // If true, run as console; if false, run as player

    public EntityCommandLink(UUID entityUUID, String command, boolean consoleExecutor) {
        this.entityUUID = entityUUID;
        this.command = command;
        this.consoleExecutor = consoleExecutor;
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }

    public String getCommand() {
        return command;
    }

    public boolean isConsoleExecutor() {
        return consoleExecutor;
    }

    /**
     * Gets the command with placeholders replaced
     * @param playerName The name of the player who clicked
     * @param playerUUID The UUID of the player who clicked
     * @return The command with placeholders replaced
     */
    public String getReplacedCommand(String playerName, String playerUUID) {
        return command
                .replace("%player%", playerName)
                .replace("%uuid%", playerUUID);
    }
}

