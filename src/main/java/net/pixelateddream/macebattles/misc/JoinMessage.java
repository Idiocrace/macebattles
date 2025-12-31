package net.pixelateddream.macebattles.misc;

import net.pixelateddream.macebattles.Macebattles;
import net.pixelateddream.macebattles.util.PlayerJoinEventHook;

import org.bukkit.configuration.Configuration;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Logger;

public class JoinMessage {
    private final Configuration config;
    private final Logger logger;

    public JoinMessage(Macebattles plugin) {
        this.config = plugin.getConfig();
        this.logger = plugin.getLogger();
        PlayerJoinEventHook.addJoinEvent(this::sendJoinMessage);
    }

    public void sendJoinMessage(PlayerJoinEvent event) {
        if (!config.getBoolean("join-message.enabled")) {
            return;
        }

        String message = config.getString("join-message.message", "0");
        if (message.equals("0")) {
            logger.warning("Join message is enabled but no message is set in the config.");
            return;
        }
        message = message.replace("%player%", event.getPlayer().getName());
        event.getPlayer().sendMessage(message);
    }
}
