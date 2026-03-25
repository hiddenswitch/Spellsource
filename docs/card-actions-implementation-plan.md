# Card Actions Implementation Plan

**Status: Phase A, B, C implemented (2026-03-24)**

## Bugs Found and Fixed

The original FSM had several critical bugs preventing most actions from working:

1. **SUMMON completely broken** — SUMMON actions always have `targetKeyToActions` (board position targets), so `findUntargetedActions` never matched. `getValidTargets` filtered out `target === -1`, so on empty board no action could be sent. Minions could never be played.

2. **Choose-one completely broken** — `choices` array was never checked. FSM would blindly send the parent action.

3. **Stale closure on `interaction.phase`** — `onEntityClicked` captured `interaction.phase` from React render closure. After FSM transitioned from `idle` → `awaiting_target`, subsequent clicks could still see `idle` phase due to batched state updates.

4. **Re-click in targeting mode stuck** — Clicking a different playable source while in `awaiting_target` did nothing. Had to Escape first.

5. **`findUntargetedActions` too broad** — Matched any action with empty `targetKeyToActions` including SUMMON on empty board edge cases and choose-one parents.

All five bugs are now fixed. The FSM has four phases: `idle`, `awaiting_target`, `awaiting_summon_position`, `awaiting_choice`.

---

## What Works Now

### 1. Summon Board Position Selection (HIGH PRIORITY)

**Problem**: When playing a minion to the battlefield, the server provides SUMMON actions with `targetKeyToActions` entries encoding valid board positions:
- `target: -1` means "rightmost position" (after all existing minions)
- `target: <minionId>` means "insert to the left of this minion"
- `friendlyBattlefieldIndex` on each pair gives the resulting index

The current `getValidTargets()` in `action-resolver.ts` filters out `target === -1`, so the "rightmost" position is invisible. When there are existing minions, clicking a friendly minion to mean "insert left of me" is confusing because the same minion might also be a valid attack source.

**What needs to happen**:
- Detect when the selected source is a SUMMON action
- Instead of highlighting target entities, render **insertion slot markers** between existing battlefield minions (and one at the rightmost position)
- Each slot corresponds to a `TargetActionPair` from the SUMMON action
- Clicking a slot sends the corresponding `pair.action` index
- The slot positions can be computed from `battlefieldPosition()` — insert between adjacent minion positions

**New FSM phase**: `awaiting_summon_position`

**New renderer component**: `SummonSlots` — renders clickable slot indicators between minions on the bottom battlefield. Each slot is a semi-transparent marker at the midpoint between two adjacent minion positions (or at the edges).

**Files to modify**:
- `interaction-state.ts` — add `awaiting_summon_position` phase, store summon slot data
- `action-resolver.ts` — add `getSummonSlots()` helper that returns `{ position: Vec3, actionIndex: number }[]`
- `game-scene.tsx` — render SummonSlots when in summon positioning phase
- `use-game-context.tsx` — handle slot click callback
- New: `renderer/summon-slots.tsx`

### 2. Choose-One Cards (MEDIUM PRIORITY)

**Problem**: Choose-one cards (e.g. Wrath: "Deal 3 damage or Deal 1 damage and draw a card") have a `choices` array on their SpellAction. The current FSM ignores `choices` entirely — it just sends the first untargeted action, which may not be the intended choice.

**How it works server-side**:
- The parent SpellAction has `sourceId` = the card entity ID
- `choices` is an array of SpellAction objects, one per choice
- Each choice may itself have `targetKeyToActions` (if the chosen effect needs a target)
- The client should show the choice options, let the player pick one, then either send immediately (untargeted choice) or enter targeting mode (targeted choice)

**What needs to happen**:
- In the FSM, when selecting a source that has actions with non-empty `choices`, transition to a new `awaiting_choice` phase instead of sending immediately
- Show a DOM overlay (similar to Discover) with the choice options
- When player picks a choice:
  - If the chosen SpellAction has no targets → send its `action` index
  - If it has targets → transition to `awaiting_target` using that choice's target list

**New FSM phase**: `awaiting_choice`

**New state fields**: `pendingChoices: SpellAction[]` (the choice options)

**Files to modify**:
- `interaction-state.ts` — add choice detection and phase
- `action-resolver.ts` — add `findChooseOneActions()` helper
- `use-game-context.tsx` — add `onChoicePicked` callback
- New or modify: `renderer/overlays/choose-one-overlay.tsx` (can reuse discover overlay pattern)

### 3. Target Type Distinction (LOW PRIORITY, UX)

**Problem**: When in `awaiting_target` phase, all valid targets glow the same yellow. There is no visual distinction between "attack this enemy" vs "heal this friendly" vs "cast spell on this". Also, the source entity type is not communicated — a minion attacking vs a spell being cast look the same.

**Possible improvements**:
- Color-code target glow by action type (red for damage/attack, green for buff/heal, yellow for neutral)
- Show a targeting line/arrow from source to cursor
- Change cursor icon based on action type
- This is polish and can be done after the core mechanics work

### 4. Summon Minion Shifting Animation (LOW PRIORITY)

**Problem**: When a minion is summoned between existing minions, the existing minions should visually shift apart to make room. Currently spring animations handle position changes, so this may work automatically once the server sends updated board positions. Verify after implementing summon slots.

### 5. Re-selecting Source While in Targeting Mode (LOW PRIORITY, UX)

**Problem**: If a player clicks a playable card while in `awaiting_target`, the current FSM tries to resolve it as a target click. If it's not a valid target, nothing happens. The player must press Escape first then re-click.

**Fix**: In `awaiting_target`, if the clicked entity is NOT a valid target but IS a playable source, cancel current targeting and start new source selection. This is a one-line check in the FSM's `select_target` case.

---

## Implementation Order

```
Phase A: Summon positioning (core gameplay, required for playing minions properly)
  1. Add getSummonSlots() to action-resolver
  2. Add awaiting_summon_position phase to interaction FSM
  3. Create SummonSlots renderer component
  4. Wire into game-scene and game-context
  5. Handle edge case: empty board (single slot, or auto-send if only one position)

Phase B: Choose-one cards (important for many cards)
  1. Add choice detection to action-resolver
  2. Add awaiting_choice phase to interaction FSM
  3. Create choose-one overlay component
  4. Wire choice selection through game-context

Phase C: Re-select source in targeting mode (quick UX win)
  1. Modify select_target in FSM to check if clicked entity is a different playable source

Phase D: Visual polish
  1. Target glow color by action type
  2. Targeting line/arrow from source to cursor
  3. Cursor changes
```

---

## Detailed Technical Design

### A. Summon Slot System

#### action-resolver.ts additions

```typescript
interface SummonSlot {
  /** The action index to send when this slot is clicked */
  actionIndex: number
  /** The battlefield index where the minion will appear */
  battlefieldIndex: number
  /** The entity ID of the minion this slot is to the LEFT of, or -1 for rightmost */
  targetId: number
}

function getSummonSlots(actions: GameActions, sourceEntityId: number): SummonSlot[]
```

Logic: find all SpellActions for sourceId with `actionType === SUMMON`, collect their `targetKeyToActions` entries, return as SummonSlot array.

#### interaction-state.ts changes

New phase: `'awaiting_summon_position'`

New state field: `summonSlots: SummonSlot[]`

In `select_source`, when the source entity has SUMMON actions:
- If only one summon position exists → send immediately (no slot selection needed)
- If multiple positions → transition to `awaiting_summon_position` with slots

New action type: `{ type: 'select_summon_slot'; slotIndex: number }`

#### summon-slots.tsx (new renderer component)

Renders clickable 3D markers between battlefield minions on the bottom side:
- Compute positions: for N minions, there are N+1 possible slots (before first, between each pair, after last)
- Each slot is a thin translucent box or plane at the midpoint
- Glow on hover, click to send action
- Only visible when `interaction.phase === 'awaiting_summon_position'`

Position calculation: given existing minion positions from `battlefieldPosition()`, slot i goes at `(pos[i-1] + pos[i]) / 2`, or at the edges for first/last slots.

### B. Choose-One System

#### action-resolver.ts additions

```typescript
function findChooseOneActions(actions: GameActions, sourceEntityId: number): SpellAction | undefined
```

Returns the SpellAction whose `choices.length > 0` for the given source, or undefined.

#### interaction-state.ts changes

New phase: `'awaiting_choice'`

New state field: `pendingChoices: SpellAction[]`

In `select_source`, when actions for entity have non-empty `choices`:
- Transition to `awaiting_choice` with the choices array

New action type: `{ type: 'select_choice'; choice: SpellAction }`
- If choice has no targets → send choice.action immediately
- If choice has targets → transition to `awaiting_target` with the choice's targets

#### choose-one-overlay.tsx

DOM overlay similar to discover. Shows each choice with:
- Name and description (from `choice.entity` or `choice.description`)
- Click to select
- Escape to cancel

---

## Server-Side Notes

The server already handles all action types correctly. The `action` index in each SpellAction/TargetActionPair maps directly to the `GameAction` list on the server. The client just needs to send the right index via `sendGameAction(actionIndex, repliesTo)`.

Key server behaviors to be aware of:
- **SUMMON then BATTLECRY**: After a summon resolves, if the minion has a targeted battlecry, the server sends a NEW `ON_REQUEST_ACTION` with `BATTLECRY`-type actions. The client handles this naturally because the FSM resets on each new action request.
- **Choose-one with targeting**: If a choose-one choice requires a target, the server may send the targeting as a follow-up ON_REQUEST_ACTION (depending on implementation), OR the choice's SpellAction itself has targetKeyToActions. Need to verify which pattern the server uses.
- **Discover sequencing**: Discover is already handled. After picking, the server may send additional action requests (e.g., if discovered card has battlecry).

---

## Files Summary

### New files
- `src/game/renderer/summon-slots.tsx` — Battlefield insertion slot markers
- `src/game/renderer/overlays/choose-one-overlay.tsx` — Choice selection DOM overlay

### Modified files
- `src/game/state/action-resolver.ts` — Add `getSummonSlots()`, `findChooseOneActions()`
- `src/game/state/interaction-state.ts` — Add phases, state fields, reducers for summon positioning and choose-one
- `src/game/hooks/use-game-context.tsx` — Add slot click and choice pick callbacks
- `src/game/renderer/game-scene.tsx` — Render SummonSlots component
- `src/pages/game.tsx` — Render choose-one overlay
- `src/game/types.ts` — Export new types if needed
- `src/game/index.ts` — Export new modules

### Unchanged
- `src/game/hooks/use-game-connection.ts` — No changes needed
- `src/game/hooks/use-animation-queue.ts` — No changes needed
- `src/game/renderer/card-mesh.tsx` — Glow logic already works for all action types
- `src/game/renderer/hero-mesh.tsx` — Already handles hero/hero power interaction
- Server-side Java code — Already encodes all action types correctly
