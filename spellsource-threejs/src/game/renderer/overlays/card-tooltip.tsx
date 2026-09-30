import React from 'react'
import type { Entity } from '../../../__generated__/client'
import { CardType } from '../../../__generated__/client'
import { StatBadge } from './stat-badge'
import styles from './overlay.module.css'

interface CardTooltipProps {
  entity: Entity | null
  mouseX: number
  mouseY: number
}

function cardTypeName(type: CardType): string {
  switch (type) {
    case CardType.Minion: return 'Minion'
    case CardType.Spell: return 'Spell'
    case CardType.Weapon: return 'Weapon'
    case CardType.Hero: return 'Hero'
    case CardType.HeroPower: return 'Hero Power'
    case CardType.Class: return 'Class'
    case CardType.Format: return 'Format'
    case CardType.Group: return 'Group'
    default: return ''
  }
}

export const CardTooltip: React.FC<CardTooltipProps> = ({ entity, mouseX, mouseY }) => {
  if (!entity) return null

  const x = mouseX + 16
  const y = mouseY + 16

  return (
    <div
      className={styles.cardTooltip}
      style={{ position: 'fixed', left: x, top: y, zIndex: 1000 }}
    >
      <div className={styles.tooltipName}>{entity.name}</div>
      <div className={styles.tooltipType}>
        {cardTypeName(entity.cardType)}
        {entity.tribes && entity.tribes.length > 0 && ` — ${entity.tribes.join(', ')}`}
      </div>
      {entity.description && (
        <div className={styles.tooltipDesc}>{entity.description}</div>
      )}
      <div className={styles.tooltipStats}>
        {entity.manaCost !== undefined && entity.manaCost !== null && (
          <StatBadge
            value={entity.manaCost}
            type="mana"
            baseValue={entity.baseManaCost ?? undefined}
          />
        )}
        {entity.attack !== undefined && entity.attack !== null && (
          <StatBadge
            value={entity.attack}
            type="attack"
            baseValue={entity.baseAttack ?? undefined}
          />
        )}
        {entity.hp !== undefined && entity.hp !== null && (
          <StatBadge
            value={entity.hp}
            type="hp"
            maxValue={entity.maxHp ?? undefined}
          />
        )}
      </div>
    </div>
  )
}
