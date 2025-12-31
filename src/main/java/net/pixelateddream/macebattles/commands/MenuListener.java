package net.pixelateddream.macebattles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MenuListener implements Listener {
    private final Macebattles plugin;
    private final DuelsMenu duelsMenu;

    public MenuListener(Macebattles plugin, DuelsMenu duelsMenu) {
        this.plugin = plugin;
        this.duelsMenu = duelsMenu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the inventory is a duels menu
        Inventory inventory = event.getInventory();
        if (inventory == null) return;

        String title = event.getView().getTitle();
        if (!title.equals("§6§lDUELS MENU")) {
            return;
        }

        // Cancel the event to prevent item removal
        event.setCancelled(true);

        // Check if a player clicked
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // Only handle clicks in the menu inventory (not player inventory)
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }

        // Handle the click
        duelsMenu.handleMenuClick(player, event.getCurrentItem(), slot);
    }
}

