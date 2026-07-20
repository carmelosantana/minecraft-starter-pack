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
- [ ] The releasable JAR and embedded `plugin.yml` were inspected; `original-*` JARs are excluded. Not inspected during this change. Note `maven-shade-plugin` is still configured despite every dependency being `provided`/`test` scope, so `original-*` JARs remain possible; not addressed in this patch.

No test dependency change was needed: `junit-jupiter-api` reaches the test classpath transitively
through the declared `junit-jupiter-engine` 5.10.1, and `@Test`, `@Nested`, `@DisplayName`, and the
`Assertions` statics all resolved. Surefire's `<argLine>` Mockito javaagent path was left untouched.

## 7. Matrix

### 7a — single-plugin runtime verification — NOT RUN

- [ ] Paper, Geyser, Floodgate, and ViaVersion start successfully together. No stack was booted for this change.
- [ ] Java and Bedrock smoke tests cover joins plus affected commands, events, permissions, persistence, and reloads. **This is the gate that would actually prove the fix**, and it was not run — no Bedrock client is available in this environment.
- [ ] Public deployment smoke tests verify `play.xpfarm.org` reaches the intended Java and Bedrock entry points. Belongs to gate 11.
- [x] Ollama and Umami unavailable-endpoint tests keep the server and plugins available when applicable. Not applicable — no external integrations.

### 7b — ten-plugin ecosystem matrix — NOT RUN

- [ ] Fresh-volume Legendary stack test covers all ten updater-managed plugins.
- [ ] Each updater-managed plugin's manifest `enabled` value, default state, and expected fresh-volume behavior are recorded separately.

Out-of-band and not a prerequisite for this release. `1.1.2` changes no updater manifest entry and
adds no dependency.

## 8. CI/CD

- [x] Identical standard plugin Actions workflow is installed with the required triggers, Temurin 25 build, artifact, checksum, and release behavior. `.github/workflows/build.yml`, present since `1.1.0`.
- [ ] Successful main Actions run is recorded before tagging. Not applicable yet — this work sits on `fix/floodgate-name-resolution`, unpushed and unmerged.
- [ ] Workflow permissions contain no broader access than the documented contract. Not re-checked during this change.

## 9. Release — `v1.1.2` NOT RELEASED

- [ ] Semantic version matches the POM, plugin metadata, and `v<version>` tag. `pom.xml` is `1.1.2` and `plugin.yml` uses `version: '${project.version}'`, but no tag exists.
- [ ] Successful tag Actions run and GitHub release are recorded. Not tagged, pushed, or merged.
- [ ] Release contains exactly one updater-matching JAR plus `SHA256SUMS.txt` and no `original-*` JAR.
- [ ] Downloaded release assets pass `sha256sum --check SHA256SUMS.txt`.

## 10. Updater

- [ ] Updater manifest/tests cover repository, destination, anchored asset regex, legacy globs, enabled state, and optional pin. Not touched — `1.1.2` requires no manifest change, but this was not re-verified against the manifest.
- [ ] Fresh install, upgrade, no-op, legacy archival, endpoint failure, and checksum failure behaviors pass.
- [ ] Updater dry-run uses a disposable directory and never a production plugin directory.
- [ ] Failure retains the installed JAR and default fail-open behavior permits Minecraft startup.

## 11. Deployment

- [ ] Dokploy redeployment notes identify the full recreation used to rerun the one-shot updater.
- [ ] Updater completion, Minecraft startup, destination JAR, and stack/plugin logs were inspected.
- [ ] No production plugin hot reload was used.

**Rollback:** `1.1.2` is additive and behaviour-preserving for Java usernames; rolling back to
`v1.1.1` restores the previous lookup, which simply cannot find Bedrock players.

## 12. Handoff

- [ ] Current-state documentation refreshed with release, CI, updater, deployment, and local pending state. Pending — the branch is unmerged.
- [ ] Known limitations, skipped checks, configuration or migration notes, rollback guidance, and follow-up owner are recorded. Recorded in this file; not yet propagated to `CURRENT_STATE.md`.
- [ ] Evidence distinguishes source commit, published tag/release, updater state, and deployed state without exposing secrets.

**Follow-up owner:** the next session to pick up `fix/floodgate-name-resolution`. The single most
valuable outstanding action is gate 7a with a real Bedrock client — every behavioural acceptance
check for this fix is currently unverified.
