# Technical Design: CreateRedux (Create Multiplatform Port)

## 1. Executive Summary & Goals

### 1.1 Objective
Port the industry-standard Minecraft mechanical engineering mod, [Create](https://github.com/Creators-of-Create/Create), to modern Minecraft versions (**1.21.11**, **26.1.x**, **26.2**, and future releases) across both **Fabric** and **NeoForge** mod loaders.

### 1.2 Guiding Architectural Principles
1. **Clean Multiplatform Separation**: Reject the upstream single-loader monolithic structure and avoid brittle legacy compatibility layers (such as Porting-Lib). Instead, adopt the battle-tested **RedFX multi-project architecture** (`:common`, `:fabric`, `:neoforge`).
2. **Merge-Hub Branching Model**: Adopt RedFX's version-agnostic `main` hub and dedicated per-Minecraft version branches (`mc-26.2`, `mc-26.1`, `mc-1.21.11`, etc.). Code flows forward cleanly via `git merge main` without cherry-pick drift.
3. **Targeted Platform Abstraction Layer (PAL)**: Implement a lightweight, zero-overhead abstraction for items, fluids, networking, registrations, and configurations. Directly bridge to native APIs:
   - **Fabric**: Fabric Transfer API (`Storage<ItemVariant>`, `Storage<FluidVariant>`), Fabric Networking, Fabric Rendering API (FRAPI).
   - **NeoForge**: Capabilities (`IItemHandler`, `IFluidHandler`), `PayloadRegistrar`, `DynamicBlockStateModel`.
4. **Modern Java & Mapping Standards**:
   - Leverage **Java 25** for 26.1+ releases (Java 21 for 1.21.11).
   - Use official Mojang mappings (`official`) across both dev and production for 26.x (retaining intermediary remapping strictly where required on legacy 1.21.x branches).
5. **High-Performance Rendering**: First-class integration with **Flywheel 1.0** (multi-loader) with graceful CPU/BER fallbacks.

---

## 2. Structural Analysis: Upstream vs. Porting-Lib vs. RedFX

### 2.1 Upstream Create (Creators-of-Create/Create)
- **Status**: Coupled exclusively to NeoForge on 1.21.1 (`mc1.21.1/dev`).
- **Deficiencies**:
  - Direct compile-time binding to `net.neoforged.neoforge.*` and `net.neoforged.fml.*`.
  - Zero abstraction between kinetic math/contraption logic and NeoForge capabilities (`IItemHandler`, `IFluidHandler`).
  - Tightly bound to NeoForge's Registrate fork.
  - Cannot build or run on Fabric without external shims.

### 2.2 Fabricators-of-Create (Porting-Lib Approach)
- **Status**: Forked downstream repository using Kotlin DSL (`build.gradle.kts`) targeting Fabric on 1.20.1/1.21.1.
- **Deficiencies**:
  - Relies on **Porting-Lib**, a massive emulation layer attempting to fake Forge/NeoForge on Fabric.
  - Re-implements `LazyOptional`, Forge model loaders, Forge events, and Forge capability wrappers on top of Fabric.
  - Substantial maintenance lag: every minor Minecraft release or internal Refactor (e.g. Data Components, `Identifier` renames, Java 25) breaks Porting-Lib entirely.
  - Divergent codebase makes syncing upstream bugfixes and new Create features an arduous manual merge nightmare.

### 2.3 The RedFX Solution (Adopted by CreateRedux)
- **Unified Multi-Project Structure**:
  - `:common`: 100% loader-agnostic game logic, kinetics math, contraption assembly, railway pathfinding, packets, and assets.
  - `:fabric`: Minimal Fabric Loader & Fabric API adapter layer.
  - `:neoforge`: Minimal NeoForge ModDev adapter layer.
- **Merge-Hub Git Strategy**:
  - `main`: Canonical, version-agnostic hub containing all shared gameplay logic, math, assets, and documentation.
  - Version branches (`mc-26.2`, `mc-26.1`, `mc-1.21.11`): Hold only loader versions, toolchains, and strictly necessary Minecraft API divergence.
  - Features and bugfixes merged down from `main` without merge conflicts.

```
+-------------------------------------------------------------------------+
|                               :common                                   |
|   Kinetics Simulation | Contraption Assembly | Train Logistics & Math   |
|   Schematics & Processing | Ponder Scenes | Codecs & Packet Payloads    |
+------------------------------------+------------------------------------+
                                     |
                 +-------------------+-------------------+
                 |                                       |
                 v                                       v
+---------------------------------+     +---------------------------------+
|             :fabric             |     |            :neoforge            |
| - Fabric Loader & Fabric API    |     | - NeoForge ModDev               |
| - Fabric Transfer API Bridge    |     | - NeoForge Capabilities Bridge  |
| - Fabric Networking Adapter     |     | - NeoForge PayloadRegistrar     |
| - Flywheel Fabric / FRAPI       |     | - Flywheel NeoForge / ModDev    |
+---------------------------------+     +---------------------------------+
                 |                                       |
                 v                                       v
     create-fabric.jar                       create-neoforge.jar
```

---

## 3. Repository & Module Layout

The workspace is organized as follows:

```
CreateRedux/
├── gradle/
│   ├── mod.properties                # Version-agnostic mod metadata (version, id, group)
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── docs/
│   ├── DESIGN.md                     # This architectural document
│   └── PLAN.md                       # Detailed execution roadmap
├── common/
│   ├── build.gradle                  # Common Loom configuration
│   └── src/
│       ├── main/
│       │   ├── java/com/simibubi/create/
│       │   │   ├── api/              # Public Create API
│       │   │   ├── content/          # Contraptions, Kinetics, Logistics, Trains, Fluids
│       │   │   ├── foundation/       # Utility, Math, Common Rendering, Mixins
│       │   │   ├── infrastructure/   # Common Config, Common Packets, Ponder definitions
│       │   │   └── platform/         # Platform Abstraction Layer (PAL) interfaces
│       │   └── resources/            # Assets, data generators, recipes, lang, common mixins
│       └── test/                     # Loader-neutral unit tests (Stress, Contraptions, Trains)
├── fabric/
│   ├── build.gradle                  # Fabric Loom, Fabric dependencies, Jar packaging
│   └── src/
│       ├── main/
│       │   ├── java/com/simibubi/create/platform/fabric/  # PAL Fabric implementations
│       │   └── resources/
│       │       ├── fabric.mod.json
│       │       └── create.accesswidener
├── neoforge/
│   ├── build.gradle                  # NeoForged ModDev configuration, Jar packaging
│   └── src/
│       ├── main/
│       │   ├── java/com/simibubi/create/platform/neoforge/ # PAL NeoForge implementations
│       │   └── resources/
│       │       ├── META-INF/
│       │       │   ├── neoforge.mods.toml
│       │       │   └── accesstransformer.cfg
│       │       └── create.neoforge.mixins.json
├── release/                          # Staging output for packaged production jars
├── build.gradle                      # Root project orchestration & release publication
├── settings.gradle                   # Multi-project inclusions (:common, :fabric, :neoforge)
└── gradle.properties                 # Per-branch toolchain, MC version, dependencies
```

---

## 4. Git Branching & Versioning Architecture

CreateRedux strictly follows the **RedFX merge-hub model**:

### 4.1 Branch Topology
| Branch | Role | Minecraft Version | NeoForge Version | Java Version | Mapping Mode |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`main`** | Shared Hub (Version-Agnostic) | *Inert* | *Inert* | 25 | N/A |
| **`mc-26.2`** | Modern Production Primary | 26.2 | 26.2.0.81+ | 25 | Official Mojang (no remap) |
| **`mc-26.1`** | Modern Production Stable | 26.1.2 | 26.1.2.94+ | 25 | Official Mojang (no remap) |
| **`mc-1.21.11`**| Legacy Modern Transition | 1.21.11 | 21.11.45+ | 21 | Intermediary (Fabric remap) |
| **`mc-26.3+`** | Future Minecraft Releases | 26.3+ | Latest | 25+ | Official Mojang |

### 4.2 Branch Responsibility Matrix
- **`main` (Version-Agnostic Hub)**:
  - Holds `gradle/mod.properties` (`mod_version=6.1.0`, `maven_group=com.simibubi.create`).
  - Holds all platform-agnostic gameplay code in `common/src/main/java`.
  - Holds all assets, models, sounds, textures, Ponder storyboards, and localization in `common/src/main/resources`.
  - Holds all unit tests in `common/src/test`.
  - Holds project documentation (`docs/DESIGN.md`, `docs/PLAN.md`, `README.md`).
  - **Rule**: Never built directly; never published directly.
- **Version Branches (`mc-26.2`, `mc-26.1`, `mc-1.21.11`)**:
  - Hold `gradle.properties` (per-branch Minecraft version, loader versions, Java toolchain version).
  - Hold version-specific shims (e.g. `Identifier` vs `ResourceLocation`, `BlockStateModel` refactors).
  - Synchronized from `main` via `git checkout <branch> && git merge main`.

---

## 5. Platform Abstraction Layer (PAL)

The core innovation of CreateRedux is replacing heavyweight emulation libraries with a clean, low-footprint PAL defined in `com.simibubi.create.platform`.

### 5.1 Item & Fluid Transfer Abstraction
Item handling (belts, chutes, funnels, mechanical arms, vaults) and fluid handling (pipes, pumps, fluid tanks, spouts, hose pulleys) are abstracted through unified storage handles:

```
                   +-----------------------------+
                   |       Create PAL API        |
                   | - TransferUtil              |
                   | - StorageProvider<T>        |
                   | - FluidStack / ItemStack    |
                   +--------------+--------------+
                                  |
            +---------------------+---------------------+
            |                                           |
            v                                           v
+-------------------------+                 +-------------------------+
|     Fabric PAL Impl     |                 |    NeoForge PAL Impl    |
| - Storage<ItemVariant>  |                 | - IItemHandler          |
| - Storage<FluidVariant> |                 | - IFluidHandler         |
| - Transaction context   |                 | - Capability Lookups    |
+-------------------------+                 +-------------------------+
```

1. **Storage Access**:
   ```java
   public interface PlatformTransferHelper {
       StorageProvider<ItemStack> getItemStorage(Level level, BlockPos pos, Direction side);
       StorageProvider<FluidStack> getFluidStorage(Level level, BlockPos pos, Direction side);
       long insertItem(StorageProvider<ItemStack> target, ItemStack stack, boolean simulate);
       ItemStack extractItem(StorageProvider<ItemStack> source, int slot, int amount, boolean simulate);
   }
   ```
2. **Fabric Implementation**:
   - Directly binds to `ItemStorage.SIDED` and `FluidStorage.SIDED`.
   - Adapts Fabric's `Transaction` scoping to Create's simulation checks (`simulate = true/false`).
   - Converts `FluidVariant` + droplet counts (`81000` droplets = 1 bucket) seamlessly to Create's millibucket (`1000` mB = 1 bucket) standard.
3. **NeoForge Implementation**:
   - Directly binds to `level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side)` and `Capabilities.FluidHandler.BLOCK`.
   - Interacts with standard NeoForge `IItemHandler` and `IFluidHandler` methods.

### 5.2 Registration & Data Generation
Upstream uses Registrate heavily. CreateRedux provides a loader-agnostic `CreateRegistrate` abstraction:
- **Common Engine**: Defines entries with standard vanilla registry objects (`Holder<Block>`, `Holder<Item>`, `BlockEntityType<?>`).
- **Fabric Hook**: Registers directly via `Registry.register(BuiltInRegistries.BLOCK, ...)` and Fabric API's `FabricItemGroup`.
- **NeoForge Hook**: Registers via `DeferredRegister` / `RegisterEvent`.
- **Data Generation**: Common data providers (BlockStateProvider, ItemModelProvider, RecipeProvider) run from the common source set and can be invoked from either loader's data task.

### 5.3 Networking Subsystem
Create sends numerous kinetic synchronization packets, contraption control commands, train signals, and client-server UI payloads.
- **Codec Standard**: All packets implement Minecraft's native `CustomPacketPayload`.
- **Payload Definition**:
  ```java
  public record ConfigureShaftPacket(BlockPos pos, float speed) implements CustomPacketPayload {
      public static final Type<ConfigureShaftPacket> TYPE = new Type<>(Create.asResource("configure_shaft"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureShaftPacket> STREAM_CODEC = ...;
      @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
  }
  ```
- **Platform Dispatchers**:
  - **Fabric**: Registered via `PayloadTypeRegistry.playC2S().register(...)` and received via `ServerPlayNetworking.registerGlobalReceiver(...)`.
  - **NeoForge**: Registered in `RegisterPayloadHandlersEvent` with `PayloadRegistrar`.

### 5.4 Access Transformation
Instead of managing divergent access mechanisms manually:
- Maintain an internal single-source access declaration.
- Generate `src/main/resources/create.accesswidener` for Fabric Loom.
- Generate `src/main/resources/META-INF/accesstransformer.cfg` for NeoForge ModDev.

---

## 6. Rendering Architecture: Flywheel 1.0 & Multiplatform Shaders

### 6.1 Flywheel 1.0 Multi-Loader Integration
Upstream Create relies on Flywheel for GPU-instanced rendering of rotational components (shafts, cogs, gearboxes, belts) and moving contraption blocks.
- Flywheel 1.0 (`dev.engine-room.flywheel`) is modern, modularized, and natively supports:
  - `flywheel-common` (API, instancing structures, shader pipeline)
  - `flywheel-fabric` (Fabric Loom, Fabric Loader)
  - `flywheel-neoforge` (NeoForged ModDev)
- `:common` depends on `flywheel-common` to declare visual instances:
  ```java
  public class ShaftVisual extends AbstractBlockEntityVisual<ShaftBlockEntity> implements SimpleDynamicVisual { ... }
  ```
- Platform subprojects package their respective Flywheel loader implementation.

### 6.2 Rendering Fallback Pipeline
For systems with incompatible GPU drivers, Sodium/Iris/Embeddium shader pipeline quirks, or when Flywheel is disabled:
- Every kinetic block entity and contraption renderer retains a standard `BlockEntityRenderer<T>` fallback.
- Utilizes vanilla `PoseStack`, `MultiBufferSource`, and `ModelData` / baked quads.

---

## 7. Minecraft Evolution & Compatibility Matrix

| Aspect | Minecraft 1.21.11 | Minecraft 26.1.x | Minecraft 26.2 | Future (26.3+) |
| :--- | :--- | :--- | :--- | :--- |
| **Java Toolchain** | Java 21 | Java 25 | Java 25 | Java 25+ |
| **Resource Identifier** | `Identifier` (Fabric) / `ResourceLocation` (NeoForge 21) | `Identifier` (unified Mojang) | `Identifier` | `Identifier` |
| **Fabric Mappings** | `intermediary` (requires `remapJar`) | `official` (Mojang direct, no remap) | `official` (no remap) | `official` (no remap) |
| **NeoForge Mappings** | `official` (Mojang) | `official` (Mojang) | `official` (Mojang) | `official` (Mojang) |
| **Block Model System**| `BakedModel` / `DelegateBakedModel` | `BlockStateModel` / Dynamic models | `BlockStateModel` | `BlockStateModel` |
| **Entity Relocations** | `AbstractSkeleton` in `monster.skeleton` | `monster.skeleton` | `monster.skeleton` | Modern structure |
| **Data Components** | Full Data Component System | Refined Component Codecs | Modern Component Codecs | Modern Component Codecs |

---

## 8. Verification & Quality Assurance Strategy

To prevent silent failures and runtime crashes (such as missing mixin targets or unmapped production symbols), CreateRedux adopts the exact pre-publish verification regimen proven in RedFX:

### 8.1 Automated Verification Tasks
- **Fabric Packaged Jar Test (26.x)**:
  ```bash
  ./gradlew :fabric:runClient -PtestJar
  ```
  Forces Loom to load the packaged jar rather than loose class files, verifying mixins and manifest references in production conditions.
- **Fabric Production Client Test (1.21.11)**:
  ```bash
  ./gradlew :fabric:runProdClient
  ```
  Launches the actual intermediary-namespace client to ensure `remapJar` succeeded and all `@Mixin` targets resolve.
- **NeoForge Packaged Jar Test**:
  ```bash
  ./gradlew :neoforge:runClient -PtestJar
  ```
  Isolates the packaged jar into `neoforge/run-testjar/mods/` to ensure FML loads the final artifact instead of dev class folders.

### 8.2 In-Game Headless Smoke Test Harness
A headless automated verification runner (`scripts/run_smoke_test.py`) that boots both Fabric and NeoForge clients into singleplayer, validates block registrations, constructs a rotational network, and confirms:
1. No crash during boot and registry freeze.
2. Stress network calculations tick correctly.
3. Contraption assembly succeeds without exceptions.
4. Clean shutdown and chunk serialization.
