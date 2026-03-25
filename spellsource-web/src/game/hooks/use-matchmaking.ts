import { useCallback, useState } from 'react'
import {
  useGetMatchmakingQueuesQuery,
  useGetDecksQuery,
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
  const { data: decksData, loading: decksLoading } = useGetDecksQuery()
  const [enqueueMutation] = useEnqueueMatchmakingMutation()
  const [cancelMutation] = useCancelMatchmakingMutation()
  const [connectToGameMutation] = useConnectToGameMutation()

  // Listen for match found — only active when searching
  useMatchFoundSubscription({
    skip: phase !== 'searching',
    onData: async ({ data: { data } }) => {
      if (!data?.matchFound) return
      const { playerKey, playerSecret } = data.matchFound
      setPhase('connecting')
      try {
        await connectToGameMutation({
          variables: { playerKey, playerSecret },
        })
        setPhase('ready')
      } catch (e) {
        setError('Failed to connect to game')
        setPhase('idle')
      }
    },
  })

  // Collect decks from both owned and shared
  const ownedDecks: DeckChoice[] = (decksData?.allDecks?.nodes ?? [])
    .filter((n): n is NonNullable<typeof n> => n != null)
    .map((d) => ({ id: d.id, name: d.name }))
  const sharedDecks: DeckChoice[] = (decksData?.allDeckShares?.nodes ?? [])
    .filter((n): n is NonNullable<typeof n> => n?.deckByDeckId != null)
    .map((n) => ({ id: n!.deckByDeckId!.id, name: n!.deckByDeckId!.name }))
  const allDecks = [...ownedDecks, ...sharedDecks]

  const enqueue = useCallback(
    async (queueId: string, deckId: string) => {
      setError(null)
      setPhase('searching')
      try {
        await enqueueMutation({
          variables: { input: { queueId, deckId } },
        })
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
