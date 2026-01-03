package net.pixelateddream.macebattles.misc;

import com.google.gson.reflect.TypeToken;
import net.pixelateddream.macebattles.util.AsyncPlayerPreLoginEventHook;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

public class EarlyAccessPaywall {
    private final Path eligibleUuidsPath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public EarlyAccessPaywall(File dataFolder) {
        File dataFolder1 = Objects.requireNonNull(dataFolder, "dataFolder");
        // combine paths in a platform-safe way
        this.eligibleUuidsPath = Paths.get(dataFolder1.getPath(), "ea", "eligible_uuids.json");
        AsyncPlayerPreLoginEventHook.addPreLoginEvent(this::conditionallyShowPaywall);
    }

    public void conditionallyShowPaywall(AsyncPlayerPreLoginEvent event) {
        UUID player = event.getUniqueId();
        if (!isPlayerEligible(event)) {
            // Note: AsyncPlayerPreLoginEvent runs before the Player object exists; don't call Bukkit.getPlayer here.
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "§cEarly Access is currently restricted.\n§cEither wait until Jan. 31, 2026, or contact us for access.\n§o§8If you already have access, please retry in a few moments.");
        }
    }

    private boolean isPlayerEligible(AsyncPlayerPreLoginEvent event) {
        // check ops via UUID is not available here; keep logic minimal for eligibility check
        List<UUID> eligibleUuids = getEligibleUuids();
        return eligibleUuids.contains(event.getUniqueId());
    }

    private List<UUID> getEligibleUuids() {
        try {
            if (Files.exists(eligibleUuidsPath)) {
                try (Reader reader = Files.newBufferedReader(eligibleUuidsPath)) {
                    List<String> eligibleStrings = gson.fromJson(reader, new TypeToken<List<String>>() {}.getType());
                    if (eligibleStrings == null) return List.of();
                    return eligibleStrings.stream().map(UUID::fromString).toList();
                } catch (JsonIOException | JsonSyntaxException | java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }
}
