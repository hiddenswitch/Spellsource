import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type FunctionComponent,
  type PropsWithChildren,
} from 'react'
import type { Entity, GameActions, SpellAction } from '../../__generated__/client'
import type { ManagedGameState, PlayerSide } from '../types'
import type { ActiveEffect, RevealedCard } from './use-animation-queue'
import type { SummonSlot } from '../state/action-resolver'
import { findEndTurnAction } from '../state/action-resolver'
import {
  createInitialInteraction,
  reduceInteraction,
  type InteractionState,
  type InteractionAction,
} from '../state/interaction-state'

export interface GameContextValue {
  state: ManagedGameState
  interaction: InteractionState
  activeEffects: ActiveEffect[]
  revealedCard: RevealedCard | null
  sendAction: (actionIndex: number) => void
  sendMulligan: (discardedCardIndices: number[]) => void
  concedeGame: () => void
  onEntityClicked: (entity: Entity, side: PlayerSide) => void
  onEndTurnClicked: () => void
  onSummonSlotClicked: (slot: SummonSlot) => void
  onChoicePicked: (choice: SpellAction) => void
  onCancel: () => void
  hoveredEntity: Entity | null
  setHoveredEntity: (entity: Entity | null) => void
}

const GameContext = createContext<GameContextValue | null>(null)

export function useGameContext(): GameContextValue {
  const ctx = useContext(GameContext)
  if (!ctx) throw new Error('useGameContext must be used within a GameContextProvider')
  return ctx
}

interface GameContextProviderProps extends PropsWithChildren {
  state: ManagedGameState
  activeEffects: ActiveEffect[]
  revealedCard: RevealedCard | null
  sendAction: (actionIndex: number) => void
  sendMulligan: (discardedCardIndices: number[]) => void
  concedeGame: () => void
}

export const GameContextProvider: FunctionComponent<GameContextProviderProps> = ({
  state,
  activeEffects,
  revealedCard,
  sendAction,
  sendMulligan,
  concedeGame,
  children,
}) => {
  const [interaction, setInteraction] = useState<InteractionState>(createInitialInteraction)
  const [hoveredEntity, setHoveredEntity] = useState<Entity | null>(null)
  const prevActionsRef = useRef<GameActions | undefined>(undefined)

  // Reset interaction when new actions arrive
  useEffect(() => {
    if (state.actions !== prevActionsRef.current) {
      prevActionsRef.current = state.actions
      const result = reduceInteraction(interaction, {
        type: 'update_playable',
        actions: state.actions,
      })
      setInteraction(result.state)
    }
  }, [state.actions])

  const dispatchInteraction = useCallback(
    (action: InteractionAction) => {
      setInteraction((prev) => {
        const result = reduceInteraction(prev, action)
        if (result.actionToSend !== undefined) {
          sendAction(result.actionToSend)
        }
        return result.state
      })
    },
    [sendAction]
  )

  // Use a ref for actions so callbacks always see latest
  const actionsRef = useRef<GameActions | undefined>(state.actions)
  actionsRef.current = state.actions

  // Entity click handler — reads interaction phase from setInteraction's prev
  // to avoid stale closure on interaction.phase
  const onEntityClicked = useCallback(
    (entity: Entity, _side: PlayerSide) => {
      const actions = actionsRef.current
      if (!actions) return

      setInteraction((prev) => {
        let action: InteractionAction
        if (prev.phase === 'awaiting_target') {
          action = { type: 'select_target', entity, actions }
        } else if (prev.phase === 'awaiting_summon_position' || prev.phase === 'awaiting_choice') {
          // Clicking an entity while in summon/choice mode — cancel and re-select
          if (prev.playableEntityIds.includes(entity.id)) {
            const cancelled = reduceInteraction(prev, { type: 'cancel' })
            const result = reduceInteraction(cancelled.state, { type: 'select_source', entity, actions })
            if (result.actionToSend !== undefined) {
              sendAction(result.actionToSend)
            }
            return result.state
          }
          return prev
        } else {
          action = { type: 'select_source', entity, actions }
        }

        const result = reduceInteraction(prev, action)
        if (result.actionToSend !== undefined) {
          sendAction(result.actionToSend)
        }
        return result.state
      })
    },
    [sendAction]
  )

  const onEndTurnClicked = useCallback(() => {
    const actions = actionsRef.current
    if (!actions) return
    const actionIndex = findEndTurnAction(actions)
    if (actionIndex !== undefined) {
      sendAction(actionIndex)
    }
  }, [sendAction])

  const onSummonSlotClicked = useCallback(
    (slot: SummonSlot) => {
      dispatchInteraction({ type: 'select_summon_slot', slot })
    },
    [dispatchInteraction]
  )

  const onChoicePicked = useCallback(
    (choice: SpellAction) => {
      const actions = actionsRef.current
      if (!actions) return
      dispatchInteraction({ type: 'select_choice', choice, actions })
    },
    [dispatchInteraction]
  )

  const onCancel = useCallback(() => {
    dispatchInteraction({ type: 'cancel' })
  }, [dispatchInteraction])

  const value: GameContextValue = {
    state,
    interaction,
    activeEffects,
    revealedCard,
    sendAction,
    sendMulligan,
    concedeGame,
    onEntityClicked,
    onEndTurnClicked,
    onSummonSlotClicked,
    onChoicePicked,
    onCancel,
    hoveredEntity,
    setHoveredEntity,
  }

  return <GameContext.Provider value={value}>{children}</GameContext.Provider>
}
