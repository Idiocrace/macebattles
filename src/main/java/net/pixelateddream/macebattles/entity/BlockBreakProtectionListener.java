package net.pixelateddream.macebattles.entity;

import net.pixelateddream.macebattles.Macebattles;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

public class BlockBreakProtectionListener implements Listener {
    private final Macebattles plugin;

    public BlockBreakProtectionListener(Macebattles plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevents block breaking except for ops in creative mode
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Allow ops/admins in creative mode to break blocks
        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Cancel all other block breaks
        event.setCancelled(true);
    }

    /**
     * Prevents block placement except for ops in creative mode
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Allow ops/admins in creative mode to place blocks
        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Cancel all other block placement
        event.setCancelled(true);
    }

    /**
     * Prevents entity damage except in active matches
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // If damager is a player
        if (damager instanceof Player player) {

            // Allow damage if player is in an active match
            if (plugin.getMatchmakingListener() != null) {
                if (plugin.getMatchmakingListener().getMatchByPlayer(player.getUniqueId()) != null) {
                    // Player is in a match, allow PvP damage
                    return;
                }
            }

            // Allow ops in creative mode to damage entities
            if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            // Cancel all other entity damage by players
            event.setCancelled(true);
        }
    }

    /**
     * Prevents environmental damage to players not in matches
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageGeneral(EntityDamageEvent event) {
        // Only protect players from environmental damage when not in matches
        if (event.getEntity() instanceof Player player) {

            // Skip if already cancelled by another handler
            if (event.isCancelled()) {
                return;
            }

            // Allow damage if player is in an active match
            if (plugin.getMatchmakingListener() != null) {
                if (plugin.getMatchmakingListener().getMatchByPlayer(player.getUniqueId()) != null) {
                    // Player is in a match, allow all damage
                    return;
                }
            }

            // Cancel environmental damage for players not in matches
            // (fall damage, fire, drowning, etc.)
            event.setCancelled(true);
        }
    }

    /**
     * Prevents hanging entity (item frames, paintings) destruction
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player) {

            // Allow ops in creative mode
            if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            // Cancel for all other players
            event.setCancelled(true);
        }
    }

    /**
     * Prevents vehicle (minecart, boat) damage
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player player) {

            // Allow ops in creative mode
            if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            // Cancel for all other players
            event.setCancelled(true);
        }
    }

    /**
     * Prevents armor stand manipulation
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();

        // Allow ops in creative mode
        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Cancel for all other players
        event.setCancelled(true);
    }

    /**
     * Prevents entity interaction (leash, name tag, etc.) except in creative
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        // Allow ops in creative mode
        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Allow players to ride entities (boats, horses, etc.)
        // but prevent other interactions like leashing
        if (event.getRightClicked().getType().toString().contains("BOAT") ||
            event.getRightClicked().getType().toString().contains("MINECART")) {
            return; // Allow riding vehicles
        }

        // Cancel other entity interactions
        event.setCancelled(true);
    }
}

