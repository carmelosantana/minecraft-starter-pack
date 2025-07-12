#!/bin/bash

# StarterPack Plugin Server Manager
# Manages a local Minecraft server for plugin development and testing

set -e

# Configuration
SERVER_DIR="server"
PLUGIN_NAME="starter-pack"
PLUGIN_JAR="target/${PLUGIN_NAME}-1.0.2.jar"
SERVER_JAR="paper.jar"
MIN_RAM="2G"
MAX_RAM="4G"
SERVER_PORT="25565"
JAVA_ARGS="-Xms${MIN_RAM} -Xmx${MAX_RAM} -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java is installed and get version
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 21 or higher."
        exit 1
    fi
    
    local java_version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    
    if [[ "$java_version" -lt 21 ]]; then
        print_error "Java 21 or higher is required. Found Java $java_version"
        exit 1
    fi
    
    print_success "Java $java_version detected"
}

# Download Paper server if not exists
download_paper() {
    if [[ ! -f "$SERVER_DIR/$SERVER_JAR" ]]; then
        print_status "Downloading Paper server..."
        mkdir -p "$SERVER_DIR"
        
        # Get latest Paper build for 1.21.6
        local paper_version="1.21.6"
        local paper_url="https://api.papermc.io/v2/projects/paper/versions/${paper_version}/builds"
        local latest_build
        latest_build=$(curl -s "$paper_url" | grep -o '"build":[0-9]*' | tail -1 | cut -d':' -f2)
        
        if [[ -z "$latest_build" ]]; then
            print_error "Failed to get latest Paper build"
            exit 1
        fi
        
        local download_url="https://api.papermc.io/v2/projects/paper/versions/${paper_version}/builds/${latest_build}/downloads/paper-${paper_version}-${latest_build}.jar"
        
        print_status "Downloading Paper ${paper_version} build ${latest_build}..."
        curl -o "$SERVER_DIR/$SERVER_JAR" "$download_url"
        
        print_success "Paper server downloaded"
    else
        print_status "Paper server already exists"
    fi
}

# Setup server environment
setup_server() {
    print_status "Setting up server environment..."
    
    check_java
    download_paper
    
    # Create server directory
    mkdir -p "$SERVER_DIR/plugins"
    
    # Accept EULA
    echo "eula=true" > "$SERVER_DIR/eula.txt"
    
    # Create server.properties
    cat > "$SERVER_DIR/server.properties" << EOF
#Minecraft server properties
server-port=${SERVER_PORT}
gamemode=survival
difficulty=normal
spawn-protection=0
max-players=20
online-mode=false
white-list=false
motd=StarterPack Plugin Test Server
view-distance=10
simulation-distance=10
enable-command-block=true
op-permission-level=4
EOF

    print_success "Server environment setup complete"
}

# Build and install plugin
install_plugin() {
    if [[ ! -f "$PLUGIN_JAR" ]]; then
        print_error "Plugin JAR not found. Run 'make build' first."
        exit 1
    fi
    
    print_status "Installing plugin..."
    cp "$PLUGIN_JAR" "$SERVER_DIR/plugins/"
    print_success "Plugin installed"
}

# Start the server
start_server() {
    if is_server_running; then
        print_warning "Server is already running"
        return
    fi
    
    if [[ ! -f "$SERVER_DIR/$SERVER_JAR" ]]; then
        print_error "Server JAR not found. Run 'make setup' first."
        exit 1
    fi
    
    print_status "Starting Minecraft server..."
    
    cd "$SERVER_DIR"
    screen -dmS minecraft java $JAVA_ARGS -jar "$SERVER_JAR" --nogui
    cd ..
    
    # Wait for server to start
    local attempts=0
    while ! is_server_running && [[ $attempts -lt 30 ]]; do
        sleep 2
        ((attempts++))
        echo -n "."
    done
    echo ""
    
    if is_server_running; then
        print_success "Server started successfully"
        print_status "Server is running on port $SERVER_PORT"
        print_status "Use 'make attach' to attach to the server console"
        print_status "Use 'make logs' to view server logs"
    else
        print_error "Server failed to start"
        exit 1
    fi
}

# Stop the server
stop_server() {
    if ! is_server_running; then
        print_warning "Server is not running"
        return
    fi
    
    print_status "Stopping server..."
    screen -S minecraft -p 0 -X stuff "stop$(printf \\r)"
    
    # Wait for server to stop
    local attempts=0
    while is_server_running && [[ $attempts -lt 30 ]]; do
        sleep 1
        ((attempts++))
    done
    
    if ! is_server_running; then
        print_success "Server stopped successfully"
    else
        print_warning "Server did not stop gracefully, killing process..."
        screen -S minecraft -X quit
        sleep 2
        if is_server_running; then
            print_error "Failed to stop server"
            exit 1
        else
            print_success "Server stopped forcefully"
        fi
    fi
}

# Check if server is running
is_server_running() {
    screen -list | grep -q "minecraft"
}

# Show server status
show_status() {
    if is_server_running; then
        print_success "Server is running"
        
        # Show server info
        local server_pid
        server_pid=$(screen -list | grep minecraft | cut -d. -f1 | xargs)
        if [[ -n "$server_pid" ]]; then
            print_status "Server PID: $server_pid"
        fi
        
        # Check if port is open
        if command -v lsof &> /dev/null; then
            if lsof -i ":$SERVER_PORT" &> /dev/null; then
                print_status "Server is listening on port $SERVER_PORT"
            else
                print_warning "Server process running but not listening on port $SERVER_PORT"
            fi
        fi
    else
        print_warning "Server is not running"
    fi
}

# Show recent server logs
show_logs() {
    if [[ -f "$SERVER_DIR/logs/latest.log" ]]; then
        tail -n 50 "$SERVER_DIR/logs/latest.log"
    else
        print_warning "No server logs found"
    fi
}

# Attach to server console
attach_server() {
    if ! is_server_running; then
        print_error "Server is not running"
        exit 1
    fi
    
    print_status "Attaching to server console..."
    print_status "Use Ctrl+A then D to detach from the console"
    screen -r minecraft
}

# Reset server (clean world and restart)
reset_server() {
    print_warning "This will delete the world and restart the server!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_status "Reset cancelled"
        return
    fi
    
    stop_server
    
    print_status "Removing world files..."
    rm -rf "$SERVER_DIR/world" "$SERVER_DIR/world_nether" "$SERVER_DIR/world_the_end"
    rm -f "$SERVER_DIR/usercache.json" "$SERVER_DIR/session.lock"
    
    install_plugin
    start_server
    
    print_success "Server reset complete"
}

# Clean all server files
clean_server() {
    print_warning "This will delete ALL server files!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_status "Clean cancelled"
        return
    fi
    
    stop_server
    
    print_status "Removing server files..."
    rm -rf "$SERVER_DIR"
    
    print_success "Server files cleaned"
}

# Show network information
show_network() {
    print_status "Network Configuration:"
    
    # Get local IP
    if command -v ifconfig &> /dev/null; then
        local_ip=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -1)
    elif command -v ip &> /dev/null; then
        local_ip=$(ip route get 8.8.8.8 | sed -n '/src/{s/.*src *\([^ ]*\).*/\1/p;q}')
    else
        local_ip="unknown"
    fi
    
    echo "Local IP: $local_ip"
    echo "Server Port: $SERVER_PORT"
    echo "Local Connection: $local_ip:$SERVER_PORT"
    
    # Check if port is open
    if command -v lsof &> /dev/null && lsof -i ":$SERVER_PORT" &> /dev/null; then
        print_success "Server is accepting connections on port $SERVER_PORT"
    else
        print_warning "Server is not listening on port $SERVER_PORT"
    fi
}

# Show online players
show_players() {
    if ! is_server_running; then
        print_error "Server is not running"
        exit 1
    fi
    
    print_status "Getting online players..."
    screen -S minecraft -p 0 -X stuff "list$(printf \\r)"
    sleep 1
    
    if [[ -f "$SERVER_DIR/logs/latest.log" ]]; then
        grep "There are" "$SERVER_DIR/logs/latest.log" | tail -1
    fi
}

# Main command handler
case "${1:-help}" in
    setup)
        setup_server
        ;;
    start)
        start_server
        ;;
    stop)
        stop_server
        ;;
    restart)
        stop_server
        sleep 2
        start_server
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    attach)
        attach_server
        ;;
    install)
        install_plugin
        ;;
    reset)
        reset_server
        ;;
    clean)
        clean_server
        ;;
    network)
        show_network
        ;;
    players)
        show_players
        ;;
    help|*)
        echo "StarterPack Plugin Server Manager"
        echo ""
        echo "Usage: $0 <command>"
        echo ""
        echo "Commands:"
        echo "  setup     - Set up server environment"
        echo "  start     - Start the server"
        echo "  stop      - Stop the server"
        echo "  restart   - Restart the server"
        echo "  status    - Show server status"
        echo "  logs      - Show recent server logs"
        echo "  attach    - Attach to server console"
        echo "  install   - Install/update plugin"
        echo "  reset     - Reset server (clean world, restart)"
        echo "  clean     - Remove all server files"
        echo "  network   - Show network configuration"
        echo "  players   - Show online players"
        echo "  help      - Show this help message"
        ;;
esac
