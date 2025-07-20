package world.hv2.starterpack.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import world.hv2.starterpack.StarterPackPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages starter pack distribution and player tracking
 */
public class StarterPackManager {
    
    private final StarterPackPlugin plugin;
    private final Set<UUID> playersReceived;
    private final NamespacedKey starterPackKey;
    
    public StarterPackManager(StarterPackPlugin plugin) {
        this.plugin = plugin;
        this.playersReceived = new HashSet<>();
        this.starterPackKey = new NamespacedKey(plugin, "received_starter_pack");
    }
    
    /**
     * Check if a player has already received their starter pack
     */
    public boolean hasReceivedStarterPack(Player player) {
        // Check in-memory cache first
        if (playersReceived.contains(player.getUniqueId())) {
            return true;
        }
        
        // Check persistent data
        if (player.getPersistentDataContainer().has(starterPackKey, PersistentDataType.BYTE)) {
            playersReceived.add(player.getUniqueId());
            return true;
        }
        
        return false;
    }
    
    /**
     * Mark a player as having received their starter pack
     */
    public void markPlayerAsReceived(Player player) {
        playersReceived.add(player.getUniqueId());
        player.getPersistentDataContainer().set(starterPackKey, PersistentDataType.BYTE, (byte) 1);
    }
     /**
     * Give starter pack to a player (original method - now with equipment for new players)
     */
    public boolean giveStarterPack(Player player) {
        // For new players, auto-equip if they don't have equipment
        return giveStarterPackWithEquipment(player, true);
    }
    
    /**
     * Give starter pack to a player with optional equipment
     */
    public boolean giveStarterPackWithEquipment(Player player, boolean equipItems) {
        if (!plugin.getConfigManager().isStarterPackEnabled()) {
            plugin.debugLog("Starter pack is disabled, not giving to " + player.getName());
            return false;
        }

        // Check if player has bypass permission
        if (player.hasPermission("starterpack.bypass")) {
            plugin.debugLog("Player " + player.getName() + " has bypass permission, not giving starter pack");
            return false;
        }

        try {
            List<ItemStack> items = createStarterPackItems();
            
            // Give items to player
            for (ItemStack item : items) {
                if (item != null) {
                    // Try to add to inventory, drop if full
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }
            }
            
            // Auto-equip items if requested and player doesn't have equipment
            if (equipItems && !hasAnyEquipment(player)) {
                equipStarterItems(player);
            }
            
            // Mark player as received
            markPlayerAsReceived(player);
            
            // Send welcome message
            String welcomeMessage = plugin.getConfigManager().getWelcomeMessage();
            welcomeMessage = welcomeMessage.replace("{player}", player.getName());
            plugin.sendMessage(player, welcomeMessage);
            
            // Broadcast if enabled
            if (plugin.getConfigManager().isBroadcastEnabled()) {
                String broadcastMessage = plugin.getConfigManager().getBroadcastMessage();
                broadcastMessage = broadcastMessage.replace("{player}", player.getName());
                plugin.broadcastMessage(broadcastMessage);
            }
            
            plugin.debugLog("Successfully gave starter pack to " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error giving starter pack to " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if player has any armor or tools equipped
     */
    public boolean hasAnyEquipment(Player player) {
        // Check armor slots
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        if (helmet != null && !helmet.getType().isAir()) return true;
        if (chestplate != null && !chestplate.getType().isAir()) return true;
        if (leggings != null && !leggings.getType().isAir()) return true;
        if (boots != null && !boots.getType().isAir()) return true;
        
        // Check main hand and off hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        if (mainHand != null && !mainHand.getType().isAir() && isWeaponOrTool(mainHand.getType())) return true;
        if (offHand != null && !offHand.getType().isAir() && isWeaponOrTool(offHand.getType())) return true;
        
        return false;
    }
    
    /**
     * Check if a material is a weapon or tool
     */
    private boolean isWeaponOrTool(Material material) {
        String name = material.name();
        return name.contains("_SWORD") || name.contains("_AXE") || name.contains("_PICKAXE") || 
               name.contains("_SHOVEL") || name.contains("_HOE") || name.contains("BOW") || 
               name.contains("CROSSBOW") || name.contains("TRIDENT") || name.contains("MACE");
    }
    
    /**
     * Equip starter items on player
     */
    public boolean equipStarterItems(Player player) {
        try {
            List<ItemStack> equipmentItems = createStarterEquipmentItems();
            
            for (ItemStack item : equipmentItems) {
                if (item != null) {
                    equipItemOnPlayer(player, item);
                }
            }
            
            plugin.debugLog("Successfully equipped starter items on " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error equipping starter items on " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Force equip items on player, overriding existing equipment
     */
    public boolean forceEquipStarterItems(Player player) {
        try {
            List<ItemStack> equipmentItems = createStarterEquipmentItems();
            
            // First, move existing equipment to inventory if possible
            moveEquipmentToInventory(player);
            
            for (ItemStack item : equipmentItems) {
                if (item != null) {
                    equipItemOnPlayer(player, item);
                }
            }
            
            plugin.debugLog("Successfully force equipped starter items on " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error force equipping starter items on " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Move existing equipment to inventory
     */
    private void moveEquipmentToInventory(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        // Move armor to inventory
        for (ItemStack armorPiece : armor) {
            if (armorPiece != null && !armorPiece.getType().isAir()) {
                addToInventoryOrDrop(player, armorPiece);
            }
        }
        
        // Move main hand item to inventory
        if (mainHand != null && !mainHand.getType().isAir()) {
            addToInventoryOrDrop(player, mainHand);
        }
        
        // Move offhand item to inventory
        if (offHand != null && !offHand.getType().isAir()) {
            addToInventoryOrDrop(player, offHand);
        }
    }
    
    /**
     * Create starter equipment items from configuration
     */
    private List<ItemStack> createStarterEquipmentItems() {
        List<ItemStack> items = new ArrayList<>();
        
        // Get the equipment list from configuration
        List<?> equipmentList = plugin.getConfigManager().getConfig().getList("starter-pack.equipment");
        if (equipmentList == null || equipmentList.isEmpty()) {
            plugin.debugLog("No starter pack equipment configured");
            return items;
        }
        
        // Process each equipment item in the list
        for (int i = 0; i < equipmentList.size(); i++) {
            Object itemObj = equipmentList.get(i);
            
            // Each item should be a map (ConfigurationSection)
            if (itemObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> itemMap = (java.util.Map<String, Object>) itemObj;
                
                try {
                    ItemStack item = createItemFromMap(itemMap);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to create equipment item from config index " + i + ": " + e.getMessage());
                }
            } else {
                plugin.getLogger().warning("Invalid equipment configuration at index " + i + ": expected map, got " + itemObj.getClass().getSimpleName());
            }
        }
        
        return items;
    }
    
    /**
     * Equip an item on a player based on its type and slot configuration
     */
    private void equipItemOnPlayer(Player player, ItemStack item) {
        Material material = item.getType();
        
        // Check if item has slot specified in its persistent data
        String configuredSlot = null;
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            NamespacedKey slotKey = new NamespacedKey(plugin, "equipment_slot");
            if (item.getItemMeta().getPersistentDataContainer().has(slotKey, PersistentDataType.STRING)) {
                configuredSlot = item.getItemMeta().getPersistentDataContainer().get(slotKey, PersistentDataType.STRING);
            }
        }
        
        // Use configured slot if available, otherwise determine from material type
        if (configuredSlot != null) {
            switch (configuredSlot.toLowerCase()) {
                case "helmet":
                    player.getInventory().setHelmet(item);
                    break;
                case "chestplate":
                    player.getInventory().setChestplate(item);
                    break;
                case "leggings":
                    player.getInventory().setLeggings(item);
                    break;
                case "boots":
                    player.getInventory().setBoots(item);
                    break;
                case "mainhand":
                    player.getInventory().setItemInMainHand(item);
                    break;
                case "offhand":
                    player.getInventory().setItemInOffHand(item);
                    break;
                default:
                    // Unknown slot, add to inventory
                    plugin.getLogger().warning("Unknown equipment slot: " + configuredSlot);
                    addToInventoryOrDrop(player, item);
                    break;
            }
        } else {
            // Fallback to material-based detection
            if (material.name().contains("_HELMET")) {
                player.getInventory().setHelmet(item);
            } else if (material.name().contains("_CHESTPLATE")) {
                player.getInventory().setChestplate(item);
            } else if (material.name().contains("_LEGGINGS")) {
                player.getInventory().setLeggings(item);
            } else if (material.name().contains("_BOOTS")) {
                player.getInventory().setBoots(item);
            } else if (isWeaponOrTool(material)) {
                player.getInventory().setItemInMainHand(item);
            } else {
                // Default to adding to inventory if we can't determine slot
                addToInventoryOrDrop(player, item);
            }
        }
    }
    
    /**
     * Add item to inventory or drop it if inventory is full
     */
    private void addToInventoryOrDrop(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItem(player.getLocation(), item);
        }
    }

    /**
     * Create starter pack items from configuration
     */
    private List<ItemStack> createStarterPackItems() {
        List<ItemStack> items = new ArrayList<>();
        
        // Get the items list from configuration
        List<?> itemsList = plugin.getConfigManager().getConfig().getList("starter-pack.items");
        if (itemsList == null || itemsList.isEmpty()) {
            plugin.getLogger().warning("No starter pack items configured!");
            return items;
        }
        
        // Process each item in the list
        for (int i = 0; i < itemsList.size(); i++) {
            Object itemObj = itemsList.get(i);
            
            // Each item should be a map (ConfigurationSection)
            if (itemObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> itemMap = (java.util.Map<String, Object>) itemObj;
                
                try {
                    ItemStack item = createItemFromMap(itemMap);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to create item from config index " + i + ": " + e.getMessage());
                }
            } else {
                plugin.getLogger().warning("Invalid item configuration at index " + i + ": expected map, got " + itemObj.getClass().getSimpleName());
            }
        }
        
        return items;
    }
    
    /**
     * Create an ItemStack from a map (YAML list item)
     */
    private ItemStack createItemFromMap(java.util.Map<String, Object> itemMap) {
        String materialName = (String) itemMap.get("material");
        if (materialName == null) {
            plugin.getLogger().warning("Missing material in item configuration");
            return null;
        }
        
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialName);
            return null;
        }
        
        int amount = 1;
        if (itemMap.containsKey("amount")) {
            Object amountObj = itemMap.get("amount");
            if (amountObj instanceof Number) {
                amount = ((Number) amountObj).intValue();
            }
        }
        
        ItemStack item = new ItemStack(material, amount);
        
        // Set custom name and lore
        if (itemMap.containsKey("name") || itemMap.containsKey("lore") || itemMap.containsKey("enchantments") || itemMap.containsKey("slot")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Set display name
                if (itemMap.containsKey("name")) {
                    String name = (String) itemMap.get("name");
                    Component displayName = LegacyComponentSerializer.legacyAmpersand().deserialize(name);
                    meta.displayName(displayName);
                }
                
                // Set lore
                if (itemMap.containsKey("lore")) {
                    Object loreObj = itemMap.get("lore");
                    if (loreObj instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<String> loreList = (java.util.List<String>) loreObj;
                        java.util.List<Component> coloredLore = new ArrayList<>();
                        for (String line : loreList) {
                            coloredLore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
                        }
                        meta.lore(coloredLore);
                    }
                }
                
                // Add enchantments
                if (itemMap.containsKey("enchantments")) {
                    Object enchantObj = itemMap.get("enchantments");
                    if (enchantObj instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> enchantMap = (java.util.Map<String, Object>) enchantObj;
                        
                        for (java.util.Map.Entry<String, Object> enchantEntry : enchantMap.entrySet()) {
                            String enchantName = enchantEntry.getKey();
                            Object levelObj = enchantEntry.getValue();
                            
                            try {
                                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
                                if (enchant != null) {
                                    int level = 1;
                                    if (levelObj instanceof Number) {
                                        level = ((Number) levelObj).intValue();
                                    }
                                    meta.addEnchant(enchant, level, true);
                                } else {
                                    plugin.getLogger().warning("Unknown enchantment: " + enchantName);
                                }
                            } catch (Exception e) {
                                plugin.getLogger().warning("Failed to apply enchantment " + enchantName + ": " + e.getMessage());
                            }
                        }
                    }
                }
                
                // Store slot information in persistent data
                if (itemMap.containsKey("slot")) {
                    String slot = (String) itemMap.get("slot");
                    if (slot != null) {
                        NamespacedKey slotKey = new NamespacedKey(plugin, "equipment_slot");
                        meta.getPersistentDataContainer().set(slotKey, PersistentDataType.STRING, slot.toLowerCase());
                    }
                }
                
                item.setItemMeta(meta);
            }
        }
        
        return item;
    }

    
    /**
     * Force give starter pack to a player (bypasses checks, does not auto-equip)
     */
    public boolean forceGiveStarterPack(Player player) {
        try {
            List<ItemStack> items = createStarterPackItems();
            
            // Give items to player
            for (ItemStack item : items) {
                if (item != null) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }
            }
            
            plugin.debugLog("Force gave starter pack to " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error force giving starter pack to " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reset starter pack status for a specific player
     */
    public boolean resetPlayerStarterPack(Player player) {
        try {
            // Remove from in-memory cache
            playersReceived.remove(player.getUniqueId());
            
            // Remove from persistent data
            player.getPersistentDataContainer().remove(starterPackKey);
            
            plugin.debugLog("Reset starter pack status for " + player.getName());
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error resetting starter pack for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reset starter pack status for all online players
     */
    public int resetAllPlayersStarterPack() {
        int resetCount = 0;
        
        try {
            // Clear in-memory cache
            playersReceived.clear();
            
            // Reset all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getPersistentDataContainer().has(starterPackKey, PersistentDataType.BYTE)) {
                    player.getPersistentDataContainer().remove(starterPackKey);
                    resetCount++;
                }
            }
            
            plugin.debugLog("Reset starter pack status for " + resetCount + " online players");
            return resetCount;
        } catch (Exception e) {
            plugin.getLogger().severe("Error resetting starter pack for all players: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Get statistics about starter pack distribution
     */
    public String getStarterPackStats() {
        int onlineWithPack = 0;
        int onlineTotal = plugin.getServer().getOnlinePlayers().size();
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hasReceivedStarterPack(player)) {
                onlineWithPack++;
            }
        }
        
        return String.format("Online players: %d | Have received pack: %d | New players: %d", 
                onlineTotal, onlineWithPack, onlineTotal - onlineWithPack);
    }
}
