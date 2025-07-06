# Contributing Guidelines

- [Getting Started](#getting-started)
- [Development Environment](#development-environment)
- [Code Standards](#code-standards)
- [Testing](#testing)
- [Pull Request Process](#pull-request-process)
- [Server Management](#server-management)
- [Docker Support](#docker-support)
- [Troubleshooting](#troubleshooting)
- [Community](#community)

## Requirements

### Development Requirements

- **Java**: 21 or higher
- **Maven**: For building projects
- **Docker**: For containerized testing (optional)
- **Git**: For version control
- **IDE**: VS Code recommended

### Server Requirements

- **Server**: Paper `1.21.6+` (recommended)
- **Memory**: Minimum 2GB RAM for development
- **Storage**: ~1GB free space for container files

## Getting Started

### Prerequisites

- **Java**: 21 or higher
- **Maven**: 3.6+ for building projects
- **Docker**: Cross platform testing (optional)
- **Git**: For version control
- **IDE**: VS Code, IntelliJ IDEA or Eclipse

### How to Make a Clean Pull Request

1. **Fork the Project**
   - Create a personal fork of the project on GitHub
   - Clone the fork on your local machine. Your remote repo on GitHub is called `origin`
   - Add the original repository as a remote called `upstream`

2. **Stay Up to Date**
   - If you created your fork a while ago, be sure to pull upstream changes into your local repository
   - Create a new branch to work on. Branch from `develop` if it exists, else from `main`

3. **Make Your Changes**
   - Implement/fix your feature, comment your code
   - Follow the code style of the project, including indentation
   - If the project has tests, please run them
   - Write or adapt tests as needed
   - Add or update the documentation as needed

4. **Prepare Your Contribution**
   - Squash your commits into a single commit with git's [interactive rebase](https://help.github.com/articles/interactive-rebase). Create a new branch if necessary
   - Push your branch to your fork on GitHub, the remote `origin`
   - From your fork open a pull request in the correct branch. Target the project's `develop` branch if there is one, else go for `main`!

5. **Follow Up**
   - If changes are requested, just push them to your branch. The PR will be updated automatically
   - Once the pull request is approved and merged you can pull the changes from `upstream` to your local repo and delete your extra branch(es)

**Important**: Please write your commit messages in the present tense. Your commit message should describe what the commit, when applied, does to the code – not what you did to the code.

## Development Environment

### Initial Setup

All plugins use a consistent development workflow with Make-based automation;

```bash
# Clone any plugin repository
git clone <plugin-repository>
cd <plugin-directory>

# Set up development environment
make setup

# Build the plugin
make build

# Start development server
make start
```

### Development Workflow

These plugins follow a standardized development cycle;

```bash
# Quick development cycle (build + install + restart)
make dev

# View server logs
make logs

# Check server status
make status

# Stop the server
make stop

# Clean up for fresh start
make clean
```

### Available Make Targets

Every plugin supports these standard Make targets;

| Target | Description |
|--------|-------------|
| `make help` | Show all available commands |
| `make setup` | Initial server setup and dependencies |
| `make build` | Build the plugin JAR |
| `make start` | Start the test server |
| `make stop` | Stop the test server |
| `make restart` | Restart the server |
| `make dev` | Quick development cycle (build + install + restart) |
| `make test` | Run unit tests |
| `make docker-test` | Test in Docker container |
| `make clean` | Clean server files |
| `make logs` | Show server logs |
| `make status` | Check server status |
| `make debug` | Interactive debug menu |

## Code Standards

### Java Coding Standards

- **Java Version**: Target Java 21+ features
- **Code Style**: ~~Follow standard Java conventions~~ Mostly output from Claude and ChatGPT
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: ~Maximum 120 characters
- **Comments**: Use JavaDoc for public methods and classes

### Plugin Architecture

- **Event Driven**: Use Bukkit event system efficiently
- **Asynchronous Processing**: Network operations and heavy tasks should be async
- **Memory Efficient**: Implement proper cleanup and resource management
- **Error Handling**: Graceful degradation and comprehensive error handling

### Configuration Standards

- **YAML Format**: All configuration files use YAML
- **Validation**: Validate configuration on startup
- **Defaults**: Provide sensible defaults for all settings
- **Documentation**: Comment configuration options clearly

### Example Code Structure

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    // Use async processing for non-critical operations
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        // Heavy processing here
    });
}
```

## Testing

### Unit Testing

All plugins should include basic unit tests;

```bash
# Run unit tests
make test

# Run tests with verbose output
mvn test -X
```

### Integration Testing

Docker helps test cross platform compatibility;

```bash
# Test in Docker container
make docker-test

# Or use docker-compose directly
docker-compose up -d
docker-compose logs -f minecraft
```

### Manual Testing Scenarios

Each plugin should document specific testing scenarios. Common patterns include;

- **Permission Testing**: Verify commands work with correct permissions
- **Configuration Testing**: Test with various configuration settings
- **Error Handling**: Test with invalid inputs and edge cases
- **Performance Testing**: Verify no significant server lag

### Test Server Setup

The test servers use Paper `1.21.6+` with these standard configurations:

- **Server Properties**: Optimized for testing
- **Plugin Environment**: Isolated test environment
- **Geyser/Floodgate**: Cross-platform compatibility testing

## Pull Request Process

### Before Submitting

1. **Test Thoroughly**
   - Run unit tests: `make test`
   - Test in Docker: `make docker-test`
   - Test on local server: `make dev`

2. **Code Quality**
   - Follow coding standards
   - Add/update documentation
   - Include tests for new features

3. **Compatibility**
   - Ensure Paper `1.21.6+` compatibility
   - Test with Geyser/Floodgate if applicable
   - Verify Java 21+ compatibility

### PR Guidelines

- **Title**: Clear, descriptive title
- **Description**: Explain what changes were made and why
- **Testing**: Include testing steps and results
- **Breaking Changes**: Clearly note any breaking changes
- **Screenshots**: Include screenshots for UI changes

### Review Process

1. **Automated Checks**: All tests must pass
2. **Code Review**: Maintainer review required
3. **Testing**: Changes will be tested
4. **Merge**: Approved PRs are merged into main branch

## Server Management

### Server Manager Script

All plugins include a comprehensive server management script:

```bash
# Direct script usage
./server-manager.sh setup     # Initial setup
./server-manager.sh start     # Start server
./server-manager.sh stop      # Stop server
./server-manager.sh restart   # Restart server
./server-manager.sh attach    # Attach to server console
./server-manager.sh players   # Show online players
./server-manager.sh backup    # Create backup
./server-manager.sh restore   # Restore from backup
```

### Configuration Management

- **EULA**: Automatically accepted during setup
- **Server Properties**: Optimized for development
- **Plugin Configs**: Default configurations provided
- **World Generation**: Consistent world settings

## Docker Support

### Docker Testing

Docker support is provided for consistent testing environments:

```bash
# Build and test in Docker
make docker-test

# Custom Docker setup
docker-compose up -d
docker-compose exec minecraft bash
```

### Docker Configuration

Standard Docker setup includes:

- **Java Players**: Port `25565`
- **Bedrock Players**: Port `19132`
- **Volume Mounting**: For plugin development
- **Log Access**: Real-time log monitoring

## Troubleshooting

### Common Issues

**Plugin not loading:**

- Check Java version (requires 21+)
- Verify Paper/Spigot version compatibility
- Check server logs for specific errors
- Ensure plugin JAR is in correct location

**Build failures:**

- Verify Maven installation
- Check Java version compatibility
- Clear Maven cache: `mvn clean`
- Check for dependency conflicts

**Server startup issues:**

- Verify EULA acceptance
- Check available memory
- Ensure ports are not in use
- Review server logs for errors

### Debug Mode

Most plugins support debug mode for troubleshooting.

In `config.yml`;

```yaml
debug: true
```

or

```yaml
logging:
  debug: true
```

### Getting Help

1. **Check Documentation**: Review plugin-specific README
2. **Search Issues**: Look for similar problems in GitHub issues
3. **Server Logs**: Always include relevant log output
4. **Minimal Reproduction**: Provide steps to reproduce the issue

## Community

### Communication

- **GitHub Issues**: For bug reports and feature requests
- **[Discord](https://discord.gg/udbJu8Sbyj)**: Discuss new ideas or solutions
- **Test Server**: Join at `play.xp.farm` to test plugins live

## License

All plugins are licensed under the [Creative Commons Attribution-NonCommercial 4.0 International License](https://creativecommons.org/licenses/by-nc/4.0/).

---

Thank you for contributing to **XP Farm's** Minecraft plugin development community!
