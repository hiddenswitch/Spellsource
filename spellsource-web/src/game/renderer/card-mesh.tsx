import React, { useState, useCallback } from 'react'
import type { ThreeEvent } from '@react-three/fiber'
import type { Entity } from 'spellsource-protos/dist/experimental/client/spellsource'
import type { PlayerSide } from '../types'
import { Zone } from '../types'
import * as C from './constants'
import type { Vec3 } from './board-layout'
import { handPosition, handRotation, battlefieldPosition } from './board-layout'

interface CardProps {
  entity: Entity
  side: PlayerSide
  zone: Zone
  index: number
  faceDown?: boolean
  position: Vec3
  rotationY?: number
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
}

const CardMesh: React.FC<CardProps> = ({
  entity,
  side,
  zone,
  faceDown = false,
  position,
  rotationY = 0,
  onEntityClicked,
  onHoverStart,
  onHoverEnd,
}) => {
  const [hovered, setHovered] = useState(false)

  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onEntityClicked?.(entity, side)
    },
    [entity, side, onEntityClicked]
  )

  const handlePointerOver = useCallback(
    (e: ThreeEvent<PointerEvent>) => {
      e.stopPropagation()
      setHovered(true)
      document.body.style.cursor = 'pointer'
      onHoverStart?.(entity)
    },
    [entity, onHoverStart]
  )

  const handlePointerOut = useCallback(
    () => {
      setHovered(false)
      document.body.style.cursor = 'default'
      onHoverEnd?.()
    },
    [onHoverEnd]
  )

  const topColor = faceDown ? C.COLOR_CARD_BACK : C.COLOR_CARD_FRONT

  return (
    <mesh
      position={position}
      rotation={[0, rotationY, 0]}
      castShadow
      receiveShadow
      onClick={handleClick}
      onPointerOver={handlePointerOver}
      onPointerOut={handlePointerOut}
    >
      <boxGeometry args={[C.CARD_WIDTH, C.CARD_DEPTH, C.CARD_HEIGHT]} />
      {/* right, left, top (visible), bottom, front edge, back edge */}
      <meshStandardMaterial attach="material-0" color={C.COLOR_CARD_FRONT} />
      <meshStandardMaterial attach="material-1" color={C.COLOR_CARD_FRONT} />
      <meshStandardMaterial attach="material-2" color={topColor} />
      <meshStandardMaterial attach="material-3" color={C.COLOR_CARD_BACK} />
      <meshStandardMaterial attach="material-4" color={C.COLOR_CARD_FRONT} />
      <meshStandardMaterial attach="material-5" color={C.COLOR_CARD_FRONT} />
    </mesh>
  )
}

interface HandProps {
  side: PlayerSide
  hand: Entity[]
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
}

export const Hand: React.FC<HandProps> = ({ side, hand, onEntityClicked, onHoverStart, onHoverEnd }) => {
  const faceDown = side === 'top'
  return (
    <group>
      {hand.map((entity, i) => (
        <CardMesh
          key={entity.id}
          entity={entity}
          side={side}
          zone={Zone.HAND}
          index={i}
          faceDown={faceDown}
          position={handPosition(side, i, hand.length)}
          rotationY={handRotation(side, i, hand.length)}
          onEntityClicked={onEntityClicked}
          onHoverStart={onHoverStart}
          onHoverEnd={onHoverEnd}
        />
      ))}
    </group>
  )
}

interface BattlefieldProps {
  side: PlayerSide
  entities: Entity[]
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
}

export const Battlefield: React.FC<BattlefieldProps> = ({
  side,
  entities,
  onEntityClicked,
  onHoverStart,
  onHoverEnd,
}) => (
  <group>
    {entities.map((entity, i) => (
      <CardMesh
        key={entity.id}
        entity={entity}
        side={side}
        zone={Zone.BATTLEFIELD}
        index={i}
        position={battlefieldPosition(side, i, entities.length)}
        onEntityClicked={onEntityClicked}
        onHoverStart={onHoverStart}
        onHoverEnd={onHoverEnd}
      />
    ))}
  </group>
)
