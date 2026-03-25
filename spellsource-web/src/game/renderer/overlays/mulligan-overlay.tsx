import React, { useCallback, useState } from 'react'
import type { Entity } from '../../../__generated__/client'
import styles from './overlay.module.css'

interface MulliganOverlayProps {
  cards: Entity[]
  onConfirm: (discardedIndices: number[]) => void
}

export const MulliganOverlay: React.FC<MulliganOverlayProps> = ({ cards, onConfirm }) => {
  const [discarded, setDiscarded] = useState<Set<number>>(new Set())

  const toggleCard = useCallback((index: number) => {
    setDiscarded((prev) => {
      const next = new Set(prev)
      if (next.has(index)) {
        next.delete(index)
      } else {
        next.add(index)
      }
      return next
    })
  }, [])

  const handleConfirm = useCallback(() => {
    onConfirm([...discarded])
  }, [discarded, onConfirm])

  return (
    <div className={styles.mulliganOverlay}>
      <div className={styles.mulliganTitle}>Choose cards to replace</div>
      <div className={styles.mulliganCards}>
        {cards.map((card, i) => (
          <div
            key={card.id}
            className={`${styles.mulliganCard} ${discarded.has(i) ? styles.discarding : ''}`}
            onClick={() => toggleCard(i)}
          >
            <div className={styles.mulliganCardCost}>
              {card.manaCost ?? 0}
            </div>
            <div className={styles.mulliganCardName}>{card.name}</div>
            <div className={styles.mulliganCardStats}>
              {card.attack !== undefined && card.attack !== null && `${card.attack} / `}
              {card.hp !== undefined && card.hp !== null && `${card.hp}`}
            </div>
          </div>
        ))}
      </div>
      <button className={styles.mulliganConfirm} onClick={handleConfirm}>
        Confirm
      </button>
    </div>
  )
}
