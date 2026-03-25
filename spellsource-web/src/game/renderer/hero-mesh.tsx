import React, { useCallback, useEffect, useRef } from 'react'
import { useSpring, animated } from '@react-spring/three'
import type { ThreeEvent } from '@react-three/fiber'
import type { Entity } from '../../__generated__/client'
import { Zone } from '../../__generated__/client'
import type { PlayerSide, PlayerEntities } from '../types'
import type { InteractionState } from '../state/interaction-state'
import type { ActiveEffect } from '../hooks/use-animation-queue'
import * as C from './constants'
import type { Vec3 } from './board-layout'
import { heroPosition, heroPowerPosition, weaponPosition, deckPosition } from './board-layout'
import { HeroOverlay, HeroPowerOverlay, WeaponOverlay, DeckCountOverlay } from './overlays/hero-overlay'

function getEmissive(
  entityId: number,
  interaction: InteractionState | undefined,
  hovered: boolean
): [number, number] {
  if (!interaction) return [0x000000, 0]
  if (interaction.selectedSource?.id === entityId) return [0x44ff44, 0.6]
  if (interaction.phase === 'awaiting_target' && interaction.validTargets.includes(entityId))
    return [0xffcc00, hovered ? 0.6 : 0.4]
  if (interaction.playableEntityIds.includes(entityId))
    return [0x44aa44, hovered ? 0.4 : 0.2]
  return [0x000000, 0]
}

/** Check if this entity has an active attack effect and compute lunge offset */
function getAttackOffset(
  entityId: number,
  effects: ActiveEffect[] | undefined,
  entityPositions: Map<number, Vec3> | undefined,
  basePos: Vec3
): Vec3 {
  if (!effects || !entityPositions) return basePos
  for (const e of effects) {
    if (e.type !== 'attack' || e.entityId !== entityId || !e.targetEntityId) continue
    const targetPos = entityPositions.get(e.targetEntityId)
    if (!targetPos) continue

    const progress = Math.min(1, (Date.now() - e.createdAt) / e.duration)
    const t = progress < 0.6
      ? progress / 0.6
      : 1 - (progress - 0.6) / 0.4
    const ease = t * t * (3 - 2 * t)

    return [
      basePos[0] + (targetPos[0] - basePos[0]) * ease * 0.5,
      basePos[1],
      basePos[2] + (targetPos[2] - basePos[2]) * ease * 0.5,
    ]
  }
  return basePos
}

interface HeroPortraitProps {
  entity: Entity
  side: PlayerSide
  interaction?: InteractionState
  effects?: ActiveEffect[]
  entityPositions?: Map<number, Vec3>
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
  onPositionReady?: (entityId: number, pos: Vec3) => void
}

const HeroPortrait: React.FC<HeroPortraitProps> = ({ entity, side, interaction, effects, entityPositions, onEntityClicked, onHoverStart, onHoverEnd, onPositionReady }) => {
  const [emissive, emissiveIntensity] = getEmissive(entity.id, interaction, false)
  const basePos = heroPosition(side)

  const reportedRef = useRef(false)
  useEffect(() => {
    if (!reportedRef.current) {
      reportedRef.current = true
      onPositionReady?.(entity.id, basePos)
    }
  }, [entity.id, basePos, onPositionReady])

  const pos = getAttackOffset(entity.id, effects, entityPositions, basePos)

  const spring = useSpring({
    pos,
    config: { tension: 300, friction: 20 },
  })

  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onEntityClicked?.(entity, side)
    },
    [entity, side, onEntityClicked]
  )

  return (
    <animated.mesh
      position={spring.pos as any}
      castShadow
      onClick={handleClick}
      onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer'; onHoverStart?.(entity) }}
      onPointerOut={() => { document.body.style.cursor = 'default'; onHoverEnd?.() }}
    >
      <cylinderGeometry args={[0.5, 0.5, 0.05, 32]} />
      <meshStandardMaterial color={C.COLOR_HERO_PORTRAIT} roughness={0.5} metalness={0.3} emissive={emissive} emissiveIntensity={emissiveIntensity} />
      <HeroOverlay entity={entity} />
    </animated.mesh>
  )
}

interface HeroPowerProps {
  entity: Entity
  side: PlayerSide
  interaction?: InteractionState
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
  onPositionReady?: (entityId: number, pos: Vec3) => void
}

const HeroPower: React.FC<HeroPowerProps> = ({ entity, side, interaction, onEntityClicked, onHoverStart, onHoverEnd, onPositionReady }) => {
  const [emissive, emissiveIntensity] = getEmissive(entity.id, interaction, false)
  const pos = heroPowerPosition(side)

  const reportedRef = useRef(false)
  useEffect(() => {
    if (!reportedRef.current) {
      reportedRef.current = true
      onPositionReady?.(entity.id, pos)
    }
  }, [entity.id, pos, onPositionReady])

  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onEntityClicked?.(entity, side)
    },
    [entity, side, onEntityClicked]
  )

  return (
    <mesh
      position={pos}
      onClick={handleClick}
      onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer'; onHoverStart?.(entity) }}
      onPointerOut={() => { document.body.style.cursor = 'default'; onHoverEnd?.() }}
    >
      <cylinderGeometry args={[0.3, 0.3, 0.04, 24]} />
      <meshStandardMaterial color={0x665544} roughness={0.6} metalness={0.2} emissive={emissive} emissiveIntensity={emissiveIntensity} />
      <HeroPowerOverlay entity={entity} />
    </mesh>
  )
}

interface WeaponProps {
  entity: Entity
  side: PlayerSide
  interaction?: InteractionState
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
  onPositionReady?: (entityId: number, pos: Vec3) => void
}

const Weapon: React.FC<WeaponProps> = ({ entity, side, interaction, onEntityClicked, onHoverStart, onHoverEnd, onPositionReady }) => {
  const [emissive, emissiveIntensity] = getEmissive(entity.id, interaction, false)
  const pos = weaponPosition(side)

  const reportedRef = useRef(false)
  useEffect(() => {
    if (!reportedRef.current) {
      reportedRef.current = true
      onPositionReady?.(entity.id, pos)
    }
  }, [entity.id, pos, onPositionReady])

  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onEntityClicked?.(entity, side)
    },
    [entity, side, onEntityClicked]
  )

  return (
    <mesh
      position={pos}
      onClick={handleClick}
      onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer'; onHoverStart?.(entity) }}
      onPointerOut={() => { document.body.style.cursor = 'default'; onHoverEnd?.() }}
    >
      <cylinderGeometry args={[0.3, 0.3, 0.04, 4]} />
      <meshStandardMaterial color={0x888888} roughness={0.3} metalness={0.7} emissive={emissive} emissiveIntensity={emissiveIntensity} />
      <WeaponOverlay entity={entity} />
    </mesh>
  )
}

interface DeckPileProps {
  side: PlayerSide
  count: number
  onBoardClicked?: (side: PlayerSide, zone: typeof Zone.Deck) => void
}

const DeckPile: React.FC<DeckPileProps> = ({ side, count, onBoardClicked }) => {
  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onBoardClicked?.(side, Zone.Deck)
    },
    [side, onBoardClicked]
  )

  const pos = deckPosition(side)
  return (
    <mesh position={[pos[0], 0.15, pos[2]]} onClick={handleClick} onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer' }} onPointerOut={() => { document.body.style.cursor = 'default' }}>
      <boxGeometry args={[C.CARD_WIDTH, 0.3, C.CARD_HEIGHT]} />
      <meshStandardMaterial color={C.COLOR_CARD_BACK} roughness={0.6} />
      <DeckCountOverlay count={count} />
    </mesh>
  )
}

interface HeroesProps {
  bottom: PlayerEntities
  top: PlayerEntities
  interaction?: InteractionState
  effects?: ActiveEffect[]
  entityPositions?: Map<number, Vec3>
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onBoardClicked?: (side: PlayerSide, zone: typeof Zone.Deck) => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
  onPositionReady?: (entityId: number, pos: Vec3) => void
}

export const Heroes: React.FC<HeroesProps> = ({ bottom, top, interaction, effects, entityPositions, onEntityClicked, onBoardClicked, onHoverStart, onHoverEnd, onPositionReady }) => (
  <group>
    {([['bottom', bottom], ['top', top]] as const).map(([side, entities]) => (
      <group key={side}>
        {entities.hero && (
          <HeroPortrait entity={entities.hero} side={side} interaction={interaction} effects={effects} entityPositions={entityPositions} onEntityClicked={onEntityClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} onPositionReady={onPositionReady} />
        )}
        {entities.heroPower && (
          <HeroPower entity={entities.heroPower} side={side} interaction={interaction} onEntityClicked={onEntityClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} onPositionReady={onPositionReady} />
        )}
        {entities.weapon && (
          <Weapon entity={entities.weapon} side={side} interaction={interaction} onEntityClicked={onEntityClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} onPositionReady={onPositionReady} />
        )}
        {entities.deck.length > 0 && (
          <DeckPile side={side} count={entities.deck.length} onBoardClicked={onBoardClicked} />
        )}
      </group>
    ))}
  </group>
)
