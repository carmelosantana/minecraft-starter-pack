package world.hv2.starterpack.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import world.hv2.starterpack.StarterPackPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles all StarterPack commands
 */
public class StarterPackCommand implements CommandExecutor, TabCompleter {
    
    private final StarterPackPlugin plugin;
    
    public StarterPackCommand(StarterPackPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "give":
                return handleGive(sender, args);
            case "equip":
                return handleEquip(sender, args);
            case "force":
                return handleForce(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "stats":
                return handleStats(sender);
            case "version":
                return handleVersion(sender);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                sender.sendMessage(Component.text("Unknown command. Use /starterpack help for available commands.", NamedTextColor.RED));
                return true;
        }
    }
     /**
     * Handle reload command
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("starterpack.admin")) {
            sender.sendMessage(Component.text("You don't have permission to reload the configuration.", NamedTextColor.RED));
            return true;
        }

        try {
            plugin.reloadPluginConfig();
            sender.sendMessage(Component.text("Configuration reloaded successfully!", NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Error reloading configuration: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
        }

        return true;
    }
    
    /**
     * Handle give command
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("starterpack.admin")) {
            sender.sendMessage(Component.text("You don't have permission to give starter packs.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /starterpack give <player>", NamedTextColor.RED));
            return true;
        }
        
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(Component.text("Player '" + playerName + "' not found or not online.", NamedTextColor.RED));
            return true;
        }
        
        boolean success = plugin.getStarterPackManager().forceGiveStarterPack(target);
        
        if (success) {
            sender.sendMessage(Component.text("Successfully gave starter pack to " + target.getName() + "!", NamedTextColor.GREEN));
            target.sendMessage(Component.text("You have been given a starter pack by " + sender.getName() + "!", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to give starter pack to " + target.getName() + ".", NamedTextColor.RED));
        }
        
        return true;
    }
    
    /**
     * Handle equip command
     */
    private boolean handleEquip(CommandSender sender, String[] args) {
        if (!sender.hasPermission("starterpack.admin")) {
            sender.sendMessage(Component.text("You don't have permission to equip starter packs.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /starterpack equip <player>", NamedTextColor.RED));
            return true;
        }
        
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(Component.text("Player '" + playerName + "' not found or not online.", NamedTextColor.RED));
            return true;
        }
        
        // Check if player already has equipment
        if (plugin.getStarterPackManager().hasAnyEquipment(target)) {
            sender.sendMessage(Component.text(target.getName() + " already has equipment. Use '/starterpack force " + target.getName() + "' to override.", NamedTextColor.YELLOW));
            return true;
        }
        
        boolean success = plugin.getStarterPackManager().equipStarterItems(target);
        
        if (success) {
            sender.sendMessage(Component.text("Successfully equipped starter items on " + target.getName() + "!", NamedTextColor.GREEN));
            target.sendMessage(Component.text("You have been equipped with starter items by " + sender.getName() + "!", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to equip starter items on " + target.getName() + ".", NamedTextColor.RED));
        }
        
        return true;
    }
    
    /**
     * Handle force command
     */
    private boolean handleForce(CommandSender sender, String[] args) {
        if (!sender.hasPermission("starterpack.admin")) {
            sender.sendMessage(Component.text("You don't have permission to force equip starter packs.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /starterpack force <player>", NamedTextColor.RED));
            return true;
        }
        
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(Component.text("Player '" + playerName + "' not found or not online.", NamedTextColor.RED));
            return true;
        }
        
        // Force equip starter items (moves existing equipment to inventory)
        boolean success = plugin.getStarterPackManager().forceEquipStarterItems(target);
        
        if (success) {
            sender.sendMessage(Component.text("Successfully force equipped starter items on " + target.getName() + "!", NamedTextColor.GREEN));
            target.sendMessage(Component.text("Your equipment has been replaced with starter items by " + sender.getName() + "!", NamedTextColor.YELLOW));
            target.sendMessage(Component.text("Your previous equipment has been moved to your inventory or dropped.", NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Component.text("Failed to force equip starter items on " + target.getName() + ".", NamedTextColor.RED));
        }
        
        return true;
    }
    
    /**
     * Handle reset command
     */
    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("starterpack.admin")) {
            sender.sendMessage(Component.text("You don't have permission to reset starter pack data.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /starterpack reset <player|all>", NamedTextColor.RED));
            return true;
        }
        
        String target = args[1];
        
        if (target.equalsIgnoreCase("all")) {
            int resetCount = plugin.getStarterPackManager().resetAllPlayersStarterPack();
            if (resetCount >= 0) {
                sender.sendMessage(Component.text("Successfully reset starter pack status for " + resetCount + " online players!", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Note: This only affects online players. Offline players will retain their status.", NamedTextColor.GRAY));
            } else {
                sender.sendMessage(Component.text("Failed to reset starter pack status for all players.", NamedTextColor.RED));
            }
            return true;
        }
        
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player '" + target + "' not found or not online.", NamedTextColor.RED));
            return true;
        }
        
        boolean success = plugin.getStarterPackManager().resetPlayerStarterPack(targetPlayer);
        if (success) {
            sender.sendMessage(Component.text("Successfully reset starter pack status for " + targetPlayer.getName() + "!", NamedTextColor.GREEN));
            targetPlayer.sendMessage(Component.text("Your starter pack status has been reset by " + sender.getName() + ". You can receive it again on next join!", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text("Failed to reset starter pack status for " + targetPlayer.getName() + ".", NamedTextColor.RED));
        }
        
        return true;
    }
    
    /**
     * Handle stats command
     */
    private boolean handleStats(CommandSender sender) {
        sender.sendMessage(Component.text("=== StarterPack Statistics ===", NamedTextColor.GOLD));
        String stats = plugin.getStarterPackManager().getStarterPackStats();
        sender.sendMessage(Component.text(stats, NamedTextColor.GRAY));
        return true;
    }
    
    /**
     * Handle version command
     */
    private boolean handleVersion(CommandSender sender) {
        sender.sendMessage(Component.text("=== StarterPack Plugin ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: ", NamedTextColor.YELLOW)
            .append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Author: ", NamedTextColor.YELLOW)
            .append(Component.text("Carmelo Santana", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Website: ", NamedTextColor.YELLOW)
            .append(Component.text("https://xp.farm", NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("Description: ", NamedTextColor.YELLOW)
            .append(Component.text(plugin.getPluginMeta().getDescription(), NamedTextColor.WHITE)));
        
        // Show configuration status
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Configuration Status:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Enabled: ", NamedTextColor.YELLOW)
            .append(Component.text((plugin.getConfigManager().isStarterPackEnabled() ? "Yes" : "No"), 
                plugin.getConfigManager().isStarterPackEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        sender.sendMessage(Component.text("Broadcast: ", NamedTextColor.YELLOW)
            .append(Component.text((plugin.getConfigManager().isBroadcastEnabled() ? "Yes" : "No"), 
                plugin.getConfigManager().isBroadcastEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        sender.sendMessage(Component.text("Debug: ", NamedTextColor.YELLOW)
            .append(Component.text((plugin.getConfigManager().isDebugEnabled() ? "Yes" : "No"), 
                plugin.getConfigManager().isDebugEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        
        return true;
    }
    
    /**
     * Send help message
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("=== StarterPack Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/starterpack help", NamedTextColor.YELLOW)
            .append(Component.text(" - Show this help message", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/starterpack version", NamedTextColor.YELLOW)
            .append(Component.text(" - Show plugin version and status", NamedTextColor.GRAY)));
        
        if (sender.hasPermission("starterpack.admin")) {
            sender.sendMessage(Component.text("/starterpack reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload configuration", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/starterpack give <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Give starter pack to a player", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/starterpack equip <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Equip starter items (only if no equipment)", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/starterpack force <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Force equip starter items (overrides existing)", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/starterpack reset <player|all>", NamedTextColor.YELLOW)
                .append(Component.text(" - Reset starter pack status", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/starterpack stats", NamedTextColor.YELLOW)
                .append(Component.text(" - Show starter pack statistics", NamedTextColor.GRAY)));
        }
        
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Aliases: ", NamedTextColor.AQUA)
            .append(Component.text("/sp, /starter", NamedTextColor.WHITE)));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "version");
            
            if (sender.hasPermission("starterpack.admin")) {
                subCommands = Arrays.asList("help", "version", "reload", "give", "equip", "force", "reset", "stats");
            }
            
            String input = args[0].toLowerCase();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("equip") || args[0].equalsIgnoreCase("force"))) {
            if (sender.hasPermission("starterpack.admin")) {
                String input = args[1].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            if (sender.hasPermission("starterpack.admin")) {
                String input = args[1].toLowerCase();
                if ("all".startsWith(input)) {
                    completions.add("all");
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}
