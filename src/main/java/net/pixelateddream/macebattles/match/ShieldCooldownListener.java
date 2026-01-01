package net.pixelateddream.macebattles.match;

import net.pixelateddream.macebattles.Macebattles;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShieldCooldownListener implements Listener {

    public ShieldCooldownListener(Macebattles plugin) {
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player defender)) return;

        // Basic mace detection - adjust to your actual mace check (persistent data, model id, etc.)
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (!isMace(weapon)) return;

        // Require at least 4 damage to trigger shield cooldown
        double damage = event.getDamage();
        if (damage < 4.0) return;

        // Apply cooldown to the shield, so it cannot block for the duration
        // cooldown in ticks (20 ticks = 1 second)
        // 3 seconds
        int shieldCooldownTicks = 60;
        defender.setCooldown(Material.SHIELD, shieldCooldownTicks);
    }
    public boolean isMace(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.MACE) return false; // adjust material if needed

        ItemMeta meta = item.getItemMeta();
        return meta != null;
    }

}
