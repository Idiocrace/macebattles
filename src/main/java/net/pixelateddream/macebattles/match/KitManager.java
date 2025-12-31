package net.pixelateddream.macebattles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class KitManager {
    private final File kitFile;
    private final FileConfiguration kitConfig;

    public KitManager(Plugin plugin) {
        this.kitFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitFile.exists()) {
            try {
                boolean created = kitFile.createNewFile();
                if (!created) plugin.getLogger().warning("Could not create kits.yml file.");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create kits.yml: " + e.getMessage());
            }
        }
        this.kitConfig = YamlConfiguration.loadConfiguration(kitFile);
    }

    public void saveKit(Player player, String kitName) {
        kitConfig.set(kitName + ".inventory", player.getInventory().getContents());
        kitConfig.set(kitName + ".armor", player.getInventory().getArmorContents());
        try {
            kitConfig.save(kitFile);
            player.sendMessage("§aKit saved for " + kitName + "!");
        } catch (IOException e) {
            player.sendMessage("§cFailed to save kit: " + e.getMessage());
        }
    }

    public void applyKit(Player player, String kitName) {
        java.util.List<?> invList = kitConfig.getList(kitName + ".inventory");
        java.util.List<?> armorList = kitConfig.getList(kitName + ".armor");
        ItemStack[] inv = null;
        ItemStack[] armor = null;
        if (invList != null) {
            inv = new ItemStack[invList.size()];
            for (int i = 0; i < invList.size(); i++) {
                Object obj = invList.get(i);
                if (obj == null) {
                    inv[i] = null;
                } else if (obj instanceof ItemStack) {
                    inv[i] = (ItemStack) obj;
                } else if (obj instanceof java.util.Map) {
                    inv[i] = ItemStack.deserialize((java.util.Map<String, Object>) obj);
                } else {
                    inv[i] = null;
                }
            }
        }
        if (armorList != null) {
            armor = new ItemStack[armorList.size()];
            for (int i = 0; i < armorList.size(); i++) {
                Object obj = armorList.get(i);
                if (obj == null) {
                    armor[i] = null;
                } else if (obj instanceof ItemStack) {
                    armor[i] = (ItemStack) obj;
                } else if (obj instanceof java.util.Map) {
                    armor[i] = ItemStack.deserialize((java.util.Map<String, Object>) obj);
                } else {
                    armor[i] = null;
                }
            }
        }
        if (inv != null) player.getInventory().setContents(inv);
        if (armor != null) player.getInventory().setArmorContents(armor);
    }

    public boolean kitExists(String kitName) {
        return kitConfig.contains(kitName + ".inventory");
    }
}
