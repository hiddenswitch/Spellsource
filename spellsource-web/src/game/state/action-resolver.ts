import {
  type GameActions,
  type SpellAction,
  type TargetActionPair,
  type ClientToServerMessage,
  ActionTypeMessage_ActionType,
  MessageTypeMessage_MessageType,
  ClientToServerMessage as ClientToServerMessageCodec,
} from 'spellsource-protos/dist/experimental/client/spellsource'

/**
 * Find the END_TURN action index from the current available actions.
 * Returns undefined if no end turn action is available.
 */
export function findEndTurnAction(actions: GameActions): number | undefined {
  for (const sa of actions.all) {
    if (sa.actionType === ActionTypeMessage_ActionType.END_TURN) {
      return sa.action
    }
  }
  return undefined
}

/**
 * Find all actions that originate from a given entity (by entity ID).
 * Returns the SpellActions whose sourceId matches.
 */
export function findActionsForEntity(actions: GameActions, entityId: number): SpellAction[] {
  return actions.all.filter((sa) => sa.sourceId === entityId)
}

/**
 * Find the action index for a targeted action (source attacks/targets a specific entity).
 * Returns undefined if no such action exists.
 */
export function findTargetedAction(
  actions: GameActions,
  sourceEntityId: number,
  targetEntityId: number
): number | undefined {
  for (const sa of actions.all) {
    if (sa.sourceId !== sourceEntityId) continue
    for (const pair of sa.targetKeyToActions) {
      if (pair.target === targetEntityId) {
        return pair.action
      }
    }
  }
  return undefined
}

/**
 * Find untargeted actions for an entity (actions with no targetKeyToActions).
 * These include DISCOVER, BATTLECRY choices, or plays that don't require a target.
 */
export function findUntargetedActions(actions: GameActions, sourceEntityId: number): SpellAction[] {
  return actions.all.filter(
    (sa) => sa.sourceId === sourceEntityId && sa.targetKeyToActions.length === 0
  )
}

/**
 * Get all valid target entity IDs for actions from a given source.
 */
export function getValidTargets(actions: GameActions, sourceEntityId: number): number[] {
  const targets = new Set<number>()
  for (const sa of actions.all) {
    if (sa.sourceId !== sourceEntityId) continue
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
    if (sa.actionType !== ActionTypeMessage_ActionType.END_TURN) {
      ids.add(sa.sourceId)
    }
  }
  return [...ids]
}

/**
 * Build a ClientToServerMessage for an action response.
 */
export function buildActionMessage(actionIndex: number, repliesTo: string): Partial<ClientToServerMessage> {
  return {
    actionIndex,
    repliesTo,
    messageType: MessageTypeMessage_MessageType.UPDATE_ACTION,
    discardedCardIndices: [],
  }
}

/**
 * Build a ClientToServerMessage for a mulligan response.
 */
export function buildMulliganMessage(discardedCardIndices: number[], repliesTo: string): Partial<ClientToServerMessage> {
  return {
    discardedCardIndices,
    repliesTo,
    messageType: MessageTypeMessage_MessageType.UPDATE_MULLIGAN,
    actionIndex: -1,
  }
}
