import React from 'react'
import * as C from './constants'

export const BoardMesh: React.FC = () => (
  <group>
    {/* Main surface */}
    <mesh position={[0, -0.05, 0]} receiveShadow>
      <boxGeometry args={[C.BOARD_WIDTH, 0.1, C.BOARD_DEPTH]} />
      <meshStandardMaterial color={C.COLOR_BOARD_SURFACE} roughness={0.8} metalness={0.1} />
    </mesh>

    {/* Raised border/rim */}
    <mesh position={[0, -0.12, 0]} receiveShadow>
      <boxGeometry args={[C.BOARD_WIDTH + 0.3, 0.15, C.BOARD_DEPTH + 0.3]} />
      <meshStandardMaterial color={C.COLOR_BOARD_BORDER} roughness={0.7} metalness={0.2} />
    </mesh>

    {/* Center divider line */}
    <mesh position={[0, 0.01, 0]}>
      <boxGeometry args={[C.BOARD_WIDTH - 1, 0.02, 0.05]} />
      <meshStandardMaterial color={C.COLOR_BOARD_DIVIDER} />
    </mesh>
  </group>
)
