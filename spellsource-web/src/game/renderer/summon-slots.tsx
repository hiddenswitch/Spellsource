import React, { useCallback, useState } from 'react'
import type { ThreeEvent } from '@react-three/fiber'
import type { SummonSlot } from '../state/action-resolver'
import type { Entity } from '../../__generated__/client'
import * as C from './constants'
import { battlefieldPosition } from './board-layout'

interface SummonSlotsProps {
  slots: SummonSlot[]
  /** Current bottom battlefield entities (to compute slot positions) */
  fieldEntities: Entity[]
  onSlotClicked: (slot: SummonSlot) => void
}

/**
 * Renders clickable slot indicators on the bottom battlefield
 * showing where a minion can be summoned.
 */
export const SummonSlots: React.FC<SummonSlotsProps> = ({
  slots,
  fieldEntities,
  onSlotClicked,
}) => {
  if (slots.length === 0) return null

  // Compute positions for N+1 slots given N existing minions
  // Slots go between existing minions and at both edges
  const fieldCount = fieldEntities.length
  const newTotal = fieldCount + 1 // one more minion after placement

  return (
    <group>
      {slots.map((slot) => (
        <SlotMarker
          key={slot.actionIndex}
          slot={slot}
          fieldCount={fieldCount}
          newTotal={newTotal}
          onSlotClicked={onSlotClicked}
        />
      ))}
    </group>
  )
}

interface SlotMarkerProps {
  slot: SummonSlot
  fieldCount: number
  newTotal: number
  onSlotClicked: (slot: SummonSlot) => void
}

const SlotMarker: React.FC<SlotMarkerProps> = ({ slot, fieldCount, newTotal, onSlotClicked }) => {
  const [hovered, setHovered] = useState(false)

  // Position this slot marker at where the new minion would appear
  const pos = battlefieldPosition('bottom', slot.battlefieldIndex, newTotal)

  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onSlotClicked(slot)
    },
    [slot, onSlotClicked]
  )

  return (
    <mesh
      position={pos}
      onClick={handleClick}
      onPointerOver={(e) => {
        e.stopPropagation()
        setHovered(true)
        document.body.style.cursor = 'pointer'
      }}
      onPointerOut={() => {
        setHovered(false)
        document.body.style.cursor = 'default'
      }}
    >
      <boxGeometry args={[0.15, 0.04, C.CARD_HEIGHT * 0.8]} />
      <meshStandardMaterial
        color={hovered ? 0x66ff66 : 0x44aa44}
        emissive={hovered ? 0x44ff44 : 0x22aa22}
        emissiveIntensity={hovered ? 0.8 : 0.5}
        transparent
        opacity={hovered ? 0.9 : 0.6}
      />
    </mesh>
  )
}
