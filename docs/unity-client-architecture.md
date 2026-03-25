# Unity Game Client Architecture

Reference document for rebuilding the Spellsource game client in Three.js/React. Describes the architecture, rendering strategy, and game flow of the Unity client in `spellsource-client/src/unity/Assets/Scripts/`.

## High-Level Architecture

The Unity client uses a **reactive pipeline** built on UniRx (Reactive Extensions for Unity):

```
Server (gRPC stream)
  → DefaultClient (network singleton)
  → GameController (central orchestrator)
    → MessageHandler[] (animation delay pipeline)
    → Reactive properties (entities, gameState, progress, actions)
      → Views subscribe & render
```

Player input flows in reverse:

```
Pointer events → CommandController → GameController → DefaultClient → Server
```

The key architectural insight: **messages from the server arrive instantly, but the client needs to animate them sequentially**. The `MessageHandler` pipeline inserts coroutine-based delays between message processing and reactive property updates, so views animate in order.

---

## Core Components

### DefaultClient (`HiddenSwitch/DefaultClient.cs`)

Singleton managing the gRPC connection. Provides:
- `GameMessages()` — Observable stream of `ServerToClientMessage` (raw, no animation delay)
- `SendGameMessage()` — Send `ClientToServerMessage` to server
- Token-based JWT auth, stored to disk for session persistence
- Automatic reconnection logic

### GameController (`Battlefield/GameController.cs`)

Central orchestrator. The most important class. Singleton accessed via `GameController.instance`.

**Reactive properties** (delayed by animations):
| Property | Type | Description |
|---|---|---|
| `entities` | `ReactiveDictionary<int, Entity>` | All game entities by ID |
| `gameState` | `ReactiveProperty<GameState>` | Latest game state |
| `progress` | `ReactiveProperty<GameProgress>` | Phase/turn state machine |
| `request` | `ReactiveProperty<ServerToClientMessage>` | Latest action/mulligan request |
| `actions` | `ReactiveDictionary<int, SpellAction>` | Available actions by source entity ID |
| `gameOver` | `ReactiveProperty<GameOver>` | Win/loss result (NOT delayed) |
| `timeLeft` | `Observable<long?>` | Turn timer countdown |
| `powerHistory` | `ReactiveCollection<GameEvent>` | Event log for history panel |

**Key detail**: `actions` is intentionally NOT delayed by animations. This lets the player queue actions faster than animations resolve. The action dictionary is keyed by source entity ID, mapping to the `SpellAction` for that entity.

**Message processing pipeline**:
1. `DefaultClient.GameMessages()` emits raw messages
2. Filter to relevant types (ON_UPDATE, ON_GAME_EVENT, ON_REQUEST_ACTION, ON_MULLIGAN, ON_GAME_END)
3. Each message passes through `MessageHandler[]` components (attached to same GameObject)
4. Handlers yield coroutine delays for animations
5. Delayed message emits to `m_Messages` subject
6. Reactive properties update, views react

**Entity zone management**: When a message arrives, the controller:
1. Updates the `entities` dictionary with all entities from `GameState.Entities`
2. Groups changed entity IDs by `LocalZoneKey` (zone type + is-local-player)
3. Runs `DiffSequence` against each zone's current entity list to produce add/move/remove operations
4. Zones animate the transitions

---

## Animation Delay Pipeline

### MessageHandler (`Battlefield/MessageHandler.cs`)

Abstract base class. Each subclass processes a specific event type and returns a coroutine delay.

```csharp
abstract IEnumerator HandleMessage(ServerToClientMessage message);
```

The GameController runs all handlers for each message via `ProcessMessage()`, concatenating their delays. This serializes animations so they play in order.

### Handler Catalog

| Handler | Trigger | Animation |
|---|---|---|
| `DrawCardHandler` | DRAW_CARD event | Deck draw animation trigger, ~0.3s delay |
| `PhysicalAttackHandler` | PHYSICAL_ATTACK event | DOTween punch attacker toward defender, shake defender. ~0.5s |
| `DamageHandler` | DAMAGE event | Floating damage number (pooled `ValueEventView`). ~0.15s |
| `HealingHandler` | HEAL event | Floating heal number. ~0.15s |
| `KillHandler` | KILL event | Death particle effect at entity position. ~0.2s |
| `AftermathHandler` | KILL event with deathrattle | Extra delay for aftermath resolution. ~0.25s |
| `MissileFiredHandler` | MISSILE_FIRED event | Parabolic projectile from source to target with hit particles. ~0.4s |
| `BoardChangeAndSummonHandler` | SUMMON event | Summon particle effect. ~0.25s |
| `OpponentPlayedCardHandler` | PLAY_CARD event (opponent) | Briefly shows opponent's card in center. ~1.5s |
| `CardsReceivedHandler` | Card added to hand | Layout update delay. ~0.15s |
| `DefaultDelayHandler` | Any opponent message | Generic delay for opponent events. ~0.15s |

### Object Pooling

Expensive visual effects use `ObjectPool<T>`:
- `OpponentPlayedCardHandler` pools card display objects
- `ValueEventHandler` pools floating damage/heal numbers
- `MissileFiredHandler` pools projectile objects

Pools preload configurable counts and rent/return instances.

---

## Entity Rendering

### EntityModelView (`Battlefield/EntityModelView.cs`)

The primary visual component for any game entity. Uses Unity's serialized field binding pattern where each visual element is a configurable slot:

**Visual slots**:
- **Images**: Primary/Secondary/Highlight/Shadow fills (colored per hero class), portrait sprite, sprite shadow
- **Text values**: Attack, Health, Armor, ManaCost, Mana, MaxMana, Name, Description, Tribe, Fires (quest counter), Spellpower
- **Card type indicators**: Toggle visibility for Minion/Spell/Weapon/Champion/Skill frames
- **Status icons**: Guard (taunt), Dodge (divine shield), Deflect, Aftermath (deathrattle), Hidden (stealth), Dash (rush), Blitz (charge), Double Strike (windfury), Toxic (poisonous), Lifedrain (lifesteal), Exhausted (summoning sickness), Stunned (frozen), Silenced, Wounded (enraged), Trigger host
- **Rarity badges**: Uncollectible, Free, Common, Rare, Epic, Legendary, Alliance
- **Outlines**: Attack/Health/Cost outlines colored green (buffed) or red (debuffed) based on comparing current vs base values
- **Counters**: Charge counter display

**Data flow**: The `data` reactive property holds the current `Entity`. When it changes, `SetDirty()` runs and updates all visual slots in a single pass. The entity's `Art` field provides hero-class-specific coloring.

**Sprite resolution**: If the entity has no sprite, falls back to `MissingAssetsProfile` arrays (per entity type) using a hash-based index for deterministic assignment.

**Multiple prefab variants exist per context**:
- `Small Card.prefab` — default compact view
- `Small Card - Hand.prefab` — in-hand variant
- `Small Card - Inspector.prefab` — hover tooltip variant
- `Large Card.prefab` — expanded detail view
- `Champion.prefab` — hero portrait
- `Minion.prefab` / `Minion - Local.prefab` — battlefield unit
- `Secret.prefab`, `Quest.prefab` — special zone displays

### HasEntity (`Battlefield/HasEntity.cs`)

Base class for any MonoBehaviour that holds an `Entity` reference. Provides the reactive `data` property. `EntityModelView` extends this.

---

## Zone System

### BaseZone

Abstract base for entity containers (hand, battlefield, hero, deck, etc.). Implements `IEntityZone` which provides:
- `entities` — current ordered list of entities in this zone
- `Key` — `LocalZoneKey` (zone type + is-local-player flag)
- Add/Move/Remove operations that trigger layout updates

### LocalZoneKey

Identifies zones: combines `ZonesMessage.Zones` enum with a boolean `isLocal` flag. Two heroes exist (local + opponent), two hands, two battlefields, etc.

### ZonesLookup

Collects all zone instances in the hierarchy via a lookup controller pattern. The GameController iterates all zones to dispatch entity changes.

### DiffSequence (`Battlefield/DiffSequence.cs`)

LCS-based diff algorithm that compares old and new entity lists for a zone and emits:
- **Add**: Entity appeared in zone (play from hand, summon, draw)
- **Move**: Entity repositioned within zone (board rearrangement)
- **Remove**: Entity left zone (died, returned to hand, played)

This drives the animated layout transitions.

### AnimatedFlowLayoutController (`Battlefield/AnimatedFlowLayoutController.cs`)

Custom layout system that replaces Unity's built-in LayoutGroup. Arranges children along configurable U/V axes with:
- DOTween-animated position transitions when entities move
- Overflow culling (excess cards in hand are hidden)
- Support for both RectTransform (2D UI) and Transform (3D) children
- Configurable spacing, padding, alignment

When entities are added/moved/removed, the layout smoothly animates all children to their new positions.

---

## Player Input

### CommandController (`Battlefield/CommandController.cs`)

Handles all player interaction with entities. State machine:

```
Unactivated → (click/drag) → Activated → (valid target) → WillIssue → (release) → Issued
                                  ↓
                             (cancel/invalid) → Unactivated
```

**Activation logic**:
- Minimum hold time (0.18s) prevents accidental clicks
- Drag distance threshold determines targeting vs clicking
- `LineController` draws a targeting line from source to cursor during drag
- Cancel zone (dragging back to source area) deactivates

**Enable conditions**: `CommandControllerEnableCondition` subclasses gate whether an entity can be interacted with:
- `PlayableCondition` — entity has available actions in `GameController.actions`
- `CanMulliganCondition` — mulligan phase is active

### ActionResolver (action lookup)

When a command is issued, the controller needs to find the right action index:
- **Untargeted plays**: Find actions for the source entity with no targets (e.g., play a spell without targets)
- **Targeted plays**: Find actions for the source entity that target a specific entity
- **End turn**: Find the END_TURN action type

The Unity client stores actions as `ReactiveDictionary<int, SpellAction>` keyed by source entity ID. Each `SpellAction` contains:
- `action` — the action index to send to server
- `actionType` — END_TURN, PHYSICAL_ATTACK, SPELL, SUMMON, etc.
- `sourceId` — which entity is acting
- `targetKeyToActions` — list of (target entity ID → action index) pairs
- `choices` — nested sub-actions for discover/choose-one

---

## Mulligan

### MulliganController (`Battlefield/MulliganController.cs`)

Simple controller that:
1. Observes `GameController.progress` for mulligan states
2. Displays starting cards in the hand zone
3. Tracks which cards the player toggles for discard
4. On confirm, calls `GameController.EndWithMulligan(discardedIndices)`

Auto-mulligan option exists (controlled by `m_AutoMulligan` flag on GameController) that immediately accepts the starting hand.

---

## Game Progress State Machine

### GameProgress (`Battlefield/GameProgress.cs`)

Derives the current game phase from message data:

| State | Condition |
|---|---|
| `Connecting` | No messages received yet |
| `InProgressBothMulligan` | ON_MULLIGAN message received, both players mulliganing |
| `InProgressLocalMulligan` | Local player still mulliganing |
| `InProgressOpposingMulligan` | Opponent still mulliganing |
| `LocalTurn` | Game state says it's local player's turn |
| `OpposingTurn` | Game state says it's opponent's turn |
| `GameOverLocalWin` | GameOver received, local player won |
| `GameOverOpposingWin` | GameOver received, local player lost |
| `Replaying` | Replay mode |

Also tracks an `Actions` sub-state:
- `Uninterruptible` — special actions required (discover, battlecry targeting)
- `Interruptible` — normal turn, can end at any time

---

## Visual Hierarchy (Scene Structure)

The main `Game.unity` scene uses a Canvas-based 2D UI layout:

```
Root Canvas (1920x1080)
├── Opponent Area
│   ├── Opponent Hero (Champion.prefab)
│   ├── Opponent Hero Power
│   ├── Opponent Weapon
│   ├── Opponent Hand (AnimatedFlowLayout, cards face-down)
│   ├── Opponent Battlefield (AnimatedFlowLayout, Minion.prefab instances)
│   ├── Opponent Secrets/Quests
│   └── Opponent Deck Pile
├── Player Area
│   ├── Player Hero (Champion.prefab)
│   ├── Player Hero Power
│   ├── Player Weapon
│   ├── Player Hand (AnimatedFlowLayout, Small Card - Hand.prefab instances)
│   ├── Player Battlefield (AnimatedFlowLayout, Minion - Local.prefab instances)
│   ├── Player Secrets/Quests
│   └── Player Deck Pile
├── Mana Tray (Mana Available/Used/Locked prefabs)
├── End Turn Button
├── Power History Panel (scrollable event log)
├── Timer Display
├── Card Inspector (hover tooltip, Large Card.prefab)
├── Opponent Played Card Display (center of screen, pooled)
└── Effect Overlays (damage numbers, particles, missiles)
```

---

## Effects System

### Particle/Visual Effects

Effects are prefab-based, instantiated at entity positions:
- `Damage.prefab` — damage impact
- `Death.prefab` — kill particles
- `Healing.prefab` — healing glow
- `Summoning.prefab` — summon flash
- `Physical Attack.prefab` — attack impact
- `Movement.prefab` — movement trail
- `Trigger Fired.prefab` — trigger activation

### Missile System

Spell projectiles (`MissileView.cs`):
- Parabolic arc from source to target
- Cast effect at source
- Hit effect at target
- Configurable prefab per spell (e.g., `Blue Fireball Missile.prefab`)

### Status Overlays

Persistent visual indicators on entities:
- `Guard.prefab` — taunt shield overlay
- `Trigger.prefab` — trigger host indicator
- `Discarded.prefab` — discard marker
- `General.prefab` — generic status overlay

---

## Hover & Inspection

### Card Inspection

When hovering over an entity:
1. `HoverController` detects pointer enter with debounce
2. `InspectOnHoverController` activates inspection
3. `InspectableCardView` renders a full-detail card near the cursor
4. Position adapts to screen edges (left/right/top)
5. `FlyInView` animates the card sliding in
6. Deactivates on pointer exit or when command activation begins

### Hover Zone Calculation

The inspection card appears in a zone that doesn't overlap the source:
- Top, Left, or OppositeFinger placement
- Configurable padding from screen edges
- Different strategies for mobile (finger occlusion) vs desktop

---

## Key Patterns for Three.js Port

### 1. Animation-Delayed Message Processing

The most important pattern to replicate. Messages must be processed sequentially with delays:
```
message arrives → compute animation duration → wait → update state → render
```

In React/Three.js, this could be:
- A message queue with `async/await` processing
- `useReducer` for state, with an animation queue that gates when the reducer runs
- Or a simpler approach: process messages immediately into state, but queue animations separately

### 2. Reactive Entity Dictionary

The `ReactiveDictionary<int, Entity>` pattern is central. Every view subscribes to entity changes by ID. In React, this maps to:
- A `Map<number, Entity>` in state
- Components select their entity by ID and re-render when it changes

### 3. Zone-Based Layout with Diff

Entities are grouped by zone. When entities move between zones, a diff algorithm determines the minimal set of add/move/remove operations. In Three.js:
- Group entities by zone in state
- Use React keys for entity components
- Animate position transitions with `@react-spring` or manual lerping

### 4. Action Lookup by Source Entity

Actions are stored as a map from source entity ID to SpellAction. When a player clicks an entity:
1. Look up actions for that entity
2. If untargeted actions exist, play them immediately
3. If targeted actions exist, enter targeting mode (show valid targets)
4. If no actions, the entity is not playable

### 5. Multiple Entity Visual Variants

Different prefabs render the same entity data depending on context (hand card vs battlefield minion vs hero). In React:
- Different components for hand cards, battlefield minions, heroes
- All receive the same `Entity` data
- Visual differences are purely component-level

### 6. Stat Coloring (Buff/Debuff Outlines)

Stats are colored based on comparison with base values:
- `attack > baseAttack` → green (buffed)
- `hp < maxHp` → red (damaged)
- `manaCost < baseManaCost` → green (cost reduced)
- `manaCost > baseManaCost` → red (cost increased)
