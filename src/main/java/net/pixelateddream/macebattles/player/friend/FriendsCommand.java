package net.pixelateddream.macebattles.player.friend;

//import net.pixelateddream.macebattles.Macebattles;

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
    //private final Macebattles plugin;

    //public FriendCommand(Macebattles plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            // Will open friends menu when I implement GUI
            sender.sendMessage("§cNo menu implemented");
            sender.sendMessage("§eUsage: /friend <add|remove|list|accept|deny> [<add|remove|accept|deny|>:player]");
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
                    assert targetPlayer != null;
                    FriendsManager.sendFriendRequest((Player) sender, targetPlayer);
                    return true;
                }
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /friend remove <player>");
                    return true;
                }
                {
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    assert targetPlayer != null;
                    FriendsManager.removeFriend((Player) sender, targetPlayer);
                    return true;
                }
            case "accept":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /friend accept <player>");
                    return true;
                }
                {
                    Player requestSender = Bukkit.getPlayer(args[1]);
                    FriendsManager.acceptFriendRequest((Player) sender, requestSender);
                    return true;
                }
            case "deny":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /friend deny <player>");
                    return true;
                }
                {
                    Player requestSender = Bukkit.getPlayer(args[1]);
                    FriendsManager.denyFriendRequest((Player) sender, requestSender);
                    return true;
                }
            case "list":
                FriendsManager.getFriendsList((Player) sender);
                return true;
            default:
                sender.sendMessage("§cUnknown subcommand: " + actionOne);
                return true;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "accept", "deny");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            List<String> playerNames = new ArrayList<>();
            for (Player player : onlinePlayers) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            List<Player> friendsList = FriendsManager.getFriendsList((Player) sender);
            List<String> friendNames = new ArrayList<>();
            for (Player friend : friendsList) {
                friendNames.add(friend.getName());
            }
            return friendNames;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny"))) {
            List<Player> pendingRequests = FriendsManager.getPendingFriendRequests((Player) sender);
            List<String> requesterNames = new ArrayList<>();
            for (Player requester : pendingRequests) {
                requesterNames.add(requester.getName());
            }
            return requesterNames;
        }

        return List.of();
    }
}
