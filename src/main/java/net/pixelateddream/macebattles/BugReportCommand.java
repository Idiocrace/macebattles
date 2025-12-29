package net.pixelateddream.macebattles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BugReportCommand implements CommandExecutor {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final File bugReportFile;
    private static final long COOLDOWN_MILLIS = 5 * 60 * 1000; // 5 minutes

    public BugReportCommand(File dataFolder) {
        this.bugReportFile = new File(dataFolder, "bugreports.txt");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        UUID uuid = player.getUniqueId();
        long now = Instant.now().toEpochMilli();
        if (cooldowns.containsKey(uuid)) {
            long last = cooldowns.get(uuid);
            if (now - last < COOLDOWN_MILLIS) {
                long remaining = (COOLDOWN_MILLIS - (now - last)) / 1000;
                player.sendMessage(ChatColor.RED + "You can only submit a bug report every 5 minutes. Please wait " + remaining + " seconds.");
                return true;
            }
        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <description of the bug>");
            return true;
        }
        String report = String.join(" ", args);
        String entry = String.format("[%s] %s (%s): %s", Instant.now(), player.getName(), uuid, report);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(bugReportFile, true))) {
            writer.write(entry);
            writer.newLine();
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to save bug report. Please contact an admin.");
            return true;
        }
        cooldowns.put(uuid, now);
        player.sendMessage(ChatColor.GREEN + "Thank you for your bug report!");
        // Optionally notify admins online
        Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("macebattles.bugreport.notify")).forEach(p ->
            p.sendMessage(ChatColor.YELLOW + "[BugReport] " + player.getName() + ": " + report)
        );
        return true;
    }
}
