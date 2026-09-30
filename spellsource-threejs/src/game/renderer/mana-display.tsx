import React from 'react'
import type { PlayerEntities } from '../types'
import type { PlayerSide } from '../types'
import * as C from './constants'
import { manaPosition } from './board-layout'
import { ManaTextOverlay } from './overlays/hero-overlay'

interface ManaDisplayProps {
  bottom: PlayerEntities
  top: PlayerEntities
}

export const ManaDisplay: React.FC<ManaDisplayProps> = ({ bottom, top }) => (
  <group>
    {([['bottom', bottom], ['top', top]] as const).map(([side, entities]) => {
      const playerEntity = entities.player
      if (!playerEntity) return null

      const mana = playerEntity.mana
      const maxMana = playerEntity.maxMana
      const basePos = manaPosition(side)

      return (
        <group key={side}>
          {Array.from({ length: maxMana }, (_, i) => {
            const filled = i < mana
            return (
              <mesh
                key={i}
                position={[
                  basePos[0] + (i % 5) * 0.25 - 0.5,
                  basePos[1] + 0.1,
                  basePos[2] + Math.floor(i / 5) * 0.25,
                ]}
              >
                <octahedronGeometry args={[0.1, 0]} />
                <meshStandardMaterial
                  color={filled ? C.COLOR_MANA_CRYSTAL : C.COLOR_MANA_EMPTY}
                  roughness={0.3}
                  metalness={0.5}
                  transparent={!filled}
                  opacity={filled ? 1.0 : 0.4}
                />
              </mesh>
            )
          })}

          {/* Mana text: current/max */}
          <group position={[basePos[0], basePos[1] + 0.3, basePos[2] - (side === 'bottom' ? 0.4 : -0.4)]}>
            <ManaTextOverlay mana={mana} maxMana={maxMana} />
          </group>
        </group>
      )
    })}
  </group>
)
