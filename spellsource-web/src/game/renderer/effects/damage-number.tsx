import React, { useMemo } from 'react'
import { Html } from '@react-three/drei'
import type { ActiveEffect } from '../../hooks/use-animation-queue'
import type { Vec3 } from '../board-layout'

interface DamageNumberProps {
  effect: ActiveEffect
  position: Vec3 | undefined
}

export const DamageNumber: React.FC<DamageNumberProps> = ({ effect, position }) => {
  if (!position || !effect.value) return null

  const elapsed = Date.now() - effect.createdAt
  const progress = Math.min(1, elapsed / effect.duration)

  // Float upward and fade out
  const yOffset = progress * 1.2
  const opacity = 1 - progress * progress

  const isDamage = effect.type === 'damage'
  const color = isDamage ? '#ff3333' : '#44ff44'
  const prefix = isDamage ? '-' : '+'

  return (
    <group position={[position[0], position[1] + 0.5 + yOffset, position[2]]}>
      <Html center style={{ pointerEvents: 'none' }}>
        <div
          style={{
            color,
            fontSize: 14,
            fontWeight: 700,
            fontFamily: "'Segoe UI', Arial, sans-serif",
            textShadow: '0 1px 3px rgba(0,0,0,0.9)',
            opacity,
            userSelect: 'none',
            whiteSpace: 'nowrap',
          }}
        >
          {prefix}{effect.value}
        </div>
      </Html>
    </group>
  )
}

interface DamageNumberLayerProps {
  effects: ActiveEffect[]
  entityPositions: Map<number, Vec3>
}

export const DamageNumberLayer: React.FC<DamageNumberLayerProps> = ({
  effects,
  entityPositions,
}) => {
  const damageEffects = useMemo(
    () => effects.filter((e) => e.type === 'damage' || e.type === 'heal'),
    [effects]
  )

  if (damageEffects.length === 0) return null

  return (
    <group>
      {damageEffects.map((effect) => (
        <DamageNumber
          key={effect.id}
          effect={effect}
          position={entityPositions.get(effect.entityId)}
        />
      ))}
    </group>
  )
}
