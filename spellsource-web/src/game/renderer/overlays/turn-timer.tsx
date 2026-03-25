import React from 'react'
import type { Timers } from '../../../__generated__/client'
import styles from './overlay.module.css'

interface TurnTimerProps {
  timers: Timers | undefined
  isLocalPlayerTurn: boolean
}

export const TurnTimer: React.FC<TurnTimerProps> = ({ timers, isLocalPlayerTurn }) => {
  if (!timers || !isLocalPlayerTurn) return null

  const remaining = Number(timers.millisRemaining ?? 0)
  const total = 75000 // default turn duration
  const pct = total > 0 ? Math.max(0, Math.min(100, (remaining / total) * 100)) : 100
  const isWarning = pct < 25

  return (
    <div className={styles.turnTimer}>
      <div
        className={`${styles.turnTimerBar} ${isWarning ? styles.warning : ''}`}
        style={{ width: `${pct}%` }}
      />
    </div>
  )
}
