# Random Mass Play Report

## What Changed

Implemented changes:

- `RandomDeck` now caches immutable per-catalogue/per-format candidate pools for class cards and neutrals, and clones only when inserting into a deck.
- `ListCardCatalogue` now uses single-pass loop-based deck candidate queries instead of composing multiple stream-heavy queries.
- `Card` now caches quest status when its `CardDesc` is assigned, so `isQuest()` is a field read instead of repeated desc/attribute lookups.
- `CardAttributeMap` now keeps a direct transient `Card` backreference instead of allocating `WeakReference` wrappers on hot clone paths.
- `GameContext.fromDecks(...)` now constructs the context with the provided catalogue and format directly.
- `BaseMap` now uses an enum-indexed copy-on-write backing store instead of eagerly cloning `EnumMap` contents.
- The benchmark now uses `GameStateValueBehaviour` for both players in `Standard`, with:
  - `maxDepth=2`
  - `parallel=false`
  - `timeout=0`
  - `lethalTimeout=0`

Benchmark source:

- `spellsource-game-benchmarks/src/jmh/java/com/hiddenswitch/spellsource/game/benchmarks/RandomMassPlayBenchmarks.java`
- `spellsource-game-benchmarks/src/jmh/java/com/hiddenswitch/spellsource/game/benchmarks/GameStateCloneBenchmarks.java`

## Validation

Completed:

- `./gradlew :spellsource-game:compileJava :spellsource-testutils:compileJava :spellsource-game-benchmarks:jmhClasses`
- `./gradlew :spellsource-cards-git:test --tests com.hiddenswitch.spellsource.tests.cards.GameStateValueBehaviourTest`
- `./gradlew :spellsource-game-benchmarks:jmhJar`
- `./gradlew :spellsource-cards-git:test`
- Cleared generated mass-play traces from `spellsource-cards-git/src/test/resources/traces` (kept the tracked `default-trace.json`)

Not completed:

- Full repository test suite

## Benchmark Setup

Workload:

- `Standard` format
- many concurrent full game sessions
- `GameStateValueBehaviour` vs `GameStateValueBehaviour`
- one benchmark op = one full completed game

JMH command shape:

```bash
java -jar spellsource-game-benchmarks/build/libs/spellsource-game-benchmarks-0.10.4-jmh.jar \
  com.hiddenswitch.spellsource.game.benchmarks.RandomMassPlayBenchmarks.randomMassPlay \
  -wi 0 -i 1 -r 4s -f 1 -t <threads> -prof gc \
  -jvmArgsAppend --enable-preview -jvmArgsAppend -Xms2g -jvmArgsAppend -Xmx2g
```

JFR command shape:

```bash
java -jar spellsource-game-benchmarks/build/libs/spellsource-game-benchmarks-0.10.4-jmh.jar \
  com.hiddenswitch.spellsource.game.benchmarks.RandomMassPlayBenchmarks.randomMassPlay \
  -wi 0 -i 1 -r 6s -f 1 -t <threads> -prof jfr:dir=<dir> \
  -jvmArgsAppend --enable-preview -jvmArgsAppend -Xms2g -jvmArgsAppend -Xmx2g
```

Notes:

- For GSVB, seed discovery was expensive enough that the reusable validated seed pool was reduced to `1`.
- That keeps setup bounded, but it also means the measured workload is one stable successful Standard seed replayed many times.

## Results

### Fresh throughput scaling with GSVB

| Threads | Games/sec | Speedup vs 1T | Alloc/op | GC time during 4s run |
| --- | ---: | ---: | ---: | ---: |
| 1 | 0.455 | 1.00x | 9.55 GB | 212 ms |
| 2 | 0.874 | 1.92x | 7.38 GB | 260 ms |
| 4 | 1.459 | 3.21x | 6.59 GB | 559 ms |
| 8 | 1.824 | 4.01x | 6.24 GB | 1644 ms |

### Delta vs prior run

- `1` thread: `+12.1%`
- `2` threads: `+7.8%`
- `4` threads: `+1.5%`
- `8` threads: `+3.0%`

### Interpretation

- `1` to `2` threads is still the best scaling region.
- `4` and `8` threads remain worthwhile, but efficiency falls off as GC pressure climbs.
- The recent fixes improved throughput modestly, but they did not materially change the shape of the curve.
- `8` threads is still near saturation for this configuration, and the saturated run still spends a large fraction of wall time in GC.

## JFR Findings

Recordings:

- `spellsource-game-benchmarks/build/jfr-gsvb-20260311/t1/.../profile.jfr`
- `spellsource-game-benchmarks/build/jfr-gsvb-20260311/t8/.../profile.jfr`

### CPU hotspots

`1` thread:

- `java.util.HashMap.getNode(Object)` at `34.95%`
- `net.demilich.metastone.game.cards.CardAttributeMap.get(Object)` at `8.09%`
- `net.demilich.metastone.game.cards.desc.CardDesc.getType()` at `6.31%`
- `java.util.AbstractList$Itr.hasNext()` at `2.59%`
- `net.demilich.metastone.game.decks.DeckFormat.getSets()` at `2.43%`

`8` threads:

- `java.util.HashMap.getNode(Object)` at `21.88%`
- `java.util.AbstractList$Itr.next()` at `8.30%`
- `net.demilich.metastone.game.cards.CardAttributeMap.get(Object)` at `7.10%`
- `java.util.AbstractList$Itr.hasNext()` at `4.09%`
- `net.demilich.metastone.game.cards.desc.CardDesc.getType()` at `2.89%`
- `net.demilich.metastone.game.entities.EntityZone.get(int)` at `2.81%`
- `net.demilich.metastone.game.entities.Entity.getAttributeValue(Attribute)` at `2.79%`

### Allocation hotspots

`1` thread allocation-by-site:

- `java.util.EnumMap.clone()` at `63.86%`
- `java.util.stream.ReferencePipeline.filter(Predicate)` at `3.17%`
- `net.demilich.metastone.game.logic.CustomCloneable.clone()` at `2.88%`
- `net.demilich.metastone.game.GameContext.<init>(GameContext)` at `2.21%`
- `java.util.LinkedHashMap.newNode(...)` at `2.17%`

`8` threads allocation-by-site:

- `java.util.EnumMap.clone()` at `64.06%`
- `java.util.AbstractMap.clone()` at `3.06%`
- `net.demilich.metastone.game.logic.CustomCloneable.clone()` at `3.05%`
- `java.util.LinkedHashMap.newNode(...)` at `2.84%`
- `java.util.EnumMap.<init>(Class)` at `2.18%`

## Conclusions

The implemented setup/query changes remain effective: the old `Card.isQuest()` and catalogue-query bottlenecks did not return. The fresh rerun confirms that GSVB is still dominated by gameplay-state cloning and map lookups:

- `HashMap.getNode(...)`
- `CardAttributeMap.get(...)`
- `EnumMap.clone()`
- iterator-heavy entity-zone traversal

That means the next wins are still inside clone-heavy state expansion and hot action-evaluation loops, not in deck construction or card-catalogue setup.

## Clone Baseline And COW Comparison

Dedicated clone benchmark:

- `spellsource-game-benchmarks/src/jmh/java/com/hiddenswitch/spellsource/game/benchmarks/GameStateCloneBenchmarks.java`
- workload: `GameContext.clone()` on a representative `Standard` midgame state at turn `6`

### Clone benchmark results

| Variant | Throughput | Average time | Alloc/op |
| --- | ---: | ---: | ---: |
| Baseline | `72.814 ops/ms` | `0.012 ms/op` | `111,714 B/op` |
| COW `BaseMap` | `112.778 ops/ms` | `0.009 ms/op` | `38,611 B/op` |

### Clone delta

- Throughput: `+54.9%`
- Average clone time: about `-25%`
- Allocation per clone: about `-65.4%`

### Clone JFR findings

Baseline clone-only profile:

- `java.util.EnumMap.clone()` accounted for `66.53%` of allocation pressure
- CPU still showed `EntityZone` traversal and `Player` reconstruction, but `EnumMap.clone()` was a visible hot method

After COW `BaseMap`:

- `java.util.EnumMap.clone()` dropped out of the clone profile
- top allocation sites shifted to:
  - `java.util.LinkedHashMap.newNode(...)` at `20.79%`
  - `java.util.AbstractMap.clone()` at `15.89%`
  - `net.demilich.metastone.game.cards.BaseMap$SharedData.<init>(int)` at `13.37%`

Interpretation:

- The map-layer change achieved the intended effect: cloned game states now share attribute/desc backing until mutation instead of duplicating enum-map storage eagerly.
- The remaining clone cost is now more about player lookup maps, zone reconstruction, and general object graph cloning than attribute-map duplication.

## End-To-End Spot Check After COW

Spot-check reruns of the GSVB mass-play benchmark after the `BaseMap` COW change:

| Threads | Before | After | Delta | Alloc/op Before | Alloc/op After |
| --- | ---: | ---: | ---: | ---: | ---: |
| 1 | `0.455 games/s` | `0.545 games/s` | `+19.8%` | `9.55 GB` | `3.58 GB` |
| 8 | `1.824 games/s` | `3.840 games/s` | `+110.5%` | `6.24 GB` | `2.45 GB` |

These were spot checks, not a full fresh thread sweep, but they are consistent with the isolated clone benchmark: removing eager base-map cloning materially reduces both allocation pressure and the GC wall-time that previously capped GSVB scaling.

## Next Best Opportunities

Highest-value follow-ups after this pass:

1. Reduce `EnumMap.clone()` traffic in entity/game-state cloning paths.
2. Audit `GameStateValueBehaviour` expansion for avoidable full-context clones.
3. Replace remaining iterator-heavy hot loops in entity-zone traversal and action expansion.
4. Reduce `HashMap` lookup churn in hot gameplay paths where key sets are fixed or enum-based.
5. Reduce clone-time `LinkedHashMap` and zone-rebuild overhead now that `EnumMap.clone()` is no longer dominant.

## File Summary

Main changes landed in:

- `spellsource-game/src/main/java/net/demilich/metastone/game/cards/Card.java`
- `spellsource-game/src/main/java/net/demilich/metastone/game/cards/CardAttributeMap.java`
- `spellsource-game/src/main/java/net/demilich/metastone/game/cards/catalogues/ListCardCatalogue.java`
- `spellsource-game/src/main/java/net/demilich/metastone/game/GameContext.java`
- `spellsource-testutils/src/main/java/com/hiddenswitch/spellsource/testutils/RandomDeck.java`
- `spellsource-game-benchmarks/src/jmh/java/com/hiddenswitch/spellsource/game/benchmarks/RandomMassPlayBenchmarks.java`
