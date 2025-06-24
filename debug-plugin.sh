#!/bin/bash
# Debug and test script for StarterPack plugin
# Run this after server is running to test plugin functionality

set -e

echo "🎒 StarterPack Plugin - Debug and Test Script"
echo "=============================================="

# Check if server is running
if ! docker-compose ps | grep -q "Up"; then
    echo "❌ Server is not running. Start it with: make start"
    exit 1
fi

echo "✅ Server is running"
echo ""

# Get container name
CONTAINER=$(docker-compose ps -q minecraft)

echo "📋 Available Debug Commands:"
echo ""
echo "=== Player Information ==="
echo "1. List online players:"
echo "   docker exec -i $CONTAINER rcon-cli list"
echo ""
echo "2. Check a player's starter pack status:"
echo "   docker exec -i $CONTAINER rcon-cli 'starterpack stats'"
echo ""
echo "=== Testing Commands ==="
echo "3. Give starter pack to a player:"
echo "   docker exec -i $CONTAINER rcon-cli 'starterpack give <player>'"
echo ""
echo "4. Reset a player's starter pack:"
echo "   docker exec -i $CONTAINER rcon-cli 'starterpack reset <player>'"
echo ""
echo "5. Reset all players' starter packs:"
echo "   docker exec -i $CONTAINER rcon-cli 'starterpack reset all'"
echo ""
echo "6. Reload plugin configuration:"
echo "   docker exec -i $CONTAINER rcon-cli 'starterpack reload'"
echo ""
echo "=== Configuration ==="
echo "7. View current config:"
echo "   docker exec $CONTAINER cat /minecraft/plugins/StarterPack/config.yml"
echo ""
echo "8. View plugin logs:"
echo "   docker exec $CONTAINER tail -f /minecraft/logs/latest.log | grep -i starterpack"
echo ""

# Interactive mode
echo "🎮 Interactive Mode - Choose an option:"
echo "1) List online players"
echo "2) Show plugin stats"
echo "3) View plugin logs (last 50 lines)"
echo "4) Test with a specific player"
echo "5) Reset all players"
echo "6) View configuration"
echo "7) Open server console"
echo "q) Quit"
echo ""

read -p "Choice: " choice

case $choice in
    1)
        echo "📋 Online Players:"
        docker exec -i $CONTAINER rcon-cli list
        ;;
    2)
        echo "📊 Plugin Statistics:"
        docker exec -i $CONTAINER rcon-cli 'starterpack stats'
        ;;
    3)
        echo "📜 Recent Plugin Logs:"
        docker exec $CONTAINER tail -n 50 /minecraft/logs/latest.log | grep -i -A2 -B2 starterpack || echo "No StarterPack logs found"
        ;;
    4)
        read -p "Enter player name to test: " player
        echo "🎁 Giving starter pack to $player:"
        docker exec -i $CONTAINER rcon-cli "starterpack give $player"
        echo ""
        echo "📊 Updated statistics:"
        docker exec -i $CONTAINER rcon-cli 'starterpack stats'
        ;;
    5)
        read -p "Reset all players' starter pack status? (y/N): " confirm
        if [[ $confirm =~ ^[Yy]$ ]]; then
            echo "🔄 Resetting all players:"
            docker exec -i $CONTAINER rcon-cli 'starterpack reset all'
        else
            echo "Canceled."
        fi
        ;;
    6)
        echo "⚙️ Current Configuration:"
        docker exec $CONTAINER cat /minecraft/plugins/StarterPack/config.yml
        ;;
    7)
        echo "🖥️ Opening server console (Ctrl+C to exit):"
        docker exec -it $CONTAINER rcon-cli
        ;;
    q|Q)
        echo "Goodbye!"
        exit 0
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "✅ Debug session complete!"
