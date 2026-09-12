# Execution Plan: CreateRedux Porting & Implementation

## 1. Overview & Phased Roadmap

This plan details the step-by-step execution to port **Create** to modern Minecraft versions (**1.21.11**, **26.1.x**, **26.2**, and future releases) on both **Fabric** and **NeoForge**, utilizing the multi-project and merge-hub architecture proven in **RedFX**.

### 1.1 Milestone Timeline Overview
```
+-------------------------------------------------------------------------------+
| Phase 0: Project Bootstrap & Multi-Module Toolchain Setup                     |
+---------------------------------------+---------------------------------------+
                                        |
                                        v
+-------------------------------------------------------------------------------+
| Phase 1: Platform Abstraction Layer (PAL) Foundation                          |
| - Transfer APIs | Registries | Codec Packets | Configs | Access Transformation|
+---------------------------------------+---------------------------------------+
                                        |
                                        v
+-------------------------------------------------------------------------------+
| Phase 2: Upstream Source Ingestion & Modularization                           |
| - Deconstruct Upstream 1.21.1 NeoForge -> Extract Common Logic               |
+---------------------------------------+---------------------------------------+
                                        |
                                        v
+-------------------------------------------------------------------------------+
| Phase 3: Subsystem Migration & Loader Binding                                 |
| - Kinetics (3.1) -> Contraptions (3.2) -> Logistics (3.3)                     |
| - Fluids (3.4)   -> Trains (3.5)       -> Schematics (3.6)                    |
+---------------------------------------+---------------------------------------+
                                        |
                                        v
+-------------------------------------------------------------------------------+
| Phase 4: Rendering & Flywheel 1.0 Integration                                 |
| - Flywheel Common/Fabric/NeoForge + BER Fallbacks                             |
+---------------------------------------+---------------------------------------+
                                        |
                                        v
+-------------------------------------------------------------------------------+
| Phase 5: Ponder Interactive Tutorial System                                   |
+---------------------------------------+---------------------------------------+
                                        |
                                        v
+-------------------------------------------------------------------------------+
| Phase 6: Multi-Version Branch Rollout & Adaptation                            |
| - Establish mc-1.21.11 -> Forward-Port to mc-26.1 & mc-26.2 (Java 25)         |
+---------------------------------------+---------------------------------------+
                                        |
                                        v
+-------------------------------------------------------------------------------+
| Phase 7: Verification, Automated Smoke Tests & CI/CD Pipeline                 |
+-------------------------------------------------------------------------------+
```

---

## 2. Detailed Execution Phases

### Phase 0: Project Bootstrap & Multi-Module Toolchain Setup
**Goal**: Initialize the CreateRedux repository with the RedFX Gradle build structure, git repository, and multi-project configuration.

- [ ] **Step 0.1: Git Repository Initialization**
  - Initialize local git repo in `CreateRedux`.
  - Create initial commit with `.gitignore`, `.editorconfig`, and directory skeletons (`common/`, `fabric/`, `neoforge/`, `gradle/`, `docs/`, `release/`).
- [ ] **Step 0.2: Gradle Multi-Project Configuration**
  - Configure `settings.gradle` including `:common`, `:fabric`, and `:neoforge`.
  - Configure Foojay toolchain resolver convention plugin.
- [ ] **Step 0.3: Mod Metadata Separation**
  - Create `gradle/mod.properties` on `main`:
    ```properties
    mod_id=create
    mod_name=Create
    mod_version=6.1.0
    maven_group=com.simibubi.create
    modrinth_project_id=create
    curseforge_project_id=328085
    ```
  - Setup root `build.gradle` to load `gradle/mod.properties` into `rootProject.ext`.
- [ ] **Step 0.4: Subproject Build Scripts**
  - Setup `common/build.gradle` using Fabric Loom with `useLegacyMixinAp = false`.
  - Setup `fabric/build.gradle` with Fabric Loader, Fabric API, ModMenu, and packaging `:common` sources.
  - Setup `neoforge/build.gradle` using `net.neoforged.moddev`, packaging `:common` sources.
  - Setup staging tasks (`copyJarToRelease`) targeting the root `release/` folder.

---

### Phase 1: Platform Abstraction Layer (PAL) Foundation
**Goal**: Implement the core abstraction layer in `:common` and its concrete bindings in `:fabric` and `:neoforge`.

- [ ] **Step 1.1: Storage & Transfer Abstraction (`TransferUtil`)**
  - Define `StorageProvider<T>`, `FluidStack`, and `ItemFilter` in `common/.../platform/transfer/`.
  - Fabric: Implement `FabricTransferHelper` mapping to Fabric Transfer API (`ItemStorage.SIDED`, `FluidStorage.SIDED`, `Transaction`).
  - NeoForge: Implement `NeoForgeTransferHelper` mapping to `Capabilities.ItemHandler.BLOCK` and `Capabilities.FluidHandler.BLOCK`.
- [ ] **Step 1.2: Content Registration Engine (`CreateRegistrate`)**
  - Implement a streamlined registration abstraction wrapping vanilla registry mechanisms.
  - Support registration of Blocks, BlockEntityTypes, Items, Fluids, EntityTypes, SoundEvents, ParticleTypes, and RecipeTypes.
  - Wire Fabric registry calls via `BuiltInRegistries` during `ModInitializer`.
  - Wire NeoForge registry calls via `RegisterEvent` / `DeferredRegister`.
- [ ] **Step 1.3: Unified Networking Layer**
  - Implement network packet protocol using Minecraft's native `CustomPacketPayload`.
  - Provide `PlatformNetworking` helper for C2S and S2C dispatch:
    - Fabric: Bind to `ServerPlayNetworking.send` / `ClientPlayNetworking.send` and `PayloadTypeRegistry`.
    - NeoForge: Bind to `PacketDistributor` and `PayloadRegistrar`.
- [ ] **Step 1.4: Multiplatform Configuration System**
  - Define config categories: `KineticsConfig`, `LogisticsConfig`, `SchematicsConfig`, `ClientConfig`, `WorldgenConfig`.
  - Hook into NeoForge's native `ModConfig`.
  - Hook into Fabric via standard JSON/TOML parser with hot-reloading capability.
- [ ] **Step 1.5: Access Widener & Transformer Pipeline**
  - Consolidate class/method access requirements.
  - Generate `create.accesswidener` for Fabric Loom and `accesstransformer.cfg` for NeoForge.

---

### Phase 2: Upstream Source Ingestion & Modularization
**Goal**: Extract Create's gameplay logic from upstream `Creators-of-Create/Create` (`mc1.21.1/dev`), strip direct Forge/NeoForge dependencies, and migrate to `:common`.

- [ ] **Step 2.1: Content Separation Analysis**
  - Categorize upstream packages:
    - Pure Common (move to `:common` immediately): `content/kinetics/`, `content/contraptions/components/`, `content/trains/track/`, `content/schematics/`.
    - Platform-Dependent (extract to PAL): `content/fluids/` (Fluid tanks, hose pulleys), `content/logistics/` (chutes, funnels, belts), `infrastructure/config/`.
- [ ] **Step 2.2: Asset and Data Migration**
  - Copy all models, blockstates, textures, sounds, and localization files to `common/src/main/resources/assets/create/`.
  - Copy recipe definitions and tags to `common/src/main/resources/data/create/`.
- [ ] **Step 2.3: Data Components & Serialization**
  - Adapt Create's item components to modern Minecraft data components (`DataComponentType`, `PatchedDataComponentMap`).

---

### Phase 3: Subsystem Migration & Loader Binding

#### Milestone 3.1: Kinetics Engine & Stress Network
- [ ] Migrate `KineticBlock`, `KineticBlockEntity`, and rotational network propagation math.
- [ ] Implement `StressImpactRegistry` and `StressCapacityRegistry` in `:common`.
- [ ] Port rotational synchronization packets (`KineticBlockEntity` state sync).
- [ ] Verify shaft, cogwheel, gearbox, clutch, gearshift, and water wheel rotational calculations on both loaders.

#### Milestone 3.2: Contraptions & Physical Movement
- [ ] Migrate contraption assembly logic: Mechanical Piston, Windmill Bearing, Mechanical Bearing, Gantry Carriage, Cart Assembler.
- [ ] Port `ContraptionCollider` and entity riding/interaction physics.
- [ ] Decouple contraption inventory storage from Forge item handlers; bind to PAL `StorageProvider`.
- [ ] Implement contraption movement packet synchronization.

#### Milestone 3.3: Logistics & Item Transport
- [ ] Port Mechanical Belts, Chutes, Funnels, Mechanical Arms, and Depots.
- [ ] Bridge belt inventory transfer to Fabric Transfer API transactions and NeoForge `IItemHandler`.
- [ ] Port Display Links, Nixie Tubes, and Content Observers.

#### Milestone 3.4: Fluid Dynamics & Hydraulics
- [ ] Port Fluid Pipes, Mechanical Pumps, Fluid Tanks, Spouts, and Hose Pulleys.
- [ ] Adapt open-ended pipe fluid spill effects to loader-neutral particle spawning.
- [ ] Reconcile Fabric's 81,000-droplet bucket unit with Create's 1,000 mB standard in fluid pipes and gauges.

#### Milestone 3.5: Railway Infrastructure & Trains
- [ ] Port Track placing, Curved Tracks, Track Switching, and Monorail/Dual-rail bogeys.
- [ ] Port Train assembly, train navigation, station scheduling, and signal blocks.
- [ ] Validate train movement across chunk boundaries and dimensional portals.

#### Milestone 3.6: Schematics & Creative Engineering Tools
- [ ] Port Schematic Table, Schematicannon, Handheld Worldshaper, and Symmetry Wand.
- [ ] Ensure NBT / component schematic file compatibility across both platforms.

---

### Phase 4: Rendering & Flywheel 1.0 Integration
**Goal**: Integrate GPU-accelerated instanced rendering across Fabric and NeoForge using Flywheel 1.0.

- [ ] **Step 4.1: Flywheel Common Dependencies**
  - Add `dev.engine-room.flywheel:flywheel-common` to `:common`.
  - Add `flywheel-fabric` to `:fabric` and `flywheel-neoforge` to `:neoforge`.
- [ ] **Step 4.2: Visual Instance Definitions**
  - Port kinetic block visuals (`ShaftVisual`, `CogVisual`, `BeltVisual`, `FlwModelInstance`).
  - Port moving contraption instanced rendering.
- [ ] **Step 4.3: BER CPU Fallback Engine**
  - Implement full BlockEntityRenderer fallbacks for all kinetic components when Flywheel is disabled or unsupported by hardware.
  - Verify seamless rendering with Sodium / Iris (Fabric) and Embeddium / Oculus (NeoForge).

---

### Phase 5: Ponder Interactive Tutorial System
**Goal**: Integrate the in-game Ponder 3D animated tutorial scenes.

- [ ] **Step 5.1: Ponder Dependency Configuration**
  - Include `net.createmod.ponder:ponder-common`, `ponder-fabric`, and `ponder-neoforge`.
- [ ] **Step 5.2: Scene Registration**
  - Register all standard Create Ponder storyboards (Kinetics, Contraptions, Logistics, Trains, Fluids).
  - Verify UI rendering, camera pathing, instruction text, and tooltips on both loaders.

---

### Phase 6: Multi-Version Branch Rollout & Adaptation
**Goal**: Deploy and maintain across target Minecraft versions using the RedFX merge-hub model.

```
                  +-----------------------------------+
                  |               main                |
                  |  - Shared PAL API & Common Code   |
                  |  - All Assets, Models & Lang      |
                  |  - Unit Tests & Documentation     |
                  +-----------------+-----------------+
                                    |
          +-------------------------+-------------------------+
          |                         |                         |
          v                         v                         v
+-------------------+     +-------------------+     +-------------------+
|     mc-1.21.11    |     |      mc-26.1      |     |      mc-26.2      |
| - MC 1.21.11      |     | - MC 26.1.2       |     | - MC 26.2         |
| - Java 21         |     | - Java 25         |     | - Java 25         |
| - Intermediary    |     | - Official Mojang |     | - Official Mojang |
| - Remap Pipeline  |     | - No remap step   |     | - No remap step   |
+-------------------+     +-------------------+     +-------------------+
```

- [ ] **Step 6.1: Transition Branch (`mc-1.21.11`)**
  - Java 21 toolchain.
  - Use `fabric-loom-remap` plugin for Fabric.
  - Reconcile `Identifier` (Fabric) vs `ResourceLocation` (NeoForge 21.11).
  - Remap production jar to intermediary.
- [ ] **Step 6.2: Modern Branch (`mc-26.1`)**
  - Upgrade toolchain to **Java 25**.
  - Switch Fabric to `fabric-loom` 1.17+ using official Mojang mappings in production (no remap step).
  - Adapt block model rendering to modern `BlockStateModel` / dynamic quad models.
- [ ] **Step 6.3: Modern Primary Branch (`mc-26.2`)**
  - Target Minecraft 26.2 and NeoForge 26.2.0.81+.
  - Synchronize from `main` via `git merge main`.
  - Validate all mixins and hooks against final 26.2 deobfuscated symbols.

---

### Phase 7: Verification, Automated Smoke Tests & CI/CD Pipeline
**Goal**: Guarantee release stability and eliminate publishing bugs.

- [ ] **Step 7.1: Pre-Publish Verification Tasks**
  - Configure `:fabric:runClient -PtestJar` (26.x) and `:fabric:runProdClient` (1.21.11).
  - Configure `:neoforge:runClient -PtestJar` with isolated `run-testjar/mods` staging.
- [ ] **Step 7.2: Headless Smoke Test Automation**
  - Implement `scripts/run_smoke_test.py` to launch headless clients and run automated checks:
    - Mod initialization without classloading errors.
    - Registry consistency across both loaders.
    - Kinetic stress network ticking and kinetic overload calculation.
    - Contraption assembly and disassembly without chunk corruption.
- [ ] **Step 7.3: Publishing Integration**
  - Setup Modrinth Minotaur and CurseForge Gradle tasks in root `build.gradle`.
  - Configure GitHub Releases publishing reading notes from `release/release-note-*.md`.

---

## 3. Risk Analysis & Mitigation Matrix

| Risk | Impact | Likelihood | Mitigation Strategy |
| :--- | :--- | :--- | :--- |
| **Fabric Transfer API vs Capability Incompatibilities** | High | Medium | Encapsulate transaction scope within PAL `StorageProvider`. Ensure simulation rollback in Fabric `Transaction` faithfully mirrors NeoForge's `simulate=true`. |
| **Breaking Model Changes in MC 26.x (`BlockStateModel`)** | High | High | Isolate model rendering adapters into version branches; keep core kinetics math in `common` decoupled from quad generation. |
| **Flywheel Version Desync Across Loaders** | Medium | Low | Flywheel 1.0 has synchronized multiplatform releases; use matching API specs and maintain pure BER fallbacks. |
| **Merge Conflicts Across Version Branches** | High | Low | Strictly enforce the RedFX rule: `gradle.properties` and version shims are per-branch; `main` holds only version-agnostic code. |
| **Java 25 Bytecode Verification on Older Toolchains** | Medium | Low | Use Gradle Foojay toolchain resolver to automatically download and provision exact OpenJDK 25 and 21 binaries. |

---

## 4. Maintenance Runbook: Supporting Future Minecraft Versions

When Mojang releases a new Minecraft version (e.g. `26.3` or `27.0`):

1. **Cut New Branch**:
   ```bash
   git checkout main
   git checkout -b mc-26.3
   ```
2. **Update `gradle.properties`**:
   - Bump `minecraft_version=26.3`
   - Bump `fabric_api_version` and `loader_version`
   - Bump `neoforge_version`
   - Update `minecraft_dependency` SemVer and Maven ranges
3. **Resolve API Diffs**:
   - If vanilla changed internal class names, update version-specific shims or access wideners.
   - Core kinetics, trains, contraptions, and recipes in `:common` remain completely untouched.
4. **Run Verification**:
   ```bash
   ./gradlew clean build
   ./gradlew :fabric:runClient -PtestJar
   ./gradlew :neoforge:runClient -PtestJar
   ```
5. **Stage & Publish**:
   - Built jars automatically stage into `release/create-<version>-mc26.3.x-[fabric|neoforge].jar`.
