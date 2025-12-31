package net.pixelateddream.macebattles.commands;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MenuListener implements Listener {
    private final DuelsMenu duelsMenu;

    public MenuListener(DuelsMenu duelsMenu) {
        this.duelsMenu = duelsMenu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Inventory where the click happened
        Inventory inventory = event.getInventory();

        String title = event.getView().getTitle();
        if (!title.equals("§6§lDUELS MENU")) {
            return;
        }

        // Cancel the event to prevent item removal
        event.setCancelled(true);

        // Check if a player clicked
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        // Only handle clicks in the menu inventory (not player inventory)
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }

        // Handle the click
        duelsMenu.handleMenuClick(player, event.getCurrentItem(), slot);
    }
}

