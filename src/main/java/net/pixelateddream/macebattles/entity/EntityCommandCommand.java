package net.pixelateddream.macebattles.entity;

import net.pixelateddream.macebattles.Macebattles;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Command to manage entity command links
 * Usage: /entitycmd <link|unlink|list> [args...]
 */
public class EntityCommandCommand implements CommandExecutor, TabCompleter {
    private final Macebattles plugin;

    public EntityCommandCommand(Macebattles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Only ops can use this command
        if (!sender.isOp()) {
            sender.sendMessage("§cYou must be an operator to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "link":
                return handleLink(sender, args);
            case "unlink":
                return handleUnlink(sender);
            case "list":
                return handleList(sender);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage("§cUnknown subcommand: " + subCommand);
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Links an entity to a command
     * Usage: /entitycmd link <console|player> <command>
     */
    private boolean handleLink(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /entitycmd link <console|player> <command>");
            sender.sendMessage("§7Example: /entitycmd link console duels queue ranked");
            sender.sendMessage("§7Then right-click an entity to link it.");
            return true;
        }

        String executorType = args[1].toLowerCase();
        boolean consoleExecutor;

        if (executorType.equals("console")) {
            consoleExecutor = true;
        } else if (executorType.equals("player")) {
            consoleExecutor = false;
        } else {
            sender.sendMessage("§cExecutor must be either 'console' or 'player'!");
            return true;
        }

        // Join the remaining args to form the command
        StringBuilder commandBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) commandBuilder.append(" ");
            commandBuilder.append(args[i]);
        }
        String cmdToExecute = commandBuilder.toString();

        // Put player in linking mode
        plugin.setPlayerLinkingMode(player.getUniqueId(), cmdToExecute, consoleExecutor);

        player.sendMessage("§e§lEntity Command Linking Mode");
        player.sendMessage("§7Right-click an entity to link it to the command:");
        player.sendMessage("§b" + cmdToExecute);
        player.sendMessage("§7Executor: §e" + (consoleExecutor ? "Console" : "Player"));
        player.sendMessage("§7Use §e/entitycmd cancel §7to exit linking mode.");
        player.sendMessage("");
        player.sendMessage("§7Placeholders: §e%player% §7= player name, §e%uuid% §7= player UUID");

        return true;
    }

    /**
     * Unlinks an entity from a command
     * Usage: /entitycmd unlink
     */
    private boolean handleUnlink(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        // Put player in unlinking mode
        plugin.setPlayerUnlinkingMode(player.getUniqueId());

        player.sendMessage("§c§lEntity Command Unlinking Mode");
        player.sendMessage("§7Right-click an entity to unlink it.");
        player.sendMessage("§7Use §e/entitycmd cancel §7to exit unlinking mode.");

        return true;
    }

    /**
     * Lists all entity command links
     * Usage: /entitycmd list
     */
    private boolean handleList(CommandSender sender) {
        List<EntityCommandLink> links = plugin.getAllEntityCommandLinks();

        if (links.isEmpty()) {
            sender.sendMessage("§7No entity command links found.");
            return true;
        }

        sender.sendMessage("§e§l========== Entity Command Links ==========");
        for (EntityCommandLink link : links) {
            UUID entityUUID = link.getEntityUUID();
            Entity entity = null;

            // Try to find the entity
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (Entity e : world.getEntities()) {
                    if (e.getUniqueId().equals(entityUUID)) {
                        entity = e;
                        break;
                    }
                }
                if (entity != null) break;
            }

            String entityInfo;
            if (entity != null) {
                entityInfo = "§a" + entity.getType() + " §7at " + entity.getLocation().getBlockX() + ", " +
                             entity.getLocation().getBlockY() + ", " + entity.getLocation().getBlockZ();
            } else {
                entityInfo = "§c[Entity not found]";
            }

            sender.sendMessage(entityInfo);
            sender.sendMessage("  §7UUID: §f" + entityUUID);
            sender.sendMessage("  §7Command: §b" + link.getCommand());
            sender.sendMessage("  §7Executor: §e" + (link.isConsoleExecutor() ? "Console" : "Player"));
            sender.sendMessage("");
        }

        return true;
    }

    /**
     * Sends help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e§l========== Entity Command Help ==========");
        sender.sendMessage("§6/entitycmd link <console|player> <command>");
        sender.sendMessage("  §7Link an entity to execute a command when clicked");
        sender.sendMessage("  §7Example: §b/entitycmd link console duels queue ranked");
        sender.sendMessage("");
        sender.sendMessage("§6/entitycmd unlink");
        sender.sendMessage("  §7Unlink an entity from its command");
        sender.sendMessage("");
        sender.sendMessage("§6/entitycmd list");
        sender.sendMessage("  §7List all entity command links");
        sender.sendMessage("");
        sender.sendMessage("§7Placeholders:");
        sender.sendMessage("  §e%player% §7- Player's name");
        sender.sendMessage("  §e%uuid% §7- Player's UUID");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.isOp()) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("link", "unlink", "list", "help", "leaderboard");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("link")) {
            return Arrays.asList("console", "player");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("leaderboard")) {
            return Arrays.asList("link", "unlink");
        }

        return new ArrayList<>();
    }
}
