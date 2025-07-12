#!/bin/bash

# Docker Test Script for StarterPack Plugin
# Tests the plugin in a Docker container environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Check if Docker is running
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        print_error "Docker is not running"
        exit 1
    fi
    
    print_success "Docker is available"
}

# Check if plugin JAR exists
check_plugin() {
    if [[ ! -f "target/starter-pack-1.0.2.jar" ]]; then
        print_error "Plugin JAR not found. Run 'make build' first."
        exit 1
    fi
    
    print_success "Plugin JAR found"
}

# Clean up any existing containers
cleanup() {
    print_status "Cleaning up existing containers..."
    
    if docker-compose ps | grep -q "minecraft"; then
        docker-compose down --volumes
    fi
    
    # Remove any orphaned containers
    docker container prune -f &> /dev/null || true
    
    print_success "Cleanup complete"
}

# Start the container
start_container() {
    print_status "Starting Minecraft server container..."
    
    # Start the container in the background
    docker-compose up -d
    
    print_success "Container started"
}

# Wait for server to be ready
wait_for_server() {
    print_status "Waiting for server to start..."
    
    local attempts=0
    local max_attempts=60
    
    while [[ $attempts -lt $max_attempts ]]; do
        if docker-compose logs minecraft 2>/dev/null | grep -q "Done"; then
            print_success "Server is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 5
        ((attempts++))
    done
    
    echo ""
    print_error "Server failed to start within timeout"
    return 1
}

# Test plugin functionality
test_plugin() {
    print_status "Testing plugin functionality..."
    
    # Check if plugin loaded
    if docker-compose logs minecraft | grep -q "StarterPack plugin enabled"; then
        print_success "Plugin loaded successfully"
    else
        print_error "Plugin failed to load"
        show_logs
        return 1
    fi
    
    # Check for any startup errors
    if docker-compose logs minecraft | grep -i "error.*starter" | grep -v "WARNING"; then
        print_warning "Found potential plugin errors in logs"
        docker-compose logs minecraft | grep -i "error.*starter" | grep -v "WARNING"
    fi
    
    # Test plugin commands (if possible)
    print_status "Plugin appears to be working correctly"
    
    return 0
}

# Show server logs
show_logs() {
    print_status "Recent server logs:"
    echo "=========================="
    docker-compose logs --tail=50 minecraft
    echo "=========================="
}

# Show container status
show_status() {
    print_status "Container status:"
    docker-compose ps
    
    print_status "Resource usage:"
    docker stats --no-stream
}

# Main test sequence
run_tests() {
    print_status "Starting Docker test for StarterPack plugin..."
    
    check_docker
    check_plugin
    cleanup
    
    start_container
    
    if wait_for_server; then
        test_plugin
        show_status
        
        print_success "Docker test completed successfully!"
        print_status "Server is running and accessible on localhost:25565"
        print_status "Use 'docker-compose logs -f minecraft' to follow logs"
        print_status "Use 'docker-compose down' to stop the server"
        
        return 0
    else
        print_error "Docker test failed"
        show_logs
        cleanup
        return 1
    fi
}

# Handle script arguments
case "${1:-test}" in
    test)
        run_tests
        ;;
    start)
        check_docker
        check_plugin
        start_container
        ;;
    stop)
        cleanup
        ;;
    logs)
        show_logs
        ;;
    status)
        show_status
        ;;
    clean)
        cleanup
        print_status "Cleaning Docker images..."
        docker image prune -f
        ;;
    help|*)
        echo "Docker Test Script for StarterPack Plugin"
        echo ""
        echo "Usage: $0 <command>"
        echo ""
        echo "Commands:"
        echo "  test    - Run full test (default)"
        echo "  start   - Start container only"
        echo "  stop    - Stop and cleanup"
        echo "  logs    - Show container logs"
        echo "  status  - Show container status"
        echo "  clean   - Clean up containers and images"
        echo "  help    - Show this help"
        ;;
esac
