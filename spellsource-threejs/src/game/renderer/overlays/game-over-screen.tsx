import React from 'react'
import type { GameOver } from '../../../__generated__/client'
import styles from './overlay.module.css'

interface GameOverScreenProps {
  gameOver: GameOver | undefined
  localPlayerId: number
  onReturn: () => void
}

export const GameOverScreen: React.FC<GameOverScreenProps> = ({
  gameOver,
  localPlayerId,
  onReturn,
}) => {
  if (!gameOver) return null

  const won = gameOver.winningPlayerId === localPlayerId

  return (
    <div className={styles.gameOverScreen}>
      <div className={`${styles.gameOverTitle} ${won ? styles.win : styles.loss}`}>
        {won ? 'Victory!' : 'Defeat'}
      </div>
      <button className={styles.gameOverButton} onClick={onReturn}>
        Return
      </button>
    </div>
  )
}
