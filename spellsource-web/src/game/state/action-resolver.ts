import type { GameActions, SpellAction, TargetActionPair } from '../../__generated__/client'
import { ActionType } from '../../__generated__/client'

/**
 * Find the END_TURN action index from the current available actions.
 */
export function findEndTurnAction(actions: GameActions): number | undefined {
  for (const sa of actions.all) {
    if (sa.actionType === ActionType.EndTurn) {
      return sa.action
    }
  }
  return undefined
}

/**
 * Find all actions that originate from a given entity (by entity ID).
 */
export function findActionsForEntity(actions: GameActions, entityId: number): SpellAction[] {
  return actions.all.filter((sa) => sa.sourceId === entityId)
}

/**
 * Find the action index for a targeted action (source attacks/targets a specific entity).
 */
export function findTargetedAction(
  actions: GameActions,
  sourceEntityId: number,
  targetEntityId: number
): number | undefined {
  for (const sa of actions.all) {
    if (sa.sourceId !== sourceEntityId) continue
    if (!sa.targetKeyToActions) continue
    for (const pair of sa.targetKeyToActions) {
      if (pair.target === targetEntityId) {
        return pair.action
      }
    }
  }
  return undefined
}

/**
 * Find untargeted actions for an entity — actions with no targetKeyToActions
 * AND no choices. Excludes SUMMON (which always has position targets).
 */
export function findUntargetedActions(actions: GameActions, sourceEntityId: number): SpellAction[] {
  return actions.all.filter(
    (sa) =>
      sa.sourceId === sourceEntityId &&
      (!sa.targetKeyToActions || sa.targetKeyToActions.length === 0) &&
      (!sa.choices || sa.choices.length === 0) &&
      sa.actionType !== ActionType.Summon
  )
}

/**
 * Get all valid target entity IDs for actions from a given source.
 * Excludes SUMMON position targets (target === -1 and friendly minion IDs used for positioning).
 */
export function getValidTargets(actions: GameActions, sourceEntityId: number): number[] {
  const targets = new Set<number>()
  for (const sa of actions.all) {
    if (sa.sourceId !== sourceEntityId) continue
    // Skip summon actions — their targets are board positions, not spell/attack targets
    if (sa.actionType === ActionType.Summon) continue
    if (!sa.targetKeyToActions) continue
    for (const pair of sa.targetKeyToActions) {
      if (pair.target !== -1) {
        targets.add(pair.target)
      }
    }
  }
  return [...targets]
}

/**
 * Get all entity IDs that are sources of playable actions.
 */
export function getPlayableEntityIds(actions: GameActions): number[] {
  const ids = new Set<number>()
  for (const sa of actions.all) {
    if (sa.actionType !== ActionType.EndTurn) {
      ids.add(sa.sourceId)
    }
  }
  return [...ids]
}

// ── Summon positioning ──────────────────────────────────────

export interface SummonSlot {
  /** The action index to send when this slot is clicked */
  actionIndex: number
  /** The battlefield index where the minion will appear */
  battlefieldIndex: number
  /** The entity ID of the minion this slot is to the LEFT of, or -1 for rightmost */
  targetId: number
}

/**
 * Get the summon position slots for a SUMMON action from a given source.
 * Each slot represents a valid board position for the new minion.
 */
export function getSummonSlots(actions: GameActions, sourceEntityId: number): SummonSlot[] {
  const slots: SummonSlot[] = []
  for (const sa of actions.all) {
    if (sa.sourceId !== sourceEntityId) continue
    if (sa.actionType !== ActionType.Summon) continue
    if (!sa.targetKeyToActions) continue
    for (const pair of sa.targetKeyToActions) {
      slots.push({
        actionIndex: pair.action,
        battlefieldIndex: pair.friendlyBattlefieldIndex,
        targetId: pair.target,
      })
    }
  }
  // Sort by battlefield index so they render left-to-right
  slots.sort((a, b) => a.battlefieldIndex - b.battlefieldIndex)
  return slots
}

// ── Choose-one ──────────────────────────────────────────────

/**
 * Find a choose-one action for the given source entity.
 * Returns the SpellAction with non-empty choices, or undefined.
 */
export function findChooseOneAction(actions: GameActions, sourceEntityId: number): SpellAction | undefined {
  for (const sa of actions.all) {
    if (sa.sourceId === sourceEntityId && sa.choices && sa.choices.length > 0) {
      return sa
    }
  }
  return undefined
}
