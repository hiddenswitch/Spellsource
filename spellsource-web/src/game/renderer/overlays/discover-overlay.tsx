import React, { useCallback } from 'react'
import type { Entity, GameActions } from '../../../__generated__/client'
import { ActionType } from '../../../__generated__/client'
import styles from './overlay.module.css'

interface DiscoverOverlayProps {
  cards: Entity[]
  actions: GameActions | undefined
  onAction: (actionIndex: number) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
}

export const DiscoverOverlay: React.FC<DiscoverOverlayProps> = ({
  cards,
  actions,
  onAction,
  onHoverStart,
  onHoverEnd,
}) => {
  const handlePick = useCallback(
    (entity: Entity) => {
      if (!actions) return
      for (const sa of actions.all) {
        if (sa.actionType === ActionType.Discover && sa.sourceId === entity.id) {
          onAction(sa.action)
          return
        }
      }
    },
    [actions, onAction]
  )

  if (cards.length === 0) return null

  return (
    <div className={styles.discoverOverlay}>
      {cards.map((card) => (
        <div
          key={card.id}
          className={styles.discoverCard}
          onClick={() => handlePick(card)}
          onMouseEnter={() => onHoverStart?.(card)}
          onMouseLeave={() => onHoverEnd?.()}
        >
          <div className={styles.discoverCardCost}>{card.manaCost ?? 0}</div>
          <div className={styles.discoverCardName}>{card.name}</div>
          {card.description && (
            <div className={styles.discoverCardDesc}>{card.description}</div>
          )}
        </div>
      ))}
    </div>
  )
}
