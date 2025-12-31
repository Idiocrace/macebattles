package net.pixelateddream.macebattles.util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlayerJoinEventHook implements Listener {

    private static final List<Consumer<PlayerJoinEvent>> joinEvents = new ArrayList<>();

    public static void addJoinEvent(Consumer<PlayerJoinEvent> event) {
        joinEvents.add(event);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (Consumer<PlayerJoinEvent> joinEvent : joinEvents) {
            joinEvent.accept(event);
        }
    }
}
