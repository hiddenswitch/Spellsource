import React from 'react'
import styles from './overlay.module.css'

interface StatBadgeProps {
  value: number
  type: 'mana' | 'attack' | 'hp' | 'armor' | 'durability'
  baseValue?: number
  maxValue?: number
}

export const StatBadge: React.FC<StatBadgeProps> = ({ value, type, baseValue, maxValue }) => {
  let colorClass = ''
  if (type === 'attack' && baseValue !== undefined && value > baseValue) {
    colorClass = styles.buffed
  } else if (type === 'hp' && maxValue !== undefined && value < maxValue) {
    colorClass = styles.damaged
  } else if (type === 'mana' && baseValue !== undefined && value < baseValue) {
    colorClass = styles.buffed
  }

  return (
    <span className={`${styles.statBadge} ${styles[type]} ${colorClass}`}>
      {value}
    </span>
  )
}
