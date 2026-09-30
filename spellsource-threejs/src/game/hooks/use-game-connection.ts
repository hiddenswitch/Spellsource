import { useCallback, useReducer, useRef, useState } from 'react'
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
  error: string | null
  connected: boolean
}

export interface UseGameConnectionOptions {
  /** Skip the subscription (don't connect until ready) */
  skip?: boolean
}

export function useGameConnection(options?: UseGameConnectionOptions): GameConnection {
  const skip = options?.skip ?? false
  const submitted = useRef(new Set<string>())
  const [error, setError] = useState<string | null>(null)
  const [state, dispatch] = useReducer(reduceGameMessage, undefined, createInitialState)

  const { enqueue, activeEffects, revealedCard } = useAnimationQueue({ dispatch })

  const { loading } = useGameMessagesSubscription({
    onError: failure => setError(failure.message),
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

  const submit = useCallback(async (messageId: string | undefined, operation: () => Promise<unknown>) => {
    if (!messageId || submitted.current.has(messageId)) return;
    submitted.current.add(messageId);
    setError(null);
    try {
      await operation();
    } catch (failure) {
      submitted.current.delete(messageId);
      setError(failure instanceof Error ? failure.message : 'Unable to send the action');
    }
  }, []);

  const sendAction = useCallback((actionIndex: number) => {
    void submit(state.actionsMessageId, () => sendActionMutation({
      variables: { actionIndex, repliesTo: state.actionsMessageId! },
    }));
  }, [state.actionsMessageId, sendActionMutation, submit]);

  const sendMulligan = useCallback((discardedCardIndices: number[]) => {
    void submit(state.mulliganMessageId, () => sendMulliganMutation({
      variables: { discardedCardIndices, repliesTo: state.mulliganMessageId! },
    }));
  }, [state.mulliganMessageId, sendMulliganMutation, submit]);

  const concedeGame = useCallback(() => {
    void concedeMutation().catch(failure => setError(failure.message));
  }, [concedeMutation]);

  return {
    state,
    activeEffects,
    revealedCard,
    sendAction,
    sendMulligan,
    concedeGame,
    error,
    connected: !skip && !loading,
  }
}
