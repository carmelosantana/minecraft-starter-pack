package world.hv2.starterpack.managers;

import org.bukkit.configuration.file.FileConfiguration;
import world.hv2.starterpack.StarterPackPlugin;

/**
 * Manages plugin configuration loading and access
 */
public class ConfigManager {
    
    private final StarterPackPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(StarterPackPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load configuration from config.yml
     */
    public void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Validate configuration
        validateConfig();
    }
    
    /**
     * Validate configuration values
     */
    private void validateConfig() {
        if (!config.contains("starter-pack.enabled")) {
            plugin.getLogger().warning("Missing 'starter-pack.enabled' in config.yml, defaulting to true");
        }
        
        if (!config.contains("starter-pack.items")) {
            plugin.getLogger().warning("Missing 'starter-pack.items' in config.yml, no items will be given!");
        }
    }
    
    // Configuration getters
    public boolean isStarterPackEnabled() {
        return config.getBoolean("starter-pack.enabled", true);
    }
    
    public boolean isBroadcastEnabled() {
        return config.getBoolean("starter-pack.broadcast", true);
    }
    
    public String getWelcomeMessage() {
        return config.getString("starter-pack.welcome-message", "&aWelcome to the server, &b{player}&a!");
    }
    
    public String getBroadcastMessage() {
        return config.getString("starter-pack.broadcast-message", "&e{player} &7has joined the server for the first time!");
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }
    
    public boolean isLogAllJoinsEnabled() {
        return config.getBoolean("debug.log-all-joins", false);
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
}
