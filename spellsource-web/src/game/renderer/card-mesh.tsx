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
import { handPosition, handRotation, battlefieldPosition, deckPosition } from './board-layout'
import { CardOverlay } from './overlays/card-overlay'
import { useEntityPositionStore } from './entity-positions'

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
      const s = progress < 0.5
        ? 1 + 0.3 * (progress * 2)
        : 1.3 - 0.3 * ((progress - 0.5) * 2)
      return [s, s, s]
    }
    if (e.type === 'death') {
      const s = 1 - progress
      return [s, s, s]
    }
  }
  return [1, 1, 1]
}

/** Find active attack effect for this entity and compute lunge target offset */
function getAttackLungeTarget(
  entityId: number,
  basePos: Vec3,
  effects: ActiveEffect[] | undefined,
  entityStore: { entities: Map<number, { pos: Vec3 }> }
): Vec3 | null {
  if (!effects) return null
  for (const e of effects) {
    if (e.type !== 'attack' || e.entityId !== entityId || !e.targetEntityId) continue
    const targetRec = entityStore.entities.get(e.targetEntityId)
    if (!targetRec) continue
    const tp = targetRec.pos
    // Lunge to 90% of the way to the target (stop just short of overlapping)
    return [
      basePos[0] + (tp[0] - basePos[0]) * 0.9,
      basePos[1],
      basePos[2] + (tp[2] - basePos[2]) * 0.9,
    ]
  }
  return null
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

  const storeRef = useEntityPositionStore()

  // Determine the starting "from" position for the spring animation.
  // Cards from the initial deal (before any game actions) start in place — no animation.
  const initialFrom = useRef<Vec3 | null>(null)
  if (initialFrom.current === null) {
    const store = storeRef.current

    if (!store.initialRenderComplete) {
      // Initial board — no entrance animation
      initialFrom.current = position
    } else {
      const prev = store.entities.get(entity.id)
      if (prev) {
        initialFrom.current = prev.pos
      } else if (zone === Zone.Hand) {
        initialFrom.current = deckPosition(side)
      } else if (zone === Zone.Battlefield) {
        // Pop the oldest removed hand position for this side
        const queue = store.removedHandPositions[side]
        const fromHand = queue.length > 0 ? queue.shift()! : null
        initialFrom.current = fromHand ?? handPosition(side, 0, 1)
      } else {
        initialFrom.current = position
      }
    }
  }

  // Record position and track hand removals
  useEffect(() => {
    const store = storeRef.current
    store.entities.set(entity.id, { pos: position, zone })
    // Mark initial render complete after the first batch of cards register
    if (!store.initialRenderComplete) {
      store.initialRenderComplete = true
    }
    onPositionReady?.(entity.id, position)

    return () => {
      if (zone === Zone.Hand) {
        storeRef.current.removedHandPositions[side].push(position)
      }
    }
  }, [entity.id, position, zone, side, storeRef, onPositionReady])

  // Attack lunge: detect active attack effect and compute lunge target
  const lungeTarget = getAttackLungeTarget(entity.id, position, effects, storeRef.current)
  const prevLungeRef = useRef<boolean>(false)
  const isLunging = lungeTarget !== null

  // Track lunge state transitions for the spring
  const [lungePhase, setLungePhase] = useState<'idle' | 'forward' | 'back'>('idle')

  useEffect(() => {
    if (isLunging && !prevLungeRef.current) {
      // Attack just started — lunge forward
      setLungePhase('forward')
      // After reaching the target, snap back
      const timer = setTimeout(() => setLungePhase('back'), 200)
      return () => clearTimeout(timer)
    }
    if (!isLunging && prevLungeRef.current) {
      setLungePhase('idle')
    }
    prevLungeRef.current = isLunging
  }, [isLunging])

  // Compute the effective spring target
  const effectivePos: Vec3 =
    lungePhase === 'forward' && lungeTarget
      ? lungeTarget
      : position

  // Base position spring
  const spring = useSpring({
    pos: effectivePos,
    rotY: rotationY,
    from: { pos: initialFrom.current!, rotY: rotationY },
    config: lungePhase === 'forward'
      ? { tension: 400, friction: 18 }  // fast snap forward
      : lungePhase === 'back'
        ? { tension: 300, friction: 22 }  // brisk return
        : { tension: 170, friction: 24 }, // normal movement
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
