import { useCallback, useReducer } from 'react'
import {
  useGameMessagesSubscription,
  useSendGameActionMutation,
  useSendMulliganMutation,
  useConcedeGameMutation,
} from '../../__generated__/client'
import type { ServerGameMessage } from '../../__generated__/client'
import { reduceGameMessage, createInitialState } from '../state/game-state-manager'
import { useAnimationQueue, type ActiveEffect, type RevealedCard } from './use-animation-queue'
import type { ManagedGameState } from '../types'

export interface GameConnection {
  state: ManagedGameState
  activeEffects: ActiveEffect[]
  revealedCard: RevealedCard | null
  sendAction: (actionIndex: number) => void
  sendMulligan: (discardedCardIndices: number[]) => void
  concedeGame: () => void
  connected: boolean
}

export interface UseGameConnectionOptions {
  /** Skip the subscription (don't connect until ready) */
  skip?: boolean
}

export function useGameConnection(options?: UseGameConnectionOptions): GameConnection {
  const skip = options?.skip ?? false
  const [state, dispatch] = useReducer(reduceGameMessage, undefined, createInitialState)

  const { enqueue, activeEffects, revealedCard } = useAnimationQueue({ dispatch })

  const { loading } = useGameMessagesSubscription({
    skip,
    onData: ({ data: { data } }) => {
      if (data?.gameMessages) {
        enqueue(data.gameMessages as ServerGameMessage)
      }
    },
  })

  const [sendActionMutation] = useSendGameActionMutation()
  const [sendMulliganMutation] = useSendMulliganMutation()
  const [concedeMutation] = useConcedeGameMutation()

  const sendAction = useCallback(
    (actionIndex: number) => {
      if (!state.actionsMessageId) return
      const messageId = state.actionsMessageId
      // Clear actions immediately to prevent double-sends
      dispatch({
        messageType: 'ON_UPDATE',
        localPlayerId: state.localPlayerId,
        isReplayMessage: false,
      } as ServerGameMessage)
      sendActionMutation({
        variables: { actionIndex, repliesTo: messageId },
      })
    },
    [state.actionsMessageId, state.localPlayerId, sendActionMutation]
  )

  const sendMulligan = useCallback(
    (discardedCardIndices: number[]) => {
      if (!state.mulliganMessageId) return
      // Clear mulligan UI immediately to prevent lingering overlay
      dispatch({
        messageType: 'ON_UPDATE',
        localPlayerId: state.localPlayerId,
        isReplayMessage: false,
      } as ServerGameMessage)
      sendMulliganMutation({
        variables: {
          discardedCardIndices,
          repliesTo: state.mulliganMessageId,
        },
      })
    },
    [state.mulliganMessageId, state.localPlayerId, sendMulliganMutation]
  )

  const concedeGame = useCallback(() => {
    concedeMutation()
  }, [concedeMutation])

  return {
    state,
    activeEffects,
    revealedCard,
    sendAction,
    sendMulligan,
    concedeGame,
    connected: !skip && !loading,
  }
}
