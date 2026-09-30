import React from 'react'
import type { RevealedCard } from '../../hooks/use-animation-queue'
import { CardType } from '../../../__generated__/client'
import { StatBadge } from './stat-badge'
import styles from './overlay.module.css'

interface CardRevealProps {
  revealedCard: RevealedCard | null
}

function cardTypeName(type: CardType): string {
  switch (type) {
    case CardType.Minion: return 'Minion'
    case CardType.Spell: return 'Spell'
    case CardType.Weapon: return 'Weapon'
    case CardType.Hero: return 'Hero'
    case CardType.HeroPower: return 'Hero Power'
    default: return ''
  }
}

export const CardReveal: React.FC<CardRevealProps> = ({ revealedCard }) => {
  if (!revealedCard) return null

  const { entity } = revealedCard
  const elapsed = Date.now() - revealedCard.createdAt
  // Fade out over the last 500ms
  const opacity = elapsed > 2500 ? Math.max(0, 1 - (elapsed - 2500) / 500) : 1

  return (
    <div className={styles.cardReveal} style={{ opacity }}>
      <div className={styles.cardRevealHeader}>Opponent played</div>
      <div className={styles.cardRevealName}>{entity.name}</div>
      <div className={styles.cardRevealType}>
        {cardTypeName(entity.cardType)}
        {entity.tribes && entity.tribes.length > 0 && ` — ${entity.tribes.join(', ')}`}
      </div>
      {entity.description && (
        <div className={styles.cardRevealDesc}>{entity.description}</div>
      )}
      <div className={styles.cardRevealStats}>
        {entity.manaCost != null && (
          <StatBadge value={entity.manaCost} type="mana" baseValue={entity.baseManaCost ?? undefined} />
        )}
        {entity.attack != null && (
          <StatBadge value={entity.attack} type="attack" baseValue={entity.baseAttack ?? undefined} />
        )}
        {entity.hp != null && (
          <StatBadge value={entity.hp} type="hp" maxValue={entity.maxHp ?? undefined} />
        )}
        {entity.durability != null && (
          <StatBadge value={entity.durability} type="durability" />
        )}
      </div>
    </div>
  )
}
