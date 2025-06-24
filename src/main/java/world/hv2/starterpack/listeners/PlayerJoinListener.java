package world.hv2.starterpack.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import world.hv2.starterpack.StarterPackPlugin;

/**
 * Handles player join events to give starter packs
 */
public class PlayerJoinListener implements Listener {
    
    private final StarterPackPlugin plugin;
    
    public PlayerJoinListener(StarterPackPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Log all joins if debug enabled
        if (plugin.getConfigManager().isLogAllJoinsEnabled()) {
            plugin.debugLog("Player " + player.getName() + " joined the server");
        }
        
        // Check if starter pack is enabled
        if (!plugin.getConfigManager().isStarterPackEnabled()) {
            plugin.debugLog("Starter pack is disabled, skipping " + player.getName());
            return;
        }
        
        // Check if player has already received starter pack
        if (plugin.getStarterPackManager().hasReceivedStarterPack(player)) {
            plugin.debugLog("Player " + player.getName() + " has already received starter pack");
            return;
        }
        
        // Check if player has bypass permission
        if (player.hasPermission("starterpack.bypass")) {
            plugin.debugLog("Player " + player.getName() + " has bypass permission, not giving starter pack");
            return;
        }
        
        // Give starter pack with a small delay to ensure player is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                boolean success = plugin.getStarterPackManager().giveStarterPack(player);
                if (success) {
                    plugin.getLogger().info("Gave starter pack to new player: " + player.getName());
                } else {
                    plugin.getLogger().warning("Failed to give starter pack to player: " + player.getName());
                }
            }
        }, 20L); // 1 second delay
    }
}
