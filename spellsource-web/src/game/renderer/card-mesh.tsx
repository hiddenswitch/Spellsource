import React, { useState, useCallback, useEffect, useRef } from 'react'
import { useSpring, animated } from '@react-spring/three'
import type { ThreeEvent } from '@react-three/fiber'
import type { Entity } from '../../__generated__/client'
import type { PlayerSide } from '../types'
import { Zone } from '../types'
import type { InteractionState } from '../state/interaction-state'
import type { ActiveEffect } from '../hooks/use-animation-queue'
import * as C from './constants'
import type { Vec3 } from './board-layout'
import { handPosition, handRotation, battlefieldPosition } from './board-layout'
import { CardOverlay } from './overlays/card-overlay'

interface CardProps {
  entity: Entity
  side: PlayerSide
  zone: Zone
  index: number
  faceDown?: boolean
  position: Vec3
  rotationY?: number
  interaction?: InteractionState
  effects?: ActiveEffect[]
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
  onPositionReady?: (entityId: number, pos: Vec3) => void
}

function getEmissive(
  entity: Entity,
  interaction: InteractionState | undefined,
  hovered: boolean
): { color: number; intensity: number } {
  if (!interaction) return { color: 0x000000, intensity: 0 }

  if (interaction.selectedSource?.id === entity.id) {
    return { color: 0x44ff44, intensity: 0.6 }
  }

  if (
    interaction.phase === 'awaiting_target' &&
    interaction.validTargets.includes(entity.id)
  ) {
    return { color: 0xffcc00, intensity: hovered ? 0.6 : 0.4 }
  }

  if (interaction.playableEntityIds.includes(entity.id)) {
    return { color: 0x44aa44, intensity: hovered ? 0.4 : 0.2 }
  }

  return { color: 0x000000, intensity: 0 }
}

function getEffectScale(entityId: number, effects?: ActiveEffect[]): [number, number, number] {
  if (!effects) return [1, 1, 1]
  for (const e of effects) {
    if (e.entityId !== entityId) continue
    const progress = Math.min(1, (Date.now() - e.createdAt) / e.duration)
    if (e.type === 'summon') {
      // Pulse up then settle: 1 → 1.3 → 1
      const s = progress < 0.5
        ? 1 + 0.3 * (progress * 2)
        : 1.3 - 0.3 * ((progress - 0.5) * 2)
      return [s, s, s]
    }
    if (e.type === 'death') {
      // Shrink to 0
      const s = 1 - progress
      return [s, s, s]
    }
  }
  return [1, 1, 1]
}

const CardMesh: React.FC<CardProps> = ({
  entity,
  side,
  zone,
  faceDown = false,
  position,
  rotationY = 0,
  interaction,
  effects,
  onEntityClicked,
  onHoverStart,
  onHoverEnd,
  onPositionReady,
}) => {
  const [hovered, setHovered] = useState(false)
  const { color: emissiveColor, intensity: emissiveIntensity } = getEmissive(
    entity,
    interaction,
    hovered
  )

  // Report position to parent for effect positioning
  const prevPosRef = useRef<string>('')
  useEffect(() => {
    const key = position.join(',')
    if (key !== prevPosRef.current) {
      prevPosRef.current = key
      onPositionReady?.(entity.id, position)
    }
  }, [entity.id, position, onPositionReady])

  // Spring-animate position changes (zone transitions)
  const spring = useSpring({
    pos: position,
    rotY: rotationY,
    config: { tension: 200, friction: 22 },
  })

  const effectScale = getEffectScale(entity.id, effects)

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
    <animated.mesh
      position={spring.pos as any}
      rotation-y={spring.rotY}
      scale={effectScale}
      castShadow
      receiveShadow
      onClick={handleClick}
      onPointerOver={handlePointerOver}
      onPointerOut={handlePointerOut}
    >
      <boxGeometry args={[C.CARD_WIDTH, C.CARD_DEPTH, C.CARD_HEIGHT]} />
      <meshStandardMaterial attach="material-0" color={C.COLOR_CARD_FRONT} emissive={emissiveColor} emissiveIntensity={emissiveIntensity} />
      <meshStandardMaterial attach="material-1" color={C.COLOR_CARD_FRONT} emissive={emissiveColor} emissiveIntensity={emissiveIntensity} />
      <meshStandardMaterial attach="material-2" color={topColor} emissive={emissiveColor} emissiveIntensity={emissiveIntensity} />
      <meshStandardMaterial attach="material-3" color={C.COLOR_CARD_BACK} />
      <meshStandardMaterial attach="material-4" color={C.COLOR_CARD_FRONT} emissive={emissiveColor} emissiveIntensity={emissiveIntensity} />
      <meshStandardMaterial attach="material-5" color={C.COLOR_CARD_FRONT} emissive={emissiveColor} emissiveIntensity={emissiveIntensity} />
      <CardOverlay entity={entity} faceDown={faceDown} />
    </animated.mesh>
  )
}

interface HandProps {
  side: PlayerSide
  hand: Entity[]
  interaction?: InteractionState
  effects?: ActiveEffect[]
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
  onPositionReady?: (entityId: number, pos: Vec3) => void
}

export const Hand: React.FC<HandProps> = ({ side, hand, interaction, effects, onEntityClicked, onHoverStart, onHoverEnd, onPositionReady }) => {
  const faceDown = side === 'top'
  return (
    <group>
      {hand.map((entity, i) => (
        <CardMesh
          key={entity.id}
          entity={entity}
          side={side}
          zone={Zone.Hand}
          index={i}
          faceDown={faceDown}
          position={handPosition(side, i, hand.length)}
          rotationY={handRotation(side, i, hand.length)}
          interaction={interaction}
          effects={effects}
          onEntityClicked={onEntityClicked}
          onHoverStart={onHoverStart}
          onHoverEnd={onHoverEnd}
          onPositionReady={onPositionReady}
        />
      ))}
    </group>
  )
}

interface BattlefieldProps {
  side: PlayerSide
  entities: Entity[]
  interaction?: InteractionState
  effects?: ActiveEffect[]
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
  onPositionReady?: (entityId: number, pos: Vec3) => void
}

export const Battlefield: React.FC<BattlefieldProps> = ({
  side,
  entities,
  interaction,
  effects,
  onEntityClicked,
  onHoverStart,
  onHoverEnd,
  onPositionReady,
}) => (
  <group>
    {entities.map((entity, i) => (
      <CardMesh
        key={entity.id}
        entity={entity}
        side={side}
        zone={Zone.Battlefield}
        index={i}
        position={battlefieldPosition(side, i, entities.length)}
        interaction={interaction}
        effects={effects}
        onEntityClicked={onEntityClicked}
        onHoverStart={onHoverStart}
        onHoverEnd={onHoverEnd}
        onPositionReady={onPositionReady}
      />
    ))}
  </group>
)
