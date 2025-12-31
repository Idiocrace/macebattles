package net.pixelateddream.macebattles.commands;

import net.pixelateddream.macebattles.player.friend.FriendsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendsCommand implements CommandExecutor, TabCompleter {
    private final FriendsManager friendsManager;

    public FriendsCommand() {
        this.friendsManager = new FriendsManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }

        if (args.length == 0) {
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
                sender.sendMessage("§eFriend request sent");
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
                sender.sendMessage("§cFriend removed");
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
                sender.sendMessage("§aFriend request accepted");
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
                sender.sendMessage("§cFriend request denied");
                return true;
            }
            case "list":
                friendsManager.getFriendsList(player);
                sender.sendMessage("§e§lYour friends:");
                for (Player friend : friendsManager.getFriendsList(player)) {
                    sender.sendMessage("§e- " + friend.getName());
                }
                return true;
            default:
                sender.sendMessage("§cUnknown subcommand: " + actionOne);
                return true;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

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