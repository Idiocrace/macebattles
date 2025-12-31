package net.pixelateddream.macebattles.util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AsyncPlayerPreLoginEventHook implements Listener {

    // Thread-safe list since this event is fired async
    private static final List<Consumer<AsyncPlayerPreLoginEvent>> preLoginEvents = new CopyOnWriteArrayList<>();

    public static void addPreLoginEvent(Consumer<AsyncPlayerPreLoginEvent> event) {
        preLoginEvents.add(event);
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        for (Consumer<AsyncPlayerPreLoginEvent> consumer : preLoginEvents) {
            try {
                consumer.accept(event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}