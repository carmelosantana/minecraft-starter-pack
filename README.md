# 🎒 StarterPack Plugin

A lightweight Minecraft plugin that gives **first-time players** a configurable **starter inventory** on join — no setup required. Ideal for survival or adventure servers that want to give players a head start with gear, tools, food, or books.

- [Features](#features)
- [Default Starter Pack](#default-starter-pack)
- [Quick Install](#quick-install)
- [Configuration](#configuration)
  - [🔁 Placeholders](#-placeholders)
- [Commands](#commands)
- [Admin Commands](#admin-commands)
- [Permissions](#permissions)
- [Contributing](#contributing)
  - [Developer Quick Start](#developer-quick-start)
- [🎓 Learn AI Powered Plugin Development](#-learn-ai-powered-plugin-development)
  - [What You'll Learn](#what-youll-learn)
  - [Course Topics](#course-topics)
  - [Booking Information](#booking-information)
    - [1-on-1 Coaching Sessions Available](#1-on-1-coaching-sessions-available)
    - [What's Included](#whats-included)
  - [Get Started](#get-started)
- [License](#license)

## Features

- 🎁 Gives new players a **starter item pack**
- ✅ **Zero configuration required** — default pack included
- 🛠 Easily customizable via `config.yml`
- 🔐 Safe and **graceful error handling** (e.g., invalid materials won't crash)
- 📚 Supports **custom names, lore, and enchantments**
- ⚡ Event-driven with **minimal performance impact**
- 🚫 **One pack per player** — uses persistent data to track
- 🔧 Admin commands for management and testing
- 🛡️ **Auto-equip starter armor and tools** for new players (if they don't have any)
- ⚔️ **Separate equipment section** in config for armor and tools
- 🎯 **Smart equipment detection** — won't equip if player already has gear

## Default Starter Pack

By default, players joining the server for the first time will receive:

**Inventory Items:**
| Item                 | Name                    | Description                   |
|----------------------|--------------------------|-------------------------------|
| 🥩 Cooked Beef ×16    | —                        | Basic starter food            |
| 🛠 Diamond Pickaxe     | §bStarter Pickaxe        | Unbreaking III, Efficiency II |
| 🍞 Gluten-Free Bread ×3 | §fGluten-Free Bread       | Bonus healing food            |

**Auto-Equipped Items (if player has no equipment):**
| Item                 | Name                    | Description                   |
|----------------------|--------------------------|-------------------------------|
| 🪖 Leather Helmet     | §bStarter Helmet         | Protection I                  |
| 🦺 Leather Chestplate | §bStarter Chestplate     | Protection I                  |
| 👖 Leather Leggings   | §bStarter Leggings       | Protection I                  |
| 🥾 Leather Boots      | §bStarter Boots          | Protection I                  |
| ⚔️ Stone Sword        | §bStarter Sword          | Sharpness I                   |

## Quick Install

1. Download the latest JAR from releases
2. Place in your server's `plugins/` directory
3. Restart your server
4. Join the server with a new player account to receive items
5. Optionally, configure in `plugins/StarterPack/config.yml`

## Configuration

Edit `plugins/StarterPack/config.yml`:

```yaml
starter-pack:
  enabled: true
  broadcast: true
  welcome-message: "&aWelcome to the server, &b{player}&a!"

  # Items to give to inventory
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

  # Equipment to auto-equip (only if player has no equipment)
  equipment:
    - material: LEATHER_HELMET
      name: "&bStarter Helmet"
      lore:
        - "&7Basic protection for your head"
      enchantments:
        PROTECTION: 1
      amount: 1
      slot: helmet

    - material: STONE_SWORD
      name: "&bStarter Sword"
      lore:
        - "&7A basic weapon for defense"
      enchantments:
        SHARPNESS: 1
      amount: 1
      slot: mainhand
```

### 🔁 Placeholders

- `{player}` — replaced with the new player's username in messages

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/starterpack help` | Show help message | `starterpack.use` |
| `/starterpack version` | Display plugin info | `starterpack.use` |
| `/starterpack reload` | Reload config file | `starterpack.admin` |
| `/starterpack give <player>` | Give pack to any player | `starterpack.admin` |
| `/starterpack equip <player>` | Equip starter items (only if no equipment) | `starterpack.admin` |
| `/starterpack force <player>` | Force equip starter items (overrides existing) | `starterpack.admin` |

**Aliases**: `/sp`, `/starter`

## Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/starterpack help` | Show command help | All users |
| `/starterpack version` | Display plugin info | All users |
| `/starterpack reload` | Reload configuration | `starterpack.admin` |
| `/starterpack give <player>` | Give starter pack to any player | `starterpack.admin` |
| `/starterpack equip <player>` | Equip starter items (only if no equipment) | `starterpack.admin` |
| `/starterpack force <player>` | Force equip starter items (overrides existing) | `starterpack.admin` |
| `/starterpack reset <player>` | Reset player's starter pack status | `starterpack.admin` |
| `/starterpack reset all` | Reset all online players | `starterpack.admin` |
| `/starterpack stats` | Show distribution statistics | `starterpack.admin` |

### Equipment Commands

- **`/starterpack equip <player>`**: Equips the player with starter equipment only if they don't currently have any armor or tools equipped. This is perfect for players who may have lost their equipment.

- **`/starterpack force <player>`**: Forces equipment on the player, moving their current equipment to inventory (or dropping it if inventory is full). This completely replaces their current equipment with the starter equipment.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `starterpack.use` | Basic command usage | `true` |
| `starterpack.admin` | Admin commands | `op` |
| `starterpack.bypass` | Don't receive starter pack | `false` |

## Contributing

Please review [Contributing Guidelines](./CONTRIBUTING.md) for developer documentation. Including;

- Development environment setup
- Code standards and testing procedures
- Pull request process
- Server management and Docker support
- Troubleshooting

### Developer Quick Start

```bash
# Set up development environment
make setup

# Quick development cycle
make dev

# Run tests
make test

# Test in Docker
make docker-test
```

## 🎓 Learn AI Powered Plugin Development

**Want to build your own Minecraft plugins?** I built our plugin collection using generative AI, and I can teach you how to do the same!

### What You'll Learn

- **AI Assisted Coding**: How to effectively use AI tools like GitHub Copilot, ChatGPT, and Claude for plugin development
- **Plugin Architecture**: Best practices for structuring robust, maintainable Minecraft plugins
- **Modern Development**: Paper API, Maven build systems, Docker testing, and CI/CD workflows
- **Problem Solving**: How to break down complex features into manageable tasks
- **Code Quality**: Testing, debugging, and optimizing AI generated code

### Course Topics

- **Getting Started**: Setting up your AI development environment
- **Plugin Fundamentals**: Events, commands, configuration, and permissions
- **Advanced Features**: Custom items, recipes, data persistence, and performance optimization
- **Testing & Deployment**: Docker containers, server management, and release workflows
- **Real Projects**: Build actual plugins from concept to completion

### Booking Information

#### 1-on-1 Coaching Sessions Available

- **Duration**: 1-2 hour sessions
- **Format**: Screen share coding sessions via video call
- **Family Friendly**: Parents are welcome and encouraged to join sessions, especially for younger students and curious parents wanting to learn alongside their children.

#### What's Included

- ✅ Live coding demonstration
- ✅ AI prompt engineering techniques
- ✅ Complete project setup and tooling
- ✅ Plugin publishing and distribution
- ✅ Follow up support and code review

### Get Started

Ready to accelerate your development with AI?

- **[Schedule your call](https://cal.com/carmelosantana/learn-minecraft-with-ai)** - Book a session today!
- **[Discord](https://discord.gg/udbJu8Sbyj)** - Ask questions, see examples.
- **Public SMP Server** - Join us at `play.xp.farm` and test our plugins live!

*Turn your plugin ideas into reality in hours, not weeks!*

## License

This project is licensed under the [Creative Commons Attribution-NonCommercial 4.0 International License](https://creativecommons.org/licenses/by-nc/4.0/).
