package world.hv2.starterpack;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

import world.hv2.starterpack.listeners.PlayerJoinListener;
import world.hv2.starterpack.managers.ConfigManager;
import world.hv2.starterpack.managers.StarterPackManager;
import world.hv2.starterpack.commands.StarterPackCommand;

import java.util.logging.Logger;

/**
 * StarterPack Plugin Main Class
 * 
 * A lightweight plugin that gives first-time players a configurable starter inventory
 * 
 * @author Carmelo Santana
 * @version 1.0.0
 */
public class StarterPackPlugin extends JavaPlugin {
    
    private static StarterPackPlugin instance;
    private ConfigManager configManager;
    private StarterPackManager starterPackManager;
    private Logger logger;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        // Initialize managers
        configManager = new ConfigManager(this);
        starterPackManager = new StarterPackManager(this);
        
        // Load configuration
        saveDefaultConfig();
        configManager.loadConfig();
        
        // Register event listeners
        registerEvents();
        
        // Register commands
        registerCommands();
        
        // Log startup
        logger.info("StarterPack plugin enabled successfully!");
        logger.info("Version: " + getDescription().getVersion());
        
        if (configManager.isDebugEnabled()) {
            logger.info("Debug mode is enabled");
        }
    }
    
    @Override
    public void onDisable() {
        logger.info("StarterPack plugin disabled.");
        instance = null;
    }
    
    /**
     * Register event listeners
     */
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }
    
    /**
     * Register commands
     */
    private void registerCommands() {
        StarterPackCommand commandExecutor = new StarterPackCommand(this);
        getCommand("starterpack").setExecutor(commandExecutor);
        getCommand("starterpack").setTabCompleter(commandExecutor);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("starterpack")) {
            return new StarterPackCommand(this).onCommand(sender, command, label, args);
        }
        return false;
    }
    
    /**
     * Reload the plugin configuration
     */
    public void reloadPluginConfig() {
        reloadConfig();
        configManager.loadConfig();
        logger.info("Configuration reloaded successfully!");
    }
    
    // Getters
    public static StarterPackPlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public StarterPackManager getStarterPackManager() {
        return starterPackManager;
    }
    
    /**
     * Send a formatted message to a player
     */
    public void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Broadcast a formatted message to all players
     */
    public void broadcastMessage(String message) {
        getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * Log debug messages if debug mode is enabled
     */
    public void debugLog(String message) {
        if (configManager.isDebugEnabled()) {
            logger.info("[DEBUG] " + message);
        }
    }
}
