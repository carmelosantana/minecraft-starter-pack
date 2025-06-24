# 🎒 StarterPack Plugin

A lightweight Minecraft plugin that gives **first-time players** a configurable **starter inventory** on join — no setup required. Ideal for survival or adventure servers that want to give players a head start with gear, tools, food, or books.

## ✨ Features

- 🎁 Gives new players a **starter item pack**
- ✅ **Zero configuration required** — default pack included
- 🛠 Easily customizable via `config.yml`
- 🔐 Safe and **graceful error handling** (e.g., invalid materials won't crash)
- 📚 Supports **custom names, lore, and enchantments**
- ⚡ Event-driven with **minimal performance impact**
- 🚫 **One pack per player** — uses persistent data to track
- 🔧 Admin commands for management and testing

## 📦 Default Starter Pack

By default, players joining the server for the first time will receive:

| Item                 | Name                    | Description                   |
|----------------------|--------------------------|-------------------------------|
| 🥩 Cooked Beef ×16    | —                        | Basic starter food            |
| 🛠 Diamond Pickaxe     | §bStarter Pickaxe        | Unbreaking III, Efficiency II |
| 🍞 Gluten-Free Bread ×3 | §fGluten-Free Bread       | Bonus healing food            |

## 🚀 Quick Install

1. Download the latest JAR from releases
2. Place in your server's `plugins/` directory
3. Restart your server
4. Join the server with a new player account to receive items
5. Optionally, configure in `plugins/StarterPack/config.yml`

## ⚙️ Configuration

Edit `plugins/StarterPack/config.yml`:

```yaml
starter-pack:
  enabled: true
  broadcast: true
  welcome-message: "&aWelcome to the server, &b{player}&a!"

  items:
    - material: DIAMOND_PICKAXE
      name: "&bStarter Pickaxe"
      lore:
        - "&7A durable tool to begin your journey"
        - "&7Unbreaking III, Efficiency II"
      enchantments:
        UNBREAKING: 3
        EFFICIENCY: 2
      amount: 1

    - material: COOKED_BEEF
      amount: 16

    - material: BREAD
      name: "&fGluten-Free Bread"
      lore:
        - "&7Healthier than wheat bread"
        - "&7Light and energizing"
        - "&aRestores extra hunger points"
      amount: 3
```

### 🔁 Placeholders

- `{player}` — replaced with the new player's username in messages

## 🛠 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/starterpack help` | Show help message | `starterpack.use` |
| `/starterpack version` | Display plugin info | `starterpack.use` |
| `/starterpack reload` | Reload config file | `starterpack.admin` |
| `/starterpack give <player>` | Give pack to any player | `starterpack.admin` |

**Aliases**: `/sp`, `/starter`

## 🔧 Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/starterpack help` | Show command help | All users |
| `/starterpack version` | Display plugin info | All users |
| `/starterpack reload` | Reload configuration | `starterpack.admin` |
| `/starterpack give <player>` | Give starter pack to any player | `starterpack.admin` |
| `/starterpack reset <player>` | Reset player's starter pack status | `starterpack.admin` |
| `/starterpack reset all` | Reset all online players | `starterpack.admin` |
| `/starterpack stats` | Show distribution statistics | `starterpack.admin` |

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `starterpack.use` | Basic command usage | `true` |
| `starterpack.admin` | Admin commands | `op` |
| `starterpack.bypass` | Don't receive starter pack | `false` |

## 🏗 Development

### Prerequisites

- Java 21+
- Maven 3.6+
- Docker (for testing)

### Building

```bash
# Clone the repository
git clone https://github.com/carmelosantana/starter-pack
cd starter-pack

# Build the plugin
make build

# Set up development server
make setup

# Start the server
make start

# Quick development cycle (build + install + restart)
make dev
```

### Available Make Targets

| Target | Description |
|--------|-------------|
| `make setup` | Initial server setup |
| `make build` | Build the plugin JAR |
| `make start` | Start the test server |
| `make stop` | Stop the test server |
| `make restart` | Restart the server |
| `make dev` | Quick development cycle |
| `make test` | Run tests |
| `make docker-test` | Test in Docker container |
| `make clean` | Clean server files |
| `make logs` | Show server logs |

### Testing

The plugin includes both unit tests and integration tests:

```bash
# Run unit tests
make test

# Test in Docker container
make docker-test

# Test on local server
make setup
make dev
```

### Server Management

The project includes a comprehensive server management script:

```bash
# Direct script usage
./server-manager.sh setup
./server-manager.sh start
./server-manager.sh attach  # Attach to server console
./server-manager.sh players # Show online players
```

## 🐳 Docker Support

Test the plugin in an isolated Docker environment:

```bash
# Build and test in Docker
make docker-test

# Or use docker-compose directly
docker-compose up -d
docker-compose logs -f minecraft
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and test thoroughly
4. Run tests: `make test`
5. Test in Docker: `make docker-test`
6. Submit a pull request

### Guidelines

- Follow existing code style
- Add tests for new features
- Update documentation as needed
- Test on both local server and Docker
- Ensure compatibility with Paper 1.21.6+

## 📋 Requirements

- **Server**: Paper 1.21.6+ (Spigot/Bukkit compatible)
- **Java**: 21 or higher
- **Players**: Works with both Java and Bedrock editions (via Geyser)

## 📝 License

Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)

## 👤 Author

**Carmelo Santana**
- Website: [hv2.world](https://hv2.world)
- GitHub: [@carmelosantana](https://github.com/carmelosantana)
- Test Server: `play.hv2.world`

## 🐛 Issues & Support

- Report bugs on [GitHub Issues](https://github.com/carmelosantana/starter-pack/issues)
- Join our test server: `play.hv2.world`
- Check the [Wiki](https://github.com/carmelosantana/starter-pack/wiki) for guides

## 🔄 Version History

- **1.0.0** - Initial release
  - Basic starter pack functionality
  - Configurable items with enchantments
  - Admin commands and permissions
  - Docker support for testing
  - Comprehensive server management tools
