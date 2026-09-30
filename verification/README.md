# Behavior-preserving render reuse verification

The production changes retain fresh block/sky light queries and cache only the exact
`String.split("\\|")` result for each custom-text entity row. The split array stays
private behind immutable `MessageParts`; text reads, font widths, graphics operations,
NBT reads and edit dirty notifications remain at their original call sites.

## Run

Use JDK 17 and Python 3. The isolated trace suite needs no downloaded dependencies:

```sh
JAVA_HOME=/path/to/jdk17 python3 tools/verify-render-reuse.py
```

Gradle verification for the supported Minefed 1.20.4 target:

```sh
./gradlew :verifyRenderReuse :fabric:setupFiles :fabric:verifyLightTypeContract :fabric:remapJar \
  -PminecraftVersion=1.20.4 --configure-on-demand
./gradlew :verifyRenderReuseForge :forge:verifyLightTypeContract :forge:build \
  -PminecraftVersion=1.20.4 --configure-on-demand
```

Set `-PpythonExecutable=python` where Python 3 is installed under that name. Root
`check` runs both trace suites, including the generated Forge source comparison.
Each loader's `check` runs its own real-dependency LightType identity contract.
The extra Gradle tasks are registered only for Minecraft 1.20.4; the existing
multi-version source setup/build workflow remains unchanged.

`verifyRenderReuseForge` depends on the repository's actual `:forge:setupFiles`
workflow. It checks byte-for-byte parity of all three affected production sources
before compiling and exercising the generated Forge sources against the fixtures.

## What is executed

- The complete current production `RenderCustomText` and `BlockCustomTextBase`,
  including the public render entry, queued callback, both faces, entity constructor,
  `getMessage`, `setMessages`, and NBT read/write entry points
- The exact current production `RenderRailsMixin.getLights` method body, extracted
  into a test-only class without changing its instructions; the full mixin is also
  compiled normally by each loader build
- Recording fixtures only at the Minecraft/MTR boundaries (world, queue, graphics,
  NBT, registry, native light query). None is included in a production source set
- A frozen pre-change text renderer and light-query method from commit
  `91cdd24f6fbf94a1b12f5432ce93d7168246e769`; only the renderer class name is changed

The trace comparison checks every recorded call in order, raw float/double argument
bits, text draw parameters, reads after enqueue and between faces/rows, refreshed
font widths, four facings, rotation variants, coordinate overflow boundaries,
Unicode/emoji, random delimiter strings, null/default/invalid rows and existing
exceptions. HotSpot's adaptive omission of repeated exception messages is disabled
only in the test VM so identical exception class/message traces are comparable.
Separate assertions inspect cache identity, private immutable ownership, NBT/edit
invalidation, entity replacement, and missing/replaced worlds. Light tests cover
empty/repeated/boundary positions, changed world values, and each query-failure point.

The fixtures deliberately do **not** claim real game/GPU integration or performance
measurements. LightType in the trace fixture is only a query-boundary sentinel.
`verifyLightTypeContract` separately runs with the selected loader's actual resolved
MTR and Minecraft classes, checks helper/singleton `==` plus native enum ordinals,
and prints the loaded class locations and JAR SHA-256 hashes. Both layers must pass
before treating dependency identity and render call equivalence as verified.
