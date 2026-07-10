# Develop Logbook

Running log of findings and fixes on the `develop` branch. Newest entries first.

## 2026-06-10 — Container reuse: postgres, keycloak and redis survive between runs

`./gradlew spellsource-server:run` now reuses Postgres, Keycloak, and Redis
across runs — they keep running (with their data and ports) after the JVM
exits, and the next run reattaches in seconds. The GraphQL container is
always fresh because it executes the working tree's code, but it joins the
same network to reach the reused services by alias. Verified across four
consecutive runs: identical container IDs each time, app ready in ~29s warm,
discovery handshake and GraphQL queries working.

**How it works:**

- Reuse is gated on testcontainers' own flag: `TESTCONTAINERS_REUSE_ENABLE=true`
  (set by the run task) or `testcontainers.reuse.enable=true` in
  `~/.testcontainers.properties`. Without the flag — e.g. plain test runs —
  everything behaves exactly as before (fresh containers, Ryuk cleanup).
- `StandaloneApplication` chains `.withReuse(REUSE)` on the three service
  containers and labels them `com.hiddenswitch.spellsource=localdev`.
- `PersistentNetwork` (spellsource-containers): a fixed-name docker network
  (`spellsource-dev`) created on demand and never deleted. `Network.SHARED`
  is recreated per JVM and its id is part of the testcontainers reuse hash,
  which would silently defeat reuse; it is still used when the flag is off.
- Clean slate when needed: `./gradlew spellsource-server:purgeLocalServices`
  removes the labeled containers (and their data) and the network.

**Bugs this surfaced and their fixes:**

1. **Silent JVM exit on deploy failure** — `EntryPoint.main` ignored the
   `Future` returned by `Application.deploy()`; since the deploy chain runs
   inside `Future.compose`, any startup exception became a failed future
   nobody observed and the JVM exited 0 with no output. Now logs the failure
   and exits 1.
2. **Flyway checksum mismatch on any persistent database** —
   `V7__Create_cards` and `R__0001_Update_cards` compute their checksum via
   `Environment.cardsChecksum()`, which hashed whatever happened to be in
   `ClasspathCardCatalogue` at call time. Flyway resolved V7's checksum
   before the catalogue loaded (1879031344) but validated on later runs
   after loading (102709437), so the second run against a kept database
   always failed validation. Fresh-per-run databases never noticed (nothing
   to validate against). Fixes:
   - `cardsChecksum()` now calls the idempotent `loadCardsFromPackage()`
     first, making the checksum a function of the cards alone.
   - `Environment.migrate()` sets `validateOnMigrate(false)`: the
     pre-existing `flyway.repair()` call already realigns every checksum it
     can, so validation only ever fired on Java migration checksums — which
     Flyway 9 repair does *not* realign — i.e. it could only break startup
     on historical databases with no remedy. Repeatable migrations still
     rerun on checksum change (that's planning, not validation), so
     `R__0001_Update_cards` continues to re-publish cards when cards change.

## 2026-06-10 — One-command local game services for the Unity editor

**The command (pre-existing, now working on Linux):**

```
./gradlew spellsource-server:run
```

This starts everything a running Unity editor needs to click Play: Postgres,
Keycloak, Redis, and the node GraphQL gateway as Testcontainers (random host
ports, auto-reaped by Ryuk on exit), plus the in-JVM verticles — gRPC Gateway
(`:8081`), Java GraphQL backend (`:4000`), Matchmaking, ClusteredGames, and
the UDP `Broadcaster`. No gradle upgrade was needed (wrapper 8.14.3 works).

How the Unity editor finds it: on Play, `ServerDiscovery.cs` multicasts
`LOXX` to `230.0.0.1:6112`; the server's `Broadcaster` replies
`SPLL<graphql url>` with the current mapped port (e.g.
`http://localhost:32871/graphql`), so the random container ports never need
to be configured anywhere. The run also rewrites
`spellsource-web/.env.local` and `spellsource-graphql/.env.local` with the
current ports for web development.

Verified end-to-end on this machine: simulated the `LOXX` handshake, got the
`SPLL` reply, and ran a GraphQL query against the discovered URL successfully.

**Two bugs fixed to make this work on native Linux Docker:**

1. `GraphQLContainer` (spellsource-containers): the node gateway stitches the
   Java backend schema from `http://host.docker.internal:4000/graphql`, but
   `host.docker.internal` only exists automatically on Docker Desktop. Added
   `withExtraHost("host.docker.internal", "host-gateway")` so native Linux
   engines resolve it.
2. `spellsource-graphql/src/schema/spellsource.ts`:
   `createSpellsourceSchema` introspected the Java backend exactly once with
   no retry. The container boots before the Java verticles, so a failed fetch
   crashed the node process and nodemon idled forever ("app crashed - waiting
   for file changes"). Wrapped the introspection in a retry loop (5s,
   mirrors the existing flyway-readiness loop in `server.ts`).

Notes:
- The run task sets `TESTCONTAINERS_REUSE_ENABLE=true`, but at the time of
  this entry none of the containers opted in with `withReuse(true)`, so every
  run was a fresh stack with an empty database. Superseded the same day: see
  the container reuse entry above.
- Stop with Ctrl+C; Ryuk removes the containers automatically.
- Expect ~1–2 min to fully ready: the GraphQL container intentionally sleeps
  30s (`SLEEP=30`) before introspecting Postgres, then retries until the Java
  backend is up.

## 2026-06-10 — Unity CS0161 fix and rogue run local testing evaluation

### Unity client: `EditorCompatibleApplication.platform` CS0161

`spellsource-client/src/unity/Assets/Scripts/HiddenSwitch/EditorCompatibleApplication.cs(16,13): error CS0161: not all code paths return a value`

The `platform` getter only returned inside platform-define branches
(`UNITY_ANDROID`, `UNITY_IOS`, `UNITY_STANDALONE_OSX`, `UNITY_STANDALONE_WIN`,
`UNITY_WEBGL`). On Linux (editor or standalone player) none of these are
defined, so the getter had no return path and compilation failed.

Fixes applied:

- Added an `#else` fallback returning `Application.platform`. This preserves
  the class's purpose — in the editor, the platform defines track the active
  build target, so the getter reports the *target* platform; on any platform
  without an explicit branch it now passes through the real runtime platform.
- Moved `using UnityEditor;` inside the `#if UNITY_EDITOR` guard. It was
  outside an empty guard block, which would break player builds independently
  of the CS0161 error (`EditorUserBuildSettings` usage further down was
  already correctly guarded).

### Rogue run: how to test and verify locally

**Verified working on this machine.** All existing rogue tests pass via
Testcontainers (Docker required, present and working):

```
./gradlew spellsource-server:test \
  --tests "com.hiddenswitch.framework.tests.RogueRunTests" \
  --tests "com.hiddenswitch.framework.tests.RogueCardTests"
```

Results (7/7 passing, ~55s with a warm build and cached images):

| Test | Time | Covers |
|---|---|---|
| `testStartRogueRun` | 4.7s | Run creation, initial 15-card deck + level card, INITIAL → CHOICE → PRE_MATCH |
| `testRogueRunChoice` | 4.2s | Making choices, deck updates |
| `testRogueRunMatch` | 8.1s | Full bot match through matchmaking, PRE_MATCH → IN_MATCH → post-match state |
| `testTrashCard` | 4.3s | Card removal from run deck |
| `testAfterMatchChoices` | 6.1s | Post-win choices, gold, equipment offers |
| `testEquipment` | 0.4s | Equipment card mechanics |
| `testLevel` | <0.1s | Level card HP modification |

#### Where the feature lives

- Server logic: `spellsource-server/src/main/java/com/hiddenswitch/framework/impl/RogueManager.java`
  (plus `RogueChoiceGameContext.java`). State machine: INITIAL → CHOICE →
  PRE_MATCH → IN_MATCH → CHOICE (win) / PRE_MATCH (loss with lives left) /
  FINISHED (out of lives, or `WIN_COUNT` = 8 bosses defeated).
- Persistence: Postgres tables `rogue_run` and `rogue_choice`
  (`spellsource-server/src/main/resources/db/migration/V12__Rogue.sql`),
  row-level security per player.
- API: GraphQL mutations `startRogueRun`, `makeRogueChoice`, `endRogueRun`,
  `reroll`, `skipBoss`, `trashCard`, `upgradeCard`; queries `currentRogueRun`,
  `getRogueRun(s)`, `getCurrentRogueClasses`, `rerollCost`. Fragments in
  `spellsource-graphql/graphql/shared/fragments/rogueRun.graphql`.
- Card pool: `format_rogue.json`
  (`spellsource-cards-git/src/main/resources/cards/formats/`). Selection is
  weighted (class match ×2, non-minion ×2; equipment rarity Legendary ×8 /
  Epic ×4 / Rare ×2; no Legendary/Epic duplicates vs. the current deck).
- Unity client UI: `spellsource-client/src/unity/Assets/Scripts/UI/RogueRunController.cs`
  and the `Rogue*` siblings in that directory.
- Web smoke page: `spellsource-web/src/pages/rogue-test.tsx`.

#### Local verification surfaces, by fidelity

1. **Integration tests** (proven above). `RogueRunTests` extends
   `FrameworkTestBase`, whose `StandaloneApplication` self-provisions
   Postgres, Keycloak, Redis, and the GraphQL gateway as Testcontainers and
   runs migrations — no manual setup. Useful patterns from the existing tests:
   - Full match: start run → `RogueManager.makeAnyChoices(runId)` → enqueue in
     the `rogueRun` queue with `deckId` + `botDeckId` →
     `client.playUntilGameOver()` → assert run state.
   - Force a win: empty the opponent deck with
     `Routines.setCardsInDeck(opponentDeck, new String[0])` before the match.
   - Determinism: runs are seeded (`XORShiftRandom`; `seed` / `seed_state`
     columns), so choice contents are reproducible — tests use seed 0 and can
     assert exact offerings.
2. **Live local stack**: run
   `spellsource-server/src/test/java/com/hiddenswitch/framework/tests/applications/EntryPoint.java`
   to deploy the same Testcontainers-backed stack as a long-running local
   server, then drive the GraphQL mutations interactively or connect a client.
3. **Web page**: `yarn dev` in `spellsource-web`, visit `/rogue-test`.
   Currently only fires `startRogueRun(heroClass: "TEST", seed: 0)` and logs
   the result to the console — a smoke test of the GraphQL path, not a full
   run UI.

#### Coverage gaps (candidates for new tests)

Not currently exercised by any test: `reroll` (and reroll cost growth),
`upgradeCard` / `upgradeCardCost`, `skipBoss`, the 8-boss victory
termination, and lives decrementing on a loss with lives > 1.
