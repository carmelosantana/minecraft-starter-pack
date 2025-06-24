# StarterPack Plugin - Debug and Testing Guide

## Overview

The StarterPack plugin now includes enhanced debugging capabilities and reset functionality to help with development and server administration.

## Player Tracking System

The plugin tracks players who have received starter packs using:

1. **PersistentDataContainer (PDC)** - Stores data directly with the player's data file
2. **In-memory cache** - For performance during server runtime
3. **NamespacedKey** - `world.hv2.starterpack:received_starter_pack`

### How It Works
- When a player joins, the plugin checks if they have the PDC marker
- If not found, they receive the starter pack and get marked
- Data persists across server restarts and survives player name changes
- Uses UUID-based tracking for reliability

## New Commands

### Admin Commands (require `starterpack.admin` permission):

#### `/starterpack reset <player>`
- Resets a specific player's starter pack status
- Removes both in-memory cache and persistent data
- Player can receive starter pack again on next join

#### `/starterpack reset all`
- Resets starter pack status for ALL online players
- ⚠️ **Warning**: Only affects online players - offline players retain their status
- Use with caution on production servers

#### `/starterpack stats`
- Shows distribution statistics
- Displays: total online players, players with packs, new players

## Configuration Synchronization

### Docker and Make Integration
Both Docker and local development now use the same configuration approach:

- **Docker**: Uses `./config.yml` (root directory)
- **Local**: Uses `src/main/resources/config.yml` 
- **Development**: Can override with custom config file

### Configuration Files
1. `src/main/resources/config.yml` - Default plugin configuration
2. `config.yml` - Docker/development override configuration
3. Server creates `plugins/StarterPack/config.yml` at runtime

## Development Workflow

### Quick Testing
```bash
# Build and install plugin
make build install

# Test specific commands
make test-commands

# Interactive debugging
make debug

# Full development cycle
make dev
```

### Debug Script
The `debug-plugin.sh` script provides interactive testing:

```bash
# Run interactively
./debug-plugin.sh

# Or via make
make debug
```

Features:
- Lists online players
- Shows plugin statistics
- Tests commands with real players
- Views logs and configuration
- Opens server console

### Testing Scenarios

1. **New Player Test**:
   ```bash
   # Reset a player
   /starterpack reset <player>
   # Have them join server
   # Verify they receive pack
   ```

2. **Statistics Monitoring**:
   ```bash
   # Check current stats
   /starterpack stats
   # Reset some players
   /starterpack reset <player>
   # Check updated stats
   ```

3. **Bulk Reset Test**:
   ```bash
   # Reset all online players
   /starterpack reset all
   # Check stats to confirm
   /starterpack stats
   ```

## Docker Development

### Build and Test
```bash
# Build plugin and Docker container
make docker-build

# Run full Docker test
make docker-test

# Debug running container
make debug
```

### Configuration Override
The Docker container uses `config.yml` in the project root, allowing easy customization without rebuilding.

## Troubleshooting

### Common Issues

1. **Permission Errors**
   - Ensure players have `starterpack.admin` for admin commands
   - Default: only ops can use admin commands

2. **Reset Not Working**
   - `reset all` only affects online players
   - Offline players need individual reset when they join

3. **Stats Not Updating**
   - Stats show current online players only
   - Restart server to clear in-memory cache completely

### Debug Information

Enable debug mode in `config.yml`:
```yaml
debug:
  enabled: true
  log-all-joins: true
```

Check logs for detailed information:
```bash
# Docker
docker exec minecraft tail -f /minecraft/logs/latest.log | grep StarterPack

# Local server
tail -f server/logs/latest.log | grep StarterPack
```

## Best Practices

1. **Testing**:
   - Always test reset commands on development server first
   - Use stats command to monitor distribution
   - Test with multiple players if possible

2. **Production**:
   - Backup player data before mass resets
   - Use individual resets rather than `reset all`
   - Monitor logs for any issues

3. **Development**:
   - Use debug mode during development
   - Keep config.yml in sync between environments
   - Test both Docker and local environments
