import { useCallback, useState } from 'react'
import {
  useGetMatchmakingQueuesQuery,
  useGetGameDecksQuery,
  useEnqueueMatchmakingMutation,
  useCancelMatchmakingMutation,
  useMatchFoundSubscription,
  useConnectToGameMutation,
} from '../../__generated__/client'

export type MatchmakingPhase = 'idle' | 'searching' | 'connecting' | 'ready'

export interface DeckChoice {
  id: string
  name: string | null | undefined
}

export interface MatchmakingState {
  phase: MatchmakingPhase
  queues: Array<{ queueId: string; name: string; description: string }>
  decks: DeckChoice[]
  loading: boolean
  error: string | null
}

export interface MatchmakingActions {
  enqueue: (queueId: string, deckId: string) => void
  cancel: () => void
}

export function useMatchmaking() {
  const [phase, setPhase] = useState<MatchmakingPhase>('idle')
  const [error, setError] = useState<string | null>(null)

  const { data: queuesData, loading: queuesLoading } = useGetMatchmakingQueuesQuery()
  const { data: decksData, loading: decksLoading } = useGetGameDecksQuery()
  const [enqueueMutation] = useEnqueueMatchmakingMutation()
  const [cancelMutation] = useCancelMatchmakingMutation()
  const [connectToGameMutation] = useConnectToGameMutation()

  // Subscribe before enqueueing so a fast bot match cannot be missed.
  useMatchFoundSubscription({
    skip: false,
    onError: failure => setError(failure.message),
    onData: async ({ data: { data } }) => {
      if (!data?.matchFound || phase === 'ready' || phase === 'connecting') return
      const { playerKey, playerSecret } = data.matchFound
      setPhase('connecting')
      try {
        const response = await connectToGameMutation({ variables: { playerKey, playerSecret } });
        if (!response.data?.connectToGame) throw new Error('The server did not connect the game');
        setPhase('ready')
      } catch (e) {
        setError('Failed to connect to game')
        setPhase('idle')
      }
    },
  })

  const allDecks: DeckChoice[] = (decksData?.decks ?? []).flatMap(deck => deck.collection ? [{
    id: deck.collection.id, name: deck.collection.name,
  }] : []);

  const enqueue = useCallback(
    async (queueId: string, deckId: string) => {
      setError(null)
      setPhase('searching')
      try {
        const result = await enqueueMutation({
          variables: { input: { queueId, deckId } },
        })
        if (!result.data?.enqueueMatchmaking) throw new Error("Matchmaking rejected the request")
      } catch (e) {
        setError('Failed to enqueue')
        setPhase('idle')
      }
    },
    [enqueueMutation]
  )

  const cancel = useCallback(async () => {
    try {
      await cancelMutation()
    } catch (_) {
      // ignore cancel errors
    }
    setPhase('idle')
    setError(null)
  }, [cancelMutation])

  const state: MatchmakingState = {
    phase,
    queues: queuesData?.matchmakingQueues ?? [],
    decks: allDecks,
    loading: queuesLoading || decksLoading,
    error,
  }

  const actions: MatchmakingActions = { enqueue, cancel }

  return { state, actions }
}
