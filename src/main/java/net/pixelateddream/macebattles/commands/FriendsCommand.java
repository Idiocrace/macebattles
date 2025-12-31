package net.pixelateddream.macebattles.commands;

import net.pixelateddream.macebattles.Macebattles;
import net.pixelateddream.macebattles.player.friend.FriendsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendsCommand implements CommandExecutor, TabCompleter {
    private Macebattles plugin;
    private final FriendsManager friendsManager = new FriendsManager(plugin);

    public FriendsCommand(Macebattles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            // Will open friends menu when I implement GUI
            sender.sendMessage("§cNo menu implemented");
            sender.sendMessage("§eUsage: /friend <add|remove|list|accept|deny> [player]");
            return true;
        }

        String actionOne = args[0].toLowerCase();
        switch (actionOne) {
            case "add":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /friend add <player>");
                    return true;
                }
            {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage("§cPlayer not found or is offline");
                    return true;
                }
                friendsManager.sendFriendRequest(player, targetPlayer);
                return true;
            }
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /friend remove <player>");
                    return true;
                }
            {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage("§cPlayer not found or is offline");
                    return true;
                }
                friendsManager.removeFriend(player, targetPlayer);
                return true;
            }
            case "accept":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /friend accept <player>");
                    return true;
                }
            {
                Player requestSender = Bukkit.getPlayer(args[1]);
                if (requestSender == null) {
                    sender.sendMessage("§cPlayer not found or is offline");
                    return true;
                }
                friendsManager.acceptFriendRequest(player, requestSender);
                return true;
            }
            case "deny":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /friend deny <player>");
                    return true;
                }
            {
                Player requestSender = Bukkit.getPlayer(args[1]);
                if (requestSender == null) {
                    sender.sendMessage("§cPlayer not found or is offline");
                    return true;
                }
                friendsManager.denyFriendRequest(player, requestSender);
                return true;
            }
            case "list":
                friendsManager.getFriendsList(player);
                return true;
            default:
                sender.sendMessage("§cUnknown subcommand: " + actionOne);
                return true;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "accept", "deny");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            List<String> playerNames = new ArrayList<>();
            for (Player p : onlinePlayers) {
                playerNames.add(p.getName());
            }
            return playerNames;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            List<Player> friendsList = friendsManager.getFriendsList(player);
            List<String> friendNames = new ArrayList<>();
            for (Player friend : friendsList) {
                friendNames.add(friend.getName());
            }
            return friendNames;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny"))) {
            List<Player> pendingRequests = friendsManager.getPendingFriendRequests(player);
            List<String> requesterNames = new ArrayList<>();
            for (Player requester : pendingRequests) {
                requesterNames.add(requester.getName());
            }
            return requesterNames;
        }

        return List.of();
    }
}