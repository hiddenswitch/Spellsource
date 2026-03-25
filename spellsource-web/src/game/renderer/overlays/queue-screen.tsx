import React from 'react'
import type { MatchmakingState, MatchmakingActions } from '../../hooks/use-matchmaking'
import styles from './overlay.module.css'

interface QueueScreenProps {
  state: MatchmakingState
  actions: MatchmakingActions
  onDemo: () => void
}

export const QueueScreen: React.FC<QueueScreenProps> = ({ state, actions, onDemo }) => {
  const { phase, queues, decks, loading, error } = state

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

  // idle — show play button
  const defaultQueue = queues.length > 0 ? queues[0] : null
  const hasDecks = decks.length > 0
  const canPlay = defaultQueue && hasDecks

  const handlePlayBot = () => {
    if (!defaultQueue || !hasDecks) return
    // Pick a random deck from the user's available decks
    const randomDeck = decks[Math.floor(Math.random() * decks.length)]
    actions.enqueue(defaultQueue.queueId, randomDeck.id)
  }

  return (
    <div className={styles.queueScreen}>
      <div className={styles.queueTitle}>Spellsource</div>
      <div className={styles.queueSubtitle}>Card game engine</div>

      <button
        className={styles.queueButton}
        onClick={handlePlayBot}
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
