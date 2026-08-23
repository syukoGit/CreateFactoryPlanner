# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Create Factory Planner is a **NeoForge mod** that adds an in-game production planner for **Create**. The v1 is a
**full-screen nodal editor**: the player lays down machines one at a time on a canvas and watches supply, demand and
actual flow propagate through the graph. It is *not* a solver — the mod does the arithmetic, the speed data and the flow
consistency; every structural decision stays with the player.

Two documents are the source of truth. Read both before planning any feature; amend the spec first when a product
decision changes, then the plan, then the code.

- **[create-factory-planner-spec-fonctionnelle-v0.5.3.md](plans/create-factory-planner-spec-fonctionnelle-v0.5.3.md)** —
  functional spec. Features `F-xx`, interfaces `I-xx`, calculation rules `R-xx`, settled decisions `D-xxx`, open
  questions `Q-xx`. **Functional only, no implementation.**
- **[create-factory-planner-plan-implementation.md](plans/create-factory-planner-plan-implementation.md)** —
  implementation plan v1.2. Milestones `M1`–`M7`, phases 0–8, gates `G1`–`G5`, numbered tasks `T-xxx`. **No product
  decision is taken there** — if implementation reveals a gap, fix the spec first.

Both documents are written in French; the code, the identifiers and `en_us` are English (D-020).

**The repository is at the very start of phase 0.** The two Gradle modules exist, but `planner-core` is an empty
skeleton and the only Java file is the `@Mod` entrypoint under `neoforge/` — no engine, no UI. Everything under
Architecture below describes the *target* structure, not what is on disk. Do not assume a class exists because it is
named here.

The differentiating argument is that the mod reads recipes and stress values **from the game as installed**, so it stays
correct on any modpack, addon or datapack — which no web calculator can guarantee. Never trade that away for hardcoded
data (P1).

The display name is **Create Factory Planner** (`mod_name`), but the identifier stays `createfactoryplanner` everywhere
it is technically load-bearing: `mod_id`, the `fr.syuko.createfactoryplanner` package, the
`assets/createfactoryplanner/` resources and the lang keys. Plans and overrides live in `config/createfactoryplanner/`.
A mod id cannot contain a hyphen (`[a-z][a-z0-9_]{1,63}`), which is why the long form is written solid. The
implementation plan writes the base package as `fr.<vous>.factoryplanner` — that is a placeholder; the real base package
is `fr.syuko.createfactoryplanner` in both modules.

**v1 targets the client** (D-002 rationale): on 1.21.1 the full `RecipeManager` is synced to the client. Since 1.21.2
Mojang only syncs `RecipeDisplay`, so the day Create ports to 26.1 the mod becomes server-required, via
`OnDatapackSyncEvent#sendRecipes` + `RecipesReceivedEvent#getRecipeMap`. That port is the reason recipe harvesting must
sit behind the `RecipeSource` interface from day one. The mod is 100 % client-side and **adds no block, item or
behaviour to the game** (D-030); a keybind is the only entry point.

## Environment

- **Minecraft** 1.21.1, **NeoForge** 21.1.248, **Java 21** (toolchain enforced in `build.gradle`).
- **Create** 6.0.11-295 in `compileOnly`. Earlier 6.0.x versions publish only a `-slim` classifier; 6.0.11-295 is the
  first with Gradle module metadata and a real mod jar, so it is also the first that resolves its own transitives.
- **EMI** 1.1.24+1.21.1 and **JEI** 19.44.0.401, runtime only, for cross-checking recipe dumps in game. The mod never
  compiles against either; both integrations are optional at runtime (T-253).
- **Create addons**, runtime only, in the `clientWithAddons` profile: Create: Connected 1.3.2-mc1.21.1 and Create:
  Ultimate Factory 2.2.4, both from the Modrinth Maven. They exist to prove F-04 — their recipes must appear in the dump
  with no code change.
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
`Sync` tasks write the same `run/mods`, so **never chain both runs in one Gradle invocation** —
`./gradlew :neoforge:runClient :neoforge:runClientWithAddons` has them fighting over the directory. Run one at a time;
each sync rewrites the folder on the way in.

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
./gradlew compileJava             # fast compile check, both modules
./gradlew build                   # full build + mod jar into neoforge/build/libs
./gradlew :planner-core:test      # JUnit 5 suite, no Minecraft, runs in seconds
./gradlew :neoforge:runClient            # client with the mod, Create, EMI and JEI
./gradlew :neoforge:runClientWithAddons  # same game directory, plus the Create addons
./gradlew :neoforge:runServer            # dedicated server
./gradlew :neoforge:runGameTestServer    # run all registered gametests, then exit
```

Single test class or method:

```bash
./gradlew :planner-core:test --tests "fr.syuko.createfactoryplanner.core.engine.AllocatorTest"
```

```bash
./gradlew :planner-core:test --tests "*AllocatorTest.maxMinFairSplit"
```

The build is **two modules** (`T-400` done): `:planner-core` is plain Java, `:neoforge` is the mod and embeds the core's
classes into its jar — no `jarJar`, the core is not a mod. Both modules are declared to the loader through
`neoForge.mods`, so a core class missing from that block fails **in game only**. The run tasks live on `:neoforge`;
`gameDirectory` and `run/mods` still point at the repository-root `run/`. There are no tests yet — JUnit 5 is wired and
`test` reports `NO-SOURCE`. Gametests are enabled (`neoforge.enabledGameTestNamespaces=createfactoryplanner`) but none
are registered.

`runGameTestServer` **crashes when no gametests exist**; that is the run config's documented behaviour, not a
regression.

## Architecture

Two Gradle modules, and inside the mod module a strict one-way layering (plan §2).

```
:planner-core   pure Java, ZERO Minecraft import — model, flow engine, diagnostics, mutations
:neoforge       gui/  →  app/  →  data/  →  :planner-core
```

**Arrows never point back up.** `planner-core` does not know that `data` exists: it receives already-normalized DTOs and
returns computation results. It manipulates opaque typed identifiers (`ResourceId`, `MachineId`, `RecipeId`);
translating them into game objects is entirely the mod module's job.

`planner-core` (base package `fr.syuko.createfactoryplanner.core`):

- **`math/`** — `Rate` (exact rationals per tick, R-01), `RateUnit`.
- **`model/`** — `Plan`, `Target`, `node/` (`Node` sealed over `ResourceNode`/`RecipeNode`/`RoutingNode`, `Port`,
  `ResourceRole`), `link/`.
- **`recipe/`** — `RecipeDto`, `IngredientDto`, `OutputDto` (guaranteed part + expectation), `CatalystDto`.
- **`machine/`** — `MachineProfile`, `ThroughputModel`, `MachineSettings`, `ParamDescriptor`, `impl/`.
- **`engine/`** — `FlowSolver`, `Flow` (supply/demand/actual), `Allocator`, `CycleResolver`, `NodeState`.
- **`diagnostics/`** — `Diagnostic`, `DiagnosticLevel`, `DiagnosticCode`, `DiagnosticCollector`.
- **`mutation/`** — `PlanMutation` (sealed, undoable commands incl. `Composite`), `InvariantChecker`.

`:neoforge` (base package `fr.syuko.createfactoryplanner`):

- **`data/recipe/`** — `RecipeSource`, `ClientRecipeSource`, `RecipeHarvester`, `RecipeNormalizer`, `CatalystDetector`.
- **`data/machine/`** — `MachineRegistry`, `MachineBootstrap`, `StressReader` (`BlockStressValues`).
- **`data/constants/`** — `ConstantStore`, `ConstantSource`, `OverrideLoader` (hot reload, R-68).
- **`data/persistence/`**, **`data/share/`**, **`data/coverage/`** — `PlanCodec`/`PlanRepository`/`PlanMigrator`,
  `ShareCodec`/`StringTable`, `CoverageReport`.
- **`app/`** — `PlannerSession` (current plan + undo/redo + result), `ResourceCatalog`, `AutoLayout`.
- **`gui/`** — `screen/`, `canvas/` (`Camera`, renderers, `HitTester`, `InteractionState`), `panel/`, `widget/`.
- **`integration/`** — `EmiIntegration`, `JeiIntegration`, both optional.

**Golden rule: `planner-core` must never import `net.minecraft.*` or `net.neoforged.*`.** That is what makes the engine
testable without launching the game. The module split makes it structural — `planner-core` declares no Minecraft
dependency, so such an import cannot compile — and `checkForbiddenImports` (`T-401`) enforces the rest:

```bash
./gradlew check
```

The task is declared once, in the root [build.gradle](build.gradle), and registered on both modules. It scans imports,
never call sites, so a fully-qualified name would slip through — that is a deliberate trade for a check that costs
milliseconds. Two rules, and the second one is the one that gets forgotten:

| Module          | Refused                                                                             |
|-----------------|-------------------------------------------------------------------------------------|
| `:planner-core` | `net.minecraft`, `net.neoforged`, `com.simibubi`                                    |
| `:neoforge`     | `com.simibubi` anywhere outside `src/main/java/fr/syuko/createfactoryplanner/data/` |

Adding a package to a rule means editing `forbiddenPackages` in the root build file — there is no second copy of the
rule in CI to keep in sync.

### The graph model

A plan is a **bipartite directed graph** (D-016). Resource nodes (an item or a fluid) and processing nodes (recipes,
routing) strictly alternate; a link always goes resource → processing or processing → resource.

Three invariants make the plan **isomorphic to the build** (P4) and are enforced structurally, not by convention:

- `R-107` / D-031 — **a resource node has at most one incoming and one outgoing link**. It is a conveyor, not a stock.
  Enforced by `Port`: a port carries at most one link. The GUI hides the `+` button when the port is taken.
- `R-108` — every branching goes through a routing node. There is **no implicit split and no implicit merge**.
- `R-126` / D-040 — **two processing nodes are never linked directly**; a resource node separates them, like a conveyor
  in game.

`R-109` (at most one link per resource on each side of a recipe node) is guaranteed by normalization, not by a check.
`InvariantChecker.canConnect` is called *before* drawing a candidate link so a forbidden connection is never even
offered; `InvariantChecker.check` runs on import (R-63) and in test assertions, never in the render loop.

**A recipe node is one physical machine** (D-021) — no machine-count field, no fractional machines. The single exception
is a line of encased fans, which is one installation parameterized by `N`.

### Flow model

There is **no solver and no auto-sizing in v1** (D-011, D-012, D-022). `FlowSolver` runs two local passes with no
feedback loop:

1. **Demand goes up** — each recipe node declares its input need from its nominal throughput; routing nodes aggregate.
2. **Actual flow comes down** — each node allocates what it really has, a recipe node takes `min(demand, allocation)`
   and produces proportionally: `production = nominal × min_i(actual_i / required_i)` (R-86).

Every link carries three visible values: supply, demand, actual flow. `deficit = demand − actual`,
`surplus = supply − actual` (R-87). Splits use **max-min fair allocation** (R-49, D-024) — the reference case is 9
iron/s split between a branch consuming 3/s and a greedy branch, which must give 3/s and 6/s with no manual setting.
Cycles are resolved by iterating both passes with an iteration cap and an "approximate result" mark (R-24). Recompute is
full and immediate on every edit (R-88), budget **300 nodes in under 50 ms** — the dominant cost is the gcd in
`Rate`, so measure early (`T-060`, gate G2).

Throughput per machine type comes from a `ThroughputModel` implementation:

| Machine                                                  | Nominal throughput                                                       |
|----------------------------------------------------------|--------------------------------------------------------------------------|
| Press, mixer, wheels, millstone, saw, deployer, crafters | `f(RPM)` — `StationThroughput`                                           |
| Encased fan line                                         | `fan_capacity × N`, `fan_capacity = 128` items/min — `FanLineThroughput` |

**Transport is out of scope in v1** (D-010): links have no type, no capacity, no constraint. v1 assumes **optimal
conveyance** (spec §1.5) — a blocking filter is presumed, so belt speed is never a bottleneck. That assumption is false
if the player does not build the filter, so `R-122` requires it to be shown on the node. For a fan line the RPM setting
therefore affects **only the SU** (R-131), never the throughput.

### Create facts the model must respect

- RPM range **1 to 256** (R-41, D-005); a global default of 128 applies, each node may override it. Above 256 components
  detach in game, so the value is refused (R-42).
- Each machine has a minimum RPM. Below it the node is an **error**, not a slowdown (R-43).
- SU impact = base impact × RPM, read at runtime via `BlockStressValues` (D-004), and **purely informative** — no
  overstress error is computed (R-54, D-015). A fan line counts every fan (R-99).
- Catalysts (an ingredient present in and out in the same quantity) are **not resource nodes**: they are an annotation
  on the recipe node plus a priming line in the shopping list (R-124), they never enter the flow passes (R-130), and in
  v1 they are considered eternal (R-125).

Useful Create classes: `AllRecipeTypes`, `ProcessingRecipe` (ingredients, `ProcessingOutput` with chance, fluids,
duration), `BlockStressValues`, `MillstoneBlockEntity`, `MechanicalPressBlockEntity`, `BasinOperatingBlockEntity`,
`CrushingWheelControllerBlockEntity`, `DeployerBlockEntity`, `EncasedFanBlockEntity`.

### Decisions that constrain the code

Settled in spec §10. Do not relitigate them in an implementation task:

- **D-002** — recipe harvesting sits behind `RecipeSource`. Nothing outside `data/recipe/` touches a `RecipeManager`.
- **D-004** — SU values read at runtime, never hardcoded.
- **D-007** — the UI is a **full-screen nodal canvas**. Without a solver, the editor *is* the product.
- **D-013** — scope is **what a Create machine can execute**, vanilla recipes included (smelting and smoking by encased
  fan, shaped/shapeless crafting by mechanical crafters, stripping by mechanical saw). The criterion is the machine, not
  the recipe's origin (R-31). One recipe executable by several machines yields several `RecipeBinding` pairs (R-32).
- **D-018** — no automatic merging of nodes carrying the same item; duplicates are flagged and aggregated in the summary
  only.
- **D-019** — computation in exact rational resources/tick, display in `/s` by default.
- **D-027 / D-028** — **overridable: any scalar that is a parameter of a formula** (`fan_capacity`, a machine's minimum
  RPM, the 256 cap). **Not overridable: the shape of the formulas**, the lookup tables, the structure of the throughput
  models — those are Java (R-65 / R-102). Every constant declares its scope, global or per-machine (R-119).
- **D-030** — no content added to the game; the mod is client-side, opened by a keybind.
- **D-039** — no assisted `×N` duplication and no collapsible clusters in v1. Reopened only by gate G5.

### Data trust

`R-69` — **no in-game measurement campaign is run.** Constants are derived from Create's code wherever it exposes them
(provenance `GAME`), from the Create wiki otherwise (provenance `WIKI`), and the user override file wins over both
(`USER`). Resolution cascade: `USER` > `GAME` > `WIKI` > embedded default.

Numerical correctness is therefore **not guaranteed by measurement**. Three guardrails replace it, and none of them is
optional:

1. **internal consistency** — a formula that reproduces several independent data points exactly is unlikely to be wrong
   (the fan model accounts for all ten rows of the reference table, rounding gaps included);
2. **visibility** — every value displays its provenance, and `WIKI` carries a permanent discreet warning (R-66, R-67);
3. **correction** — the user override is the fix-up mechanism, hot-reloaded (R-65, R-68).

`R-133` — **any constant derivable from Create's code must be derived from it.** A `WIKI` value that becomes derivable
migrates to `GAME`. The coverage report (I-11) lists the remaining `WIKI` constants so the debt stays visible. Never
populate a constants file from model knowledge and present it as anything but `WIKI`. The headline risk is the mod lying
to the player; a wrong number costs more than a missing one (P6).

Mod metadata is templated in
[`neoforge/src/main/templates/META-INF/neoforge.mods.toml`](neoforge/src/main/templates/META-INF/neoforge.mods.toml),
expanded by the
`generateModMetadata` task from `gradle.properties`. Edit the template, never the copy under `build/`. Adding a
`${placeholder}` there requires adding the matching entry to `replaceProperties` in `build.gradle` — a missing property
fails the build.

## Conventions

- **No comments — the code must be self-descriptive.** Do not add Javadoc, descriptive, explanatory, example, or warning
  comments in any file (`.java`, `.gradle`, `.properties`, `.yaml`, …). Make intent clear through names and structure
  instead; if a comment feels necessary, extract a well-named method or constant. (`.comment(...)` calls building a
  `ModConfigSpec` are not code comments — they generate the user-facing config file and must stay. License headers in
  generated wrapper scripts also stay.)
- **No `switch` on a machine type** anywhere in the engine, the UI or the serialization (F-16). A machine type is a
  registry entry; a throughput formula is a class. Adding a machine must cost one registry entry and one data entry.
- Use the glossary in spec §4 for naming: `ResourceNode`, `RecipeNode`, `RoutingNode`, `SourceNode`/`SinkNode` roles,
  `Supply`, `Demand`, `ActualFlow`, `Deficit`, `Surplus`, `Catalyst`, `PrimingAmount`, `NominalRate`, `RecipeBinding`.
  The docs and the UI are French, the code is English — the glossary is authoritative for both.
- Keep new user-facing strings in
  [`en_us.json`](neoforge/src/main/resources/assets/createfactoryplanner/lang/en_us.json) and reference them via
  `Component.translatable(...)`. `en_us` is the reference, `fr_fr` ships with it (D-020, T-255).
- Prefer Create's **public APIs**. There is no mixin config in this repo; if UI injection ever requires one, it has to
  be created along with its `[[mixins]]` entry in the metadata template.
- The bulk of the tests belong in `planner-core` as plain JUnit, because they run in seconds. GameTests are reserved for
  what genuinely needs the game: recipe discovery counts, stress reading, hot reload of overrides.
- **The thirteen reference tests of plan §6.2 are the executable spec** (`T-300`) — max-min allocation 9/s → (3/s, 6/s),
  six-level chain without drift, `1 iron + 25 % iron` → `1.25 iron`, four ingots in four slots → one input, sandpaper as
  catalyst, fan line `128 × N`, convergent and divergent cycles, underfed machine prorated on the min, two links on one
  port refused, processing→processing refused, export/import round-trip, reversibility of N mutations. Write them before
  the feature whenever possible.
- A golden coverage-report file (`T-310`/`T-311`) is the regression net against Create updates: when it fails after a
  version bump, updating it is a deliberate act, not an oversight.

## Git

- **Never run `git commit` without an explicit go for that specific commit.** Editing files, staging them and proposing
  a message is fine; creating the commit is not. An instruction to "do the changes for commit N" is **not** permission
  to commit them, and permission for one commit never carries over to the next.
- The same rule covers every history rewrite (`reset`, `commit --amend`, `rebase`, `cherry-pick`) and `git push`.
- Commit messages are a **single Conventional Commits line** — no body, no `Co-Authored-By` trailer.