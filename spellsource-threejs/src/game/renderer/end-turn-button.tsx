import React, { useCallback } from 'react'
import type { ThreeEvent } from '@react-three/fiber'
import * as C from './constants'
import { endTurnPosition } from './board-layout'

interface EndTurnButtonProps {
  active: boolean
  onClick?: () => void
}

export const EndTurnButton: React.FC<EndTurnButtonProps> = ({ active, onClick }) => {
  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onClick?.()
    },
    [onClick]
  )

  return (
    <mesh
      position={endTurnPosition()}
      onClick={handleClick}
      onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer' }}
      onPointerOut={() => { document.body.style.cursor = 'default' }}
    >
      <cylinderGeometry args={[0.5, 0.5, 0.08, 16]} />
      <meshStandardMaterial
        color={active ? C.COLOR_END_TURN_ACTIVE : C.COLOR_END_TURN_INACTIVE}
        roughness={0.4}
        metalness={0.3}
      />
    </mesh>
  )
}
