import React, { useCallback } from 'react'
import type { SpellAction } from '../../../__generated__/client'
import styles from './overlay.module.css'

interface ChooseOneOverlayProps {
  choices: SpellAction[]
  onPick: (choice: SpellAction) => void
  onCancel: () => void
}

export const ChooseOneOverlay: React.FC<ChooseOneOverlayProps> = ({
  choices,
  onPick,
  onCancel,
}) => {
  if (choices.length === 0) return null

  return (
    <div className={styles.discoverOverlay}>
      {choices.map((choice, i) => (
        <div
          key={choice.action}
          className={styles.discoverCard}
          onClick={() => onPick(choice)}
        >
          {choice.entity?.manaCost != null && (
            <div className={styles.discoverCardCost}>{choice.entity.manaCost}</div>
          )}
          <div className={styles.discoverCardName}>
            {choice.entity?.name || choice.description || `Choice ${i + 1}`}
          </div>
          {(choice.entity?.description || choice.description) && (
            <div className={styles.discoverCardDesc}>
              {choice.entity?.description || choice.description}
            </div>
          )}
        </div>
      ))}
    </div>
  )
}
