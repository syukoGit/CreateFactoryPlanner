# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Create Factory Planner is a **NeoForge mod** that adds an in-game production planner for **Create**: pick an output and
a target throughput, get back the full production chain — machine counts, raw inputs, Stress Unit cost and transport
bottlenecks. It is the Factorio Factory Planner / Helmod idea adapted to Create's rotation, SU and belt-throughput
model.

**[create-factory-planner-roadmap.md](create-factory-planner-roadmap.md) is the source of truth** for scope, phasing,
validation criteria and the numbered decisions (D-001 … D-008) referenced throughout this file. Read it before planning
any feature; update it when a decision changes.

**The repository is at the very start of phase 0.** The only Java file is the `@Mod` entrypoint — no `core/`, no solver,
no UI exists yet. Everything under Architecture below describes the *target* structure, not what is on disk. Do not
assume a class exists because it is named here.

The differentiating argument is that the mod reads recipes and stress values **from the game as installed**, so it stays
correct on any modpack, addon or datapack — which no web calculator can guarantee. Never trade that away for hardcoded
data.

The display name is **Create Factory Planner** (`mod_name`), but the identifier stays `createfactoryplanner` everywhere
it is technically load-bearing: `mod_id`, the `fr.syuko.createfactoryplanner` package, the
`assets/createfactoryplanner/`
resources and the lang keys. The roadmap uses `cfp` as the short form for user-facing paths (`/cfp` command,
`config/cfp/`). A mod id cannot contain a hyphen (`[a-z][a-z0-9_]{1,63}`), which is why the long form is written solid.

**v1 targets the client** (D-002 rationale): on 1.21.1 the full `RecipeManager` is synced to the client. Since 1.21.2
Mojang only syncs `RecipeDisplay`, so the day Create ports to 26.1 the mod becomes server-required, via
`OnDatapackSyncEvent#sendRecipes` + `RecipesReceivedEvent#getRecipeMap`. That port is the reason recipe harvesting must
sit behind the `RecipeSource` interface from day one.

## Environment

- **Minecraft** 1.21.1, **NeoForge** 21.1.248, **Java 21** (toolchain enforced in `build.gradle`).
- **Create** 6.0.11-295 in `compileOnly`. Earlier 6.0.x versions publish only a `-slim` classifier; 6.0.11-295 is the
  first with Gradle module metadata and a real mod jar, so it is also the first that resolves its own transitives.
- **EMI** 1.1.24+1.21.1 and **JEI** 19.44.0.401, runtime only, for cross-checking recipe dumps in game. The mod never
  compiles against either.
- **Create addons**, runtime only, in the `clientWithAddons` profile: Create: Connected 1.3.2-mc1.21.1 and Create:
  Ultimate Factory 2.2.4, both from the Modrinth Maven. They exist to prove D-009 — their recipes must appear in the
  dump with no code change.
- Mappings: Parchment (`parchment_minecraft_version` / `parchment_mappings_version`).
- All version numbers live in `gradle.properties` — change them there, not in `build.gradle`.

**Dev runs load third-party mods from `run/mods/`, never from the classpath.** NeoForge 21.1 registers four
`IModFileCandidateLocator`s — `NeoForgeDevProvider`, `ModsFolderLocator`, `MavenDirectoryLocator`, `UserdevLocator` —
and not one of them scans the classpath. A jar added via `additionalRuntimeClasspath` is therefore linkable but
invisible to mod loading, which surfaces as `Currently, create is not installed` even though the jar is right there in
`build/moddev/clientLegacyClasspath.txt`. The `devMods` configuration is synced into `run/mods` by the `installDevMods`
task, hooked to every run through `taskBefore`. It is deliberately `transitive = false`: the Create jar already bundles
Registrate, Ponder and Flywheel under `META-INF/jarjar/`, and copying them alongside it would load them twice.
`installDevMods` is a `Sync` task, so it deletes anything else in `run/mods` — drop manual test jars elsewhere.

**Two client profiles share one game directory.** `runClient` syncs `devMods` only; `runClientWithAddons` syncs
`devMods` plus `devModsAddons`. Both runs pin `gameDirectory` to `run/`, so worlds, configs and dumps are the same on
either side and a dump can be compared with and without addons on the same save. The cost of that choice: the two
`Sync` tasks write the same `run/mods`, so **never chain both runs in one Gradle invocation** — `./gradlew runClient
runClientWithAddons` has them fighting over the directory. Run one at a time; each sync rewrites the folder on the way
in.

Four repositories are declared. `maven.createmod.net` serves Create, Ponder and Flywheel. `mvn.devos.one/snapshots`
serves **Registrate**, pulled in transitively by Create: the usual `maven.tterrag.com` stopped publishing in 2023 and
has nothing past MC 1.20, so `MC1.21-1.3.0+67` exists only on that mirror. If dependency resolution breaks with
`Could not find com.tterrag.registrate:Registrate`, that mirror is the cause, not the Create coordinate.
`maven.terraformersmc.com/releases` serves EMI, `maven.blamejared.com` serves JEI, and `api.modrinth.com/maven` serves
the Create addons — the last one is restricted to the `maven.modrinth` group by a `content` filter so it is not probed
for every other dependency. Modrinth coordinates are `maven.modrinth:<slug>:<version-number-or-id>`; the slug and the
exact version come from `api.modrinth.com/v2/project/<slug>/version`, never from guesswork.

**A Modrinth version *number* is not unique, and resolving by number can silently hand back a Forge jar.**
Create: Ultimate Factory publishes `2.2.4` twice — Forge/1.20.1 and NeoForge/1.21.1 — so
`maven.modrinth:create-ultimate-factory:2.2.4` fetched the Forge file (234456 bytes, only `META-INF/mods.toml`).
NeoForge then skipped it with `Skipping jar. File … is for Minecraft Forge or an older version of NeoForge`, a WARN in
the middle of `debug.log` that fails nothing — the client boots happily with the addon simply absent. That is why the
coordinate is pinned to the opaque **version id** `AEMRNsNS` (235239 bytes, ships both `mods.toml` and
`neoforge.mods.toml`). Create: Connected keeps a readable version because its `1.3.2-mc1.21.1` already carries the
Minecraft version and cannot collide. After changing any addon coordinate, check that the jar in `run/mods` contains
`META-INF/neoforge.mods.toml`, and confirm the mod id appears in `Found valid mod file` in `run/logs/debug.log` — an
absent addon is invisible otherwise.

## Commands

Use the Gradle wrapper (`./gradlew` on bash, `gradlew.bat` on cmd). Build config caching and daemon are enabled.

```bash
./gradlew compileJava        # fast compile check
./gradlew build              # full build + jar into build/libs
./gradlew test               # JUnit 5 suite (core/ only, no Minecraft)
./gradlew runClient            # client with the mod, Create, EMI and JEI
./gradlew runClientWithAddons  # same game directory, plus the Create addons
./gradlew runServer          # dedicated server
./gradlew runGameTestServer  # run all registered gametests, then exit
```

Single test class or method:

```bash
./gradlew test --tests "fr.syuko.createfactoryplanner.core.solver.SolverTest"
./gradlew test --tests "*SolverTest.byproductReducesDedicatedProduction"
```

There are no tests yet — JUnit 5 is wired and `test` reports `NO-SOURCE`. Gametests are enabled
(`neoforge.enabledGameTestNamespaces=createfactoryplanner`) but none are registered.

`runGameTestServer` **crashes when no gametests exist**; that is the run config's documented behaviour, not a
regression.

## Architecture

The code is organized by **integration boundary**, not by technical layer: a pure core reasoned about in isolation, with
thin adapters around each third-party API. Target structure (roadmap §3), base package
`fr.syuko.createfactoryplanner`:

- **`core/`** — `model/` (`ItemKey`, `RecipeNode`, `MachineInstance`, `KineticNetwork`, `ProductionPlan`), `graph/`
  (graph construction, cycle detection), `solver/` (throughput resolution, material balance).
- **`data/`** — `MachineProfile`, `TransportProfile`, JSON loading and user override merging.
- **`integration/recipes/`** — the `RecipeSource` interface and its client implementation (`level.getRecipeManager()`);
  the future server implementation lands here too.
- **`integration/create/`** — the Create adapter: `AllRecipeTypes` → DTO, `BlockStressValues`.
- **`ui/`** — the layout mini-lib and the planner screens.
- **`resources/data/`** — `machine_profiles.json`, `transport_profiles.json`.

**Golden rule: `core/` must never import `net.minecraft.*`.** That is what makes the solver testable without launching
the game, and it is the difference between a project that can evolve and one that gets abandoned. Enforce the boundaries
by grep — all three must return nothing:

```bash
grep -rl "net\.minecraft" src/main/java/fr/syuko/createfactoryplanner/core/
grep -rl "com\.simibubi" src/main/java/fr/syuko/createfactoryplanner/core/ src/main/java/fr/syuko/createfactoryplanner/ui/
grep -rl "com\.simibubi" --include="*.java" src/main/java/fr/syuko/createfactoryplanner/ | grep -v "/integration/create/"
```

### Decisions that constrain the code

These are settled (roadmap §2). Do not relitigate them in an implementation task:

- **D-002** — recipe harvesting sits behind `RecipeSource`. Nothing outside `integration/recipes/` touches a
  `RecipeManager`.
- **D-003** — machine speed values are data (`machine_profiles.json`), never constants in Java, with a user override
  file merged on top.
- **D-004** — **SU values are read at runtime via `BlockStressValues`, never hardcoded.** Hardcoding them breaks every
  pack that overrides stress by config or datapack, which is exactly the promise the mod is sold on.
- **D-005** — RPM is fixed per network (default 128, configurable). This is what keeps the material balance **linear**
  and the v1 solver trivial. A feature that makes RPM a free variable is a v3 item and needs a MIP solver.
- **D-006** — the kinetic-network object is modelled from v1 even though the UI exposes a single network. Retrofitting
  it later would be expensive.
- **D-007** — the UI is a **hierarchical table**, not a node graph. Nodal drag & drop in a Minecraft GUI is its own
  project.

### Throughput model

For each node:

```
effective = min(machine_throughput(type, rpm), input_transport, output_transport)
```

Machine throughput comes from the interpolated JSON curve. Transport throughput comes from `transport_profiles.json`
and, for belts, **depends on the RPM of the same kinetic network** — that coupling is what web calculators do not model,
and it is the reason the mod exists. A diagnostic must name *which* element is the bottleneck, not merely report a
reduced number.

### Create facts the model must respect

- Default cap of 256 RPM; a component that tries to exceed it detaches.
- Each machine has a minimum RPM (30 for the mixer, for instance).
- SU impact = base impact × |RPM|.
- An overstressed network stops **entirely** — there is no gradual degradation.

Useful Create classes: `AllRecipeTypes`, `ProcessingRecipe` (ingredients, `ProcessingOutput` with chance, fluids,
duration), `BlockStressValues`, `MillstoneBlockEntity`, `MechanicalPressBlockEntity`, `BasinOperatingBlockEntity`,
`CrushingWheelControllerBlockEntity`, `DeployerBlockEntity`, `AllConfigs.server().logistics` (funnel cooldowns).

### Data trust

**The Create wiki is community-maintained and documents `ops/s = f(RPM)` badly.** Phase 2 gates the whole speed table
behind nine manual in-game measurements (millstone, press, mixer × 3 RPM values) with a < 10 % tolerance. Until that
gate passes, treat any speed figure — including one produced from model knowledge — as unverified. Never populate
`machine_profiles.json` from memory and present it as measured. The headline risk in the roadmap is the mod lying to the
player; a wrong number costs more than a missing one.

Mod metadata is templated in
[`src/main/templates/META-INF/neoforge.mods.toml`](src/main/templates/META-INF/neoforge.mods.toml), expanded by the
`generateModMetadata` task from `gradle.properties`. Edit the template, never the copy under `build/`. Adding a
`${placeholder}` there requires adding the matching entry to `replaceProperties` in `build.gradle` — a missing property
fails the build.

## Conventions

- **No comments — the code must be self-descriptive.** Do not add Javadoc, descriptive, explanatory, example, or warning
  comments in any file (`.java`, `.gradle`, `.properties`, `.yaml`, …). Make intent clear through names and structure
  instead; if a comment feels necessary, extract a well-named method or constant. (`.comment(...)` calls building a
  `ModConfigSpec` are not code comments — they generate the user-facing config file and must stay. License headers in
  generated wrapper scripts also stay.)
- Keep new user-facing strings in
  [`en_us.json`](src/main/resources/assets/createfactoryplanner/lang/en_us.json) and reference them via
  `Component.translatable(...)`. FR and EN are both shipped (roadmap phase 6).
- Prefer Create's **public APIs**. There is no mixin config in this repo; if UI injection ever requires one, it has to
  be created along with its `[[mixins]]` entry in the metadata template.
- The bulk of the tests belong in `core/` as plain JUnit, because they run in seconds. Coverage target > 80 % on
  `core/`, no figure elsewhere. The five reference solver cases (trivial, chain, byproduct, cycle, probabilistic) are
  spelled out in roadmap §3 and must be verifiable by hand.

## Git

- **Never run `git commit` without an explicit go for that specific commit.** Editing files, staging them and proposing
  a message is fine; creating the commit is not. An instruction to "do the changes for commit N" is **not** permission
  to commit them, and permission for one commit never carries over to the next.
- The same rule covers every history rewrite (`reset`, `commit --amend`, `rebase`, `cherry-pick`) and `git push`.
- Commit messages are a **single Conventional Commits line** — no body, no `Co-Authored-By` trailer.
- The repository has **no commits yet**: everything is staged on `master` awaiting an initial commit.