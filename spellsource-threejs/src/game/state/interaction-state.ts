import type { Entity, GameActions, SpellAction } from '../../__generated__/client'
import { ActionType } from '../../__generated__/client'
import {
  findActionsForEntity,
  findUntargetedActions,
  findTargetedAction,
  findChooseOneAction,
  getValidTargets,
  getPlayableEntityIds,
  getSummonSlots,
  type SummonSlot,
} from './action-resolver'

export type InteractionPhase =
  | 'idle'
  | 'awaiting_target'
  | 'awaiting_summon_position'
  | 'awaiting_choice'

export interface InteractionState {
  phase: InteractionPhase
  /** The entity the player clicked on to begin an action */
  selectedSource: Entity | null
  /** Valid target entity IDs when awaiting_target */
  validTargets: number[]
  /** Entity IDs that have playable actions this turn */
  playableEntityIds: number[]
  /** Summon position slots when awaiting_summon_position */
  summonSlots: SummonSlot[]
  /** Choice options when awaiting_choice */
  pendingChoices: SpellAction[]
}

export type InteractionAction =
  | { type: 'reset' }
  | { type: 'update_playable'; actions: GameActions | undefined }
  | { type: 'select_source'; entity: Entity; actions: GameActions }
  | { type: 'select_target'; entity: Entity; actions: GameActions }
  | { type: 'select_summon_slot'; slot: SummonSlot }
  | { type: 'select_choice'; choice: SpellAction; actions: GameActions }
  | { type: 'cancel' }

export function createInitialInteraction(): InteractionState {
  return {
    phase: 'idle',
    selectedSource: null,
    validTargets: [],
    playableEntityIds: [],
    summonSlots: [],
    pendingChoices: [],
  }
}

export interface InteractionResult {
  state: InteractionState
  /** If set, send this action index to the server */
  actionToSend?: number
}

export function reduceInteraction(
  current: InteractionState,
  action: InteractionAction
): InteractionResult {
  switch (action.type) {
    case 'reset':
      return {
        state: {
          ...createInitialInteraction(),
          playableEntityIds: current.playableEntityIds,
        },
      }

    case 'update_playable':
      return {
        state: {
          ...createInitialInteraction(),
          playableEntityIds: action.actions
            ? getPlayableEntityIds(action.actions)
            : [],
        },
      }

    case 'cancel':
      return {
        state: {
          ...current,
          phase: 'idle',
          selectedSource: null,
          validTargets: [],
          summonSlots: [],
          pendingChoices: [],
        },
      }

    case 'select_source': {
      const { entity, actions } = action
      const entityActions = findActionsForEntity(actions, entity.id)

      if (entityActions.length === 0) {
        return { state: current }
      }

      // 1. Check for choose-one cards
      const chooseOne = findChooseOneAction(actions, entity.id)
      if (chooseOne) {
        return {
          state: {
            ...current,
            phase: 'awaiting_choice',
            selectedSource: entity,
            pendingChoices: chooseOne.choices,
            validTargets: [],
            summonSlots: [],
          },
        }
      }

      // 2. Check for summon actions (minion placement)
      const summonSlots = getSummonSlots(actions, entity.id)
      if (summonSlots.length > 0) {
        // If only one position, send immediately (no choice needed)
        if (summonSlots.length === 1) {
          return {
            state: createInitialInteraction(),
            actionToSend: summonSlots[0].actionIndex,
          }
        }
        return {
          state: {
            ...current,
            phase: 'awaiting_summon_position',
            selectedSource: entity,
            summonSlots,
            validTargets: [],
            pendingChoices: [],
          },
        }
      }

      // 3. Check for untargeted actions (untargeted spells, hero power, equip weapon, etc.)
      const untargeted = findUntargetedActions(actions, entity.id)
      if (untargeted.length > 0) {
        return {
          state: createInitialInteraction(),
          actionToSend: untargeted[0].action,
        }
      }

      // 4. Check for targeted actions (targeted spells, physical attacks, targeted hero power)
      const targets = getValidTargets(actions, entity.id)
      if (targets.length > 0) {
        return {
          state: {
            ...current,
            phase: 'awaiting_target',
            selectedSource: entity,
            validTargets: targets,
            summonSlots: [],
            pendingChoices: [],
          },
        }
      }

      return { state: current }
    }

    case 'select_target': {
      const { entity: target, actions } = action
      if (!current.selectedSource) return { state: current }

      const actionIndex = findTargetedAction(
        actions,
        current.selectedSource.id,
        target.id
      )

      if (actionIndex !== undefined) {
        return {
          state: createInitialInteraction(),
          actionToSend: actionIndex,
        }
      }

      // If clicked entity isn't a valid target but IS a playable source, re-select
      if (current.playableEntityIds.includes(target.id)) {
        return reduceInteraction(
          { ...current, phase: 'idle', selectedSource: null, validTargets: [], summonSlots: [], pendingChoices: [] },
          { type: 'select_source', entity: target, actions }
        )
      }

      return { state: current }
    }

    case 'select_summon_slot': {
      return {
        state: createInitialInteraction(),
        actionToSend: action.slot.actionIndex,
      }
    }

    case 'select_choice': {
      const { choice, actions } = action

      // If choice has no targets, send immediately
      if (!choice.targetKeyToActions || choice.targetKeyToActions.length === 0) {
        return {
          state: createInitialInteraction(),
          actionToSend: choice.action,
        }
      }

      // Choice requires targeting — enter awaiting_target using the choice's targets
      const targets: number[] = []
      for (const pair of choice.targetKeyToActions) {
        if (pair.target !== -1) {
          targets.push(pair.target)
        }
      }

      if (targets.length > 0) {
        return {
          state: {
            ...current,
            phase: 'awaiting_target',
            // Keep selectedSource but update targets to the choice's targets
            validTargets: targets,
            summonSlots: [],
            pendingChoices: [],
          },
        }
      }

      // No valid targets for this choice — send it anyway
      return {
        state: createInitialInteraction(),
        actionToSend: choice.action,
      }
    }

    default:
      return { state: current }
  }
}
