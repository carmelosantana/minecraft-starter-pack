# New or Edited Plugin Checklist

This file records the **current real state** of an already-shipped, released plugin. It was created
retroactively during the `1.1.2` Floodgate bug-fix, not at the start of new work. Gates that were
not performed during this change are left unchecked with a short note rather than removed.

- Plugin name: `StarterPack`
- Slug: `starter-pack`
- Repository: `carmelosantana/minecraft-starter-pack`
- Owner: `Carmelo Santana`
- Target version: `1.1.2` (bug-fix patch over the released `1.1.1`)
- Paper version: `26.1.2 build 74`
- Java version: `25`
- Updater destination: `starter-pack.jar`
- External services: `none`
- Status: `active`
- Autonomy: `autonomous`

Maven `artifactId`: `starter-pack`. `plugin.yml` name: `StarterPack`. Releasable JAR:
`starter-pack-<version>.jar`.
Current released version at time of this change: `v1.1.1`.

## What `1.1.2` changes

Bedrock accounts join through Floodgate under a `.`-prefixed Java-side username — a player who
calls themself `carm` is `.acarm` on the server. All four player-taking subcommands used
`Bukkit.getPlayer(String)`, which matches a *prefix of the name*; `getPlayer("carm")` therefore
never matched `.acarm`, and the operator was told the player was not online.

A new `world.hv2.starterpack.util.PlayerLookup` tries the typed name, then the `.`-prefixed form,
exactly and then case-insensitively, and finally falls back to Bukkit's own partial matching so the
pre-existing partial-name behaviour is not regressed. Failed lookups now name who *is* online,
which is the only channel through which a Bedrock player can discover the prefixed form — Geyser
bakes the command tree into the login packet and never sends command-suggestion packets, so Bedrock
clients have no tab completion at all.

Rewired call sites, all in `src/main/java/world/hv2/starterpack/commands/StarterPackCommand.java`:
`give`, `equip`, `force`, and `reset`. In `reset`, the reserved literal `all` is still matched in an
early-return branch *before* any player lookup runs, so `/starterpack reset all` cannot be captured
by a player named `all`.

## 1. Scope

- [x] Status is explicitly recorded as active, experimental, or excluded.
- [x] Purpose, commands, events, permissions, configuration, persistence, and acceptance checks are defined. Recorded below from the shipped plugin; this change adds no command, permission, event, or config key.
- [x] Known limitations and any intentionally withheld gates are recorded.

### Player-facing purpose

StarterPack gives first-time players a configurable starter inventory on join, with admin commands
to hand out, equip, force-equip, and reset packs.

### Commands

| Command | Arguments | Who |
| --- | --- | --- |
| `/starterpack help` | — | `starterpack.use` |
| `/starterpack version` | — | `starterpack.use` |
| `/starterpack reload` | — | `starterpack.admin` |
| `/starterpack give` | `<player>` | `starterpack.admin` |
| `/starterpack equip` | `<player>` | `starterpack.admin` |
| `/starterpack force` | `<player>` | `starterpack.admin` |
| `/starterpack reset` | `<player\|all>` | `starterpack.admin` |
| `/starterpack stats` | — | `starterpack.admin` |

Aliases `/sp`, `/starter`. Unchanged in `1.1.2`.

### Events

`PlayerJoinEvent` — grants the starter pack on first join. Unchanged in `1.1.2`.

### Permissions

`starterpack.use` (default true), `starterpack.admin` (op), `starterpack.bypass` (false).
Unchanged in `1.1.2`.

### Configuration

`src/main/resources/config.yml`. No keys added, removed, or renamed in `1.1.2`. The Floodgate
prefix is deliberately **not** a config key — it is owned by Floodgate's own `config.yml`, and a
second copy here would be an unvalidatable source of truth that rots silently if only one is
changed. A server that has reconfigured the prefix still resolves through the case-insensitive
sweep.

### Persistence

Per-player metadata via the existing manager. No files or database added in `1.1.2`.

### Dependencies

Hard: none. Soft: none. `softdepend: [floodgate]` was **not** added — `PlayerLookup` works purely
on usernames and never touches `FloodgateApi`, so it needs no class from Floodgate and no load
ordering relative to it.

### Acceptance checks for `1.1.2`

1. `/starterpack give <bare-name>` finds a Floodgate Bedrock player whose real username is
   `.<x><bare-name>`. **NOT VERIFIED — no Bedrock client available.**
2. Same for `equip`, `force`, and `reset`. **NOT VERIFIED — no Bedrock client available.**
3. An exact Java username still resolves, and still wins over any partial match.
   Covered by unit tests at the candidate-list level only.
4. Partial-name matching that worked before still works (no regression from the previous
   `Bukkit.getPlayer` behaviour). Preserved by construction in `resolveAllowingPartial`;
   **not runtime-verified.**
5. `/starterpack reset all` still resets all online players and is not treated as a player name.
   Preserved by construction — the `all` branch returns before the lookup; **not runtime-verified.**
6. A failed lookup lists the online players. Unit-tested at the message level.
7. `mvn clean verify` passes. **Verified — see §6.**

### Known limitations

- **No runtime verification of this fix.** The behaviour that motivated the change — a real Bedrock
  client joining through Floodgate under a prefixed username — was not reproduced. The evidence for
  `1.1.2` is unit tests over the pure name-candidate logic plus a green build, nothing more.
- **`resolve` is not unit-tested.** It calls `Bukkit.getPlayerExact` / `Bukkit.getOnlinePlayers` and
  returns `Player`, none of which can be constructed headlessly, and no MockBukkit dependency exists
  in this repository. Only `targetNameCandidates` and `noSuchPlayerMessage` are covered.
- **The prefix is hardcoded to Floodgate's `.` default.** A server that changed it relies on the
  case-insensitive sweep and on Bukkit's partial match, not on an exact candidate.
- **Java package is `world.hv2.starterpack` while the Maven group is `org.xpfarm`.** Pre-existing
  inconsistency, untouched here; renaming the package is a breaking change and out of scope for a
  patch release.
- **Source files carry no license header.** The repository ships an AGPL-3.0 `LICENSE` file and
  Maven license metadata, but no `.java` file in this repository has a header comment. The new
  `PlayerLookup.java` matches that existing convention rather than introducing a header only one
  file would have.

## 2. Repository

- [x] Repository is `carmelosantana/minecraft-starter-pack` with an SSH `origin` and `main` branch.
- [x] Existing user-owned worktree changes were identified and preserved. Working tree was clean on `main` at `8049359` before branching.
- [x] No `herobrinesystems` references remain in source, metadata, workflows, remotes, or documentation.

## 3. Metadata

- [x] AGPL-3.0-or-later `LICENSE` and Maven license metadata are present and consistent.
- [x] `https://xpfarm.org` metadata and Carmelo Santana author metadata are present in `plugin.yml`.
- [ ] `play.xpfarm.org` is recorded as the public Minecraft server hostname where server identity is documented. Not checked during this change.
- [x] New work uses the `org.xpfarm` Maven group. `groupId` is `org.xpfarm`; the Java package predates it — see Known limitations.
- [x] Repository slug, artifact, releasable JAR, updater destination, and `plugin.yml` names are consistent.
- [x] No secrets committed in source, defaults, tests, logs, history, or documentation. No secret was introduced by this change.

Gates 2 and 3 were satisfied by the existing released plugin; `minecraft-plugin-scaffold` is not
re-run.

## 4. Compatibility

- [x] Java 25/Paper 26.1.2 build 74 compile succeeds and `plugin.yml` uses `api-version: '1.21'`. `mvn clean verify` green against `paper-api 26.1.2.build.74-stable`.
- [x] Hard dependencies, soft dependencies, optional APIs, and load ordering were reviewed and declared. None — see Dependencies.
- [x] Geyser/Floodgate/ViaVersion review covers Bedrock-safe input, UI, inventory, identity, and protocol behavior. **This change is exactly that review's output**: it fixes Bedrock *identity* handling (the Floodgate username prefix) and compensates for the Geyser limitation that Bedrock clients receive no command suggestions. No UI, inventory, or protocol surface is touched.

## 5. External services

- [x] External integrations are disabled by default or require explicit configuration and have bounded timeouts. No external integrations.
- [x] Ollama/Umami-style external endpoints are optional and failure-tolerant when applicable. Not applicable.
- [x] Endpoint failure cannot fail server/plugin startup, and diagnostics redact secrets. Not applicable.

## 6. Tests and build

- [x] Unit tests cover separable logic, configuration, serialization, permissions, and failure paths where applicable. 6 tests added in `PlayerLookupTest` covering `targetNameCandidates` and `noSuchPlayerMessage`; written failing first (7 `cannot find symbol` compile errors with the helper absent), then made to pass. See the coverage limits under Known limitations.
- [x] `mvn --batch-mode --no-transfer-progress clean verify` succeeds. `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`. The 2 pre-existing `StarterPackPluginTest` tests stayed green throughout.
- [x] The releasable JAR and embedded `plugin.yml` were inspected; `original-*` JARs are excluded. Verified by unzipping the built JAR. Embedded `plugin.yml` reads `version: '1.1.2'`, `api-version: '1.21'`, `main: world.hv2.starterpack.StarterPackPlugin`. Bytecode major version of the first `.class` entry is **69 (Java 25)**, matching the ecosystem standard.

      **Exclusion is at the CI release-asset step, not at build time.** `target/` contains both
      `starter-pack-1.1.2.jar` and `original-starter-pack-1.1.2.jar` — the `original-*` JAR *is*
      still produced locally. It is excluded from released assets by `.github/workflows/build.yml`,
      which filters `! -name 'original-*'` on both the SHA256SUMS step and the `gh release upload`
      step (and excludes `!target/original-*.jar` from the uploaded build artifact). So no
      `original-*` JAR can reach a release, but one does exist on disk after a local build.

      `maven-shade-plugin` is a **no-op** here: every dependency is `provided`/`test` scope, so it
      shades nothing and exists only to rename the untouched jar, which is what creates the
      `original-*` file. `agua-de-florida` resolved this by removing shading entirely; doing the
      same here is out of scope for this change.

No test dependency change was needed: `junit-jupiter-api` reaches the test classpath transitively
through the declared `junit-jupiter-engine` 5.10.1, and `@Test`, `@Nested`, `@DisplayName`, and the
`Assertions` statics all resolved. Surefire's `<argLine>` Mockito javaagent path was left untouched.

## 7. Matrix

### 7a — single-plugin runtime verification (`1.1.2`) — PARTIAL

Evidence below comes from a **single disposable Legendary stack run on 2026-07-20**
(image `05jchambers/legendary-minecraft-geyser-floodgate:latest`) with **all six fixed plugin
JARs mounted together**. The same run backs the gate 7a note in all six repositories.

- [x] Paper, Geyser, Floodgate, and ViaVersion start successfully together. **Verified.** Paper
      reached `Done (18.178s)! For help, type "help"`. The Java port answered a real Minecraft
      protocol handshake — not merely a TCP connect — reporting `Paper 26.1.2 | protocol 775` and
      `PLAYERS: 0 / 20`. `/plugins` reported 9 plugins, all green/enabled: AguaDeFlorida, floodgate,
      Geyser-Spigot, GlutenFreeBread, StarterPack, TheCurse, ViaVersion, WildWeatherUpdate,
      WorldCRUD. Companion versions observed: floodgate v2.2.5-SNAPSHOT (b138-fc99cfc),
      Geyser-Spigot v2.11.0-SNAPSHOT (Geyser 2.11.0-b1200), ViaVersion present; Geyser started on
      UDP port 19200. Each plugin enabled at its new version with **zero exceptions, errors, or
      SEVERE lines attributable to any of the six** — including `Enabling StarterPack v1.1.2`.
- [ ] Java and Bedrock smoke tests cover joins plus affected commands, events, permissions,
      persistence, and reloads. **PARTIAL — the Java side was exercised, the Bedrock side was not.
      Left unchecked deliberately.**

      *What was exercised.* The **Floodgate prefix assumption was confirmed empirically, not merely
      from documentation**: reading `/minecraft/plugins/floodgate/config.yml` inside the running
      container on the Floodgate 2.2.5 build showed `username-prefix: "."` and
      `replace-spaces: true`, alongside the shipped comment "Floodgate prepends a prefix to bedrock
      usernames to avoid conflicts". The `.` prefix this fix depends on is now **observed on the
      actual runtime, not assumed** — the single most important upgrade to the evidence.

      The **new failure path was then exercised end-to-end over RCON on the live server** for every
      fixed command across all six plugins — `/aguadeflorida give carm`, `/curse start carm`,
      `/curse book carm`, `/worldcrud listpermissions carm`, `/starterpack give carm`,
      `/gfbread clear carm`, and `/weather trigger rain carm` — and each returned the new
      message with no exception: exactly `No player matches 'carm'; no players are online.` This proves that
      `PlayerLookup.resolve` / `resolveAllowingPartial` / `onlineNames` / `noSuchPlayerMessage`
      actually execute correctly against real Bukkit APIs, that command dispatch reaches them, and
      that the message renders — none of which the unit tests could show.

      *What remains unverified.* **The positive match is still unproven.** No real Bedrock client
      was available, so no player with a `.`-prefixed Java-side username ever joined. What is
      verified is that the resolution path runs without error and that the not-found branch is
      correct; that `/starterpack give carm` actually **finds** a Bedrock player named `.acarm` has
      **not** been observed. Only the empty-online-list branch of `noSuchPlayerMessage` was
      exercised; the branch that lists online player names was not. The operator will verify live on
      the dev server with helpers. `resolve` / `resolveAllowingPartial` still have **no unit-test
      coverage** (Bukkit statics, no MockBukkit).
- [ ] Public deployment smoke tests verify `play.xpfarm.org` reaches the intended Java and Bedrock entry points. Belongs to gate 11, not this gate.
- [x] Ollama and Umami unavailable-endpoint tests keep the server and plugins available when applicable. Not applicable — no external integrations.

### 7b — ten-plugin ecosystem matrix — NOT RUN

- [ ] Fresh-volume Legendary stack test covers all ten updater-managed plugins.
- [ ] Each updater-managed plugin's manifest `enabled` value, default state, and expected fresh-volume behavior are recorded separately.

Out-of-band and not a prerequisite for this release. `1.1.2` changes no updater manifest entry and
adds no dependency.

### 7b — ecosystem matrix (12 plugins) — PASSED 2026-07-21

Trigger: the updater manifest changed — Timber Blast `v1.0.0` was enrolled
(`carmelosantana/minecraft-plugin-updater` commit `6065b03`), taking the roster from 11 to 12.

- [x] Fresh-volume Legendary stack test covers all updater-managed plugins. **12/12 PRESENT.**
      Run via the shared rig (`xpfarm-test-stack matrix up --from-releases`) on a fresh volume,
      roster read from the live `plugins.json` rather than a hardcoded list. The rig cross-checks
      the plugin count the server announces against what it parsed, and asserts each plugin is
      **enabled**, not merely listed.
- [x] Each updater-managed plugin's manifest `enabled` value, default state, and expected
      fresh-volume behavior are recorded separately. All 12 entries have `enabled` absent
      (equivalent to `true`) and no `pin`; every one was therefore expected to install and enable,
      and every one did. No entry was disabled, so there is no intentional-absence row this run.
- [x] Paper, Geyser, Floodgate, and ViaVersion start successfully together.
      Paper reached `Done (15.543s)! For help, type "help"`; the Java port answered a real
      protocol handshake reporting `Paper 26.1.2 | protocol 775`, `PLAYERS: 0 / 20`. Companions:
      Geyser-Spigot 2.11.0-SNAPSHOT, floodgate 2.2.5-SNAPSHOT, ViaVersion 5.11.0.
- [ ] Java and Bedrock smoke tests cover joins. **Not performed — no client attaches to this
      stack by design.** Per `PLUGIN_LIFECYCLE.md` §7 this is not a blocker; client behavior is a
      tracked gate-12 play-test obligation, not a matrix result.
- [x] `play.xpfarm.org` reaches the intended Java and Bedrock entry points.
      Read-only production check, separate from the disposable stack: DNS `168.231.74.113`;
      Java `25565` answered a real handshake (`Paper 26.1.2 | protocol 775`, 1 player online);
      Bedrock UDP `19132` reachable.
- [x] Ollama and Umami unavailable-endpoint tests keep the server and plugins available.
      Neither service exists in this stack, so this is the negative path by construction. Both
      self-disabled cleanly: `Ollama integration is disabled; no API client or listeners were
      started.` and `Umami analytics is disabled; no tracking listeners or network clients were
      started.` Server stayed healthy (`list` responded) with all 12 enabled.

This plugin's row: the updater reported `StarterPack: installed v1.1.2` from the published release
asset and Paper enabled it alongside the other 11. `--from-releases` was used deliberately — it
installs the real published assets through the real updater, so this is what production installs.

Co-resident: AguaDeFlorida 2.0.0, CopperKingdom 0.2.1, TheCurse 0.2.2, DeathDepot 1.1.1, ElectricFurnace 0.2.1, GlutenFreeBread 1.1.3, Ollama 0.2.1, TimberBlast 1.0.0, Umami 1.1.1, WildWeatherUpdate 1.0.2, WorldCRUD 1.1.2.

Zero exceptions, SEVERE lines, or enable failures attributable to any plugin. No secrets in any
log line. Stack torn down with `matrix down`; lease released, no orphaned containers.

## 8. CI/CD

- [x] Identical standard plugin Actions workflow is installed with the required triggers, Temurin 25 build, artifact, checksum, and release behavior. `.github/workflows/build.yml`, present since `1.1.0`.
- [x] Successful main Actions run is recorded before tagging. `fix/floodgate-name-resolution` was merged fast-forward to `main` and pushed on 2026-07-20. The `main`-branch Actions run for commit `a10382a` completed with conclusion `success` **before** tag `v1.1.2` was created. No tag was pushed against a red or in-flight run.
- [ ] Workflow permissions contain no broader access than the documented contract. Not re-checked during this change.

## 9. Release — `v1.1.2` COMPLETE

- [x] Semantic version matches the POM, plugin metadata, and `v<version>` tag. Verified: `pom.xml` `<version>` `1.1.2` equals tag `v1.1.2` equals the `plugin.yml` version read out of the built JAR.
- [x] Successful tag Actions run and GitHub release are recorded. Annotated tag `v1.1.2` created on verified commit `a10382a` and pushed; the tag Actions run completed with conclusion `success`. GitHub release published 2026-07-20 14:47:59 UTC with `draft=false`, `prerelease=false`, and it is now the repository's Latest release.
- [x] Release contains exactly one updater-matching JAR plus `SHA256SUMS.txt` and no `original-*` JAR. Verified by downloading the published release assets: exactly one JAR matching the updater asset pattern, plus `SHA256SUMS.txt`, and no `original-*` JAR.
- [x] Downloaded release assets pass `sha256sum --check SHA256SUMS.txt`. Reported `OK` for the JAR.

## 10. Updater

- [ ] Updater manifest/tests cover repository, destination, anchored asset regex, legacy globs, enabled state, and optional pin. Not touched — `1.1.2` requires no manifest change, but this was not re-verified against the manifest.
- [ ] Fresh install, upgrade, no-op, legacy archival, endpoint failure, and checksum failure behaviors pass.
- [ ] Updater dry-run uses a disposable directory and never a production plugin directory.
- [ ] Failure retains the installed JAR and default fail-open behavior permits Minecraft startup.

Updater enrollment work was **not performed in this pass** (`v1.1.2` release only).

## 11. Deployment

- [ ] Dokploy redeployment notes identify the full recreation used to rerun the one-shot updater.
- [ ] Updater completion, Minecraft startup, destination JAR, and stack/plugin logs were inspected.
- [ ] No production plugin hot reload was used.

**Not performed.** The operator will deploy and verify live on `play.xpfarm.org` via the dev server with helpers.

**Rollback:** `1.1.2` is additive and behaviour-preserving for Java usernames; rolling back to
`v1.1.1` restores the previous lookup, which simply cannot find Bedrock players.

## 12. Handoff

- [ ] Current-state documentation refreshed with release, CI, updater, deployment, and local pending state. Pending — the branch is unmerged.
- [ ] Known limitations, skipped checks, configuration or migration notes, rollback guidance, and follow-up owner are recorded. Recorded in this file; not yet propagated to `CURRENT_STATE.md`.
- [ ] Evidence distinguishes source commit, published tag/release, updater state, and deployed state without exposing secrets.

**Follow-up owner:** the next session to pick up `fix/floodgate-name-resolution`. The single most
valuable outstanding action is gate 7a with a real Bedrock client — every behavioural acceptance
check for this fix is currently unverified.
