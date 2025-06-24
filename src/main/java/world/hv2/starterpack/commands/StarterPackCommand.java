package world.hv2.starterpack.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

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
                sendMessage(sender, "&cUnknown command. Use &f/starterpack help &cfor available commands.");
                return true;
        }
    }
    
    /**
     * Handle reload command
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("starterpack.admin")) {
            sendMessage(sender, "&cYou don't have permission to reload the configuration.");
            return true;
        }
        
        try {
            plugin.reloadPluginConfig();
            sendMessage(sender, "&aConfiguration reloaded successfully!");
        } catch (Exception e) {
            sendMessage(sender, "&cError reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * Handle give command
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("starterpack.admin")) {
            sendMessage(sender, "&cYou don't have permission to give starter packs.");
            return true;
        }
        
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /starterpack give <player>");
            return true;
        }
        
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            sendMessage(sender, "&cPlayer '" + playerName + "' not found or not online.");
            return true;
        }
        
        boolean success = plugin.getStarterPackManager().forceGiveStarterPack(target);
        
        if (success) {
            sendMessage(sender, "&aSuccessfully gave starter pack to " + target.getName() + "!");
            sendMessage(target, "&aYou have been given a starter pack by " + sender.getName() + "!");
        } else {
            sendMessage(sender, "&cFailed to give starter pack to " + target.getName() + ".");
        }
        
        return true;
    }
    
    /**
     * Handle reset command
     */
    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("starterpack.admin")) {
            sendMessage(sender, "&cYou don't have permission to reset starter pack data.");
            return true;
        }
        
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /starterpack reset <player|all>");
            return true;
        }
        
        String target = args[1];
        
        if (target.equalsIgnoreCase("all")) {
            int resetCount = plugin.getStarterPackManager().resetAllPlayersStarterPack();
            if (resetCount >= 0) {
                sendMessage(sender, "&aSuccessfully reset starter pack status for " + resetCount + " online players!");
                sendMessage(sender, "&7Note: This only affects online players. Offline players will retain their status.");
            } else {
                sendMessage(sender, "&cFailed to reset starter pack status for all players.");
            }
            return true;
        }
        
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer == null) {
            sendMessage(sender, "&cPlayer '" + target + "' not found or not online.");
            return true;
        }
        
        boolean success = plugin.getStarterPackManager().resetPlayerStarterPack(targetPlayer);
        if (success) {
            sendMessage(sender, "&aSuccessfully reset starter pack status for " + targetPlayer.getName() + "!");
            sendMessage(targetPlayer, "&eYour starter pack status has been reset by " + sender.getName() + ". You can receive it again on next join!");
        } else {
            sendMessage(sender, "&cFailed to reset starter pack status for " + targetPlayer.getName() + ".");
        }
        
        return true;
    }
    
    /**
     * Handle stats command
     */
    private boolean handleStats(CommandSender sender) {
        sendMessage(sender, "&6=== StarterPack Statistics ===");
        String stats = plugin.getStarterPackManager().getStarterPackStats();
        sendMessage(sender, "&7" + stats);
        return true;
    }
    
    /**
     * Handle version command
     */
    private boolean handleVersion(CommandSender sender) {
        sendMessage(sender, "&6StarterPack Plugin");
        sendMessage(sender, "&7Version: &f" + plugin.getDescription().getVersion());
        sendMessage(sender, "&7Author: &f" + plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", ""));
        sendMessage(sender, "&7Website: &f" + plugin.getDescription().getWebsite());
        sendMessage(sender, "&7Description: &f" + plugin.getDescription().getDescription());
        
        // Show configuration status
        sendMessage(sender, "");
        sendMessage(sender, "&6Configuration Status:");
        sendMessage(sender, "&7Enabled: " + (plugin.getConfigManager().isStarterPackEnabled() ? "&aYes" : "&cNo"));
        sendMessage(sender, "&7Broadcast: " + (plugin.getConfigManager().isBroadcastEnabled() ? "&aYes" : "&cNo"));
        sendMessage(sender, "&7Debug: " + (plugin.getConfigManager().isDebugEnabled() ? "&aYes" : "&cNo"));
        
        return true;
    }
    
    /**
     * Send help message
     */
    private void sendHelpMessage(CommandSender sender) {
        sendMessage(sender, "&6=== StarterPack Commands ===");
        sendMessage(sender, "&f/starterpack help &7- Show this help message");
        sendMessage(sender, "&f/starterpack version &7- Show plugin version and status");
        
        if (sender.hasPermission("starterpack.admin")) {
            sendMessage(sender, "&f/starterpack reload &7- Reload configuration");
            sendMessage(sender, "&f/starterpack give <player> &7- Give starter pack to a player");
            sendMessage(sender, "&f/starterpack reset <player|all> &7- Reset starter pack status");
            sendMessage(sender, "&f/starterpack stats &7- Show starter pack statistics");
        }
        
        sendMessage(sender, "");
        sendMessage(sender, "&7Aliases: &f/sp, /starter");
    }
    
    /**
     * Send formatted message to sender
     */
    private void sendMessage(CommandSender sender, String message) {
        String prefix = "&8[&6StarterPack&8] ";
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "version");
            
            if (sender.hasPermission("starterpack.admin")) {
                subCommands = Arrays.asList("help", "version", "reload", "give", "reset", "stats");
            }
            
            String input = args[0].toLowerCase();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
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
