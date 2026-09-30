import React, { useState } from 'react'
import type { MatchmakingState, MatchmakingActions } from '../../hooks/use-matchmaking'
import styles from './overlay.module.css'

interface QueueScreenProps {
  state: MatchmakingState
  actions: MatchmakingActions
  onDemo: () => void
}

export const QueueScreen: React.FC<QueueScreenProps> = ({ state, actions, onDemo }) => {
  const { phase, queues, decks, loading, error } = state
  const [selectedDeckId, setSelectedDeckId] = useState<string | null>(null)

  if (phase === 'searching') {
    return (
      <div className={styles.queueScreen}>
        <div className={styles.queueTitle}>Finding Opponent</div>
        <div className={styles.queueStatus}>
          <span className={styles.queueSpinner} />
          Searching for a match...
        </div>
        <button className={styles.queueButtonSecondary} onClick={actions.cancel}>
          Cancel
        </button>
        {error && <div className={styles.queueError}>{error}</div>}
      </div>
    )
  }

  if (phase === 'connecting') {
    return (
      <div className={styles.queueScreen}>
        <div className={styles.queueTitle}>Match Found</div>
        <div className={styles.queueStatus}>
          <span className={styles.queueSpinner} />
          Connecting to game...
        </div>
      </div>
    )
  }

  // idle — show deck picker + play button
  const defaultQueue = queues.length > 0 ? queues[0] : null
  const hasDecks = decks.length > 0
  const chosenDeck = selectedDeckId ?? (hasDecks ? decks[0].id : null)
  const canPlay = defaultQueue && chosenDeck

  const handlePlay = () => {
    if (!defaultQueue || !chosenDeck) return
    actions.enqueue(defaultQueue.queueId, chosenDeck)
  }

  return (
    <div className={styles.queueScreen}>
      <div className={styles.queueTitle}>Spellsource</div>
      <div className={styles.queueSubtitle}>Card game engine</div>

      {hasDecks && (
        <div className={styles.deckSelector}>
          <div className={styles.deckSelectorLabel}>Choose a deck</div>
          <div className={styles.deckList}>
            {decks.map((deck) => (
              <div
                key={deck.id}
                className={`${styles.deckItem} ${chosenDeck === deck.id ? styles.selected : ''}`}
                onClick={() => setSelectedDeckId(deck.id)}
              >
                <div className={styles.deckItemCheck} />
                <div className={styles.deckItemName}>
                  {deck.name || 'Unnamed Deck'}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <button
        className={styles.queueButton}
        onClick={handlePlay}
        disabled={loading || !canPlay}
      >
        {loading ? 'Loading...' : !hasDecks ? 'No decks available' : 'Play vs Bot'}
      </button>

      {error && <div className={styles.queueError}>{error}</div>}

      <span className={styles.queueDemoLink} onClick={onDemo}>
        Demo mode (offline)
      </span>
    </div>
  )
}
