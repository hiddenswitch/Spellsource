import React, { useCallback } from 'react'
import type { ThreeEvent } from '@react-three/fiber'
import type { Entity } from 'spellsource-protos/dist/experimental/client/spellsource'
import { ZonesMessage_Zones } from 'spellsource-protos/dist/experimental/client/spellsource'
import type { PlayerSide, PlayerEntities } from '../types'
import * as C from './constants'
import { heroPosition, heroPowerPosition, weaponPosition, deckPosition } from './board-layout'

interface HeroPortraitProps {
  entity: Entity
  side: PlayerSide
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
}

const HeroPortrait: React.FC<HeroPortraitProps> = ({ entity, side, onEntityClicked }) => {
  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onEntityClicked?.(entity, side)
    },
    [entity, side, onEntityClicked]
  )

  return (
    <mesh position={heroPosition(side)} castShadow onClick={handleClick} onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer' }} onPointerOut={() => { document.body.style.cursor = 'default' }}>
      <cylinderGeometry args={[0.5, 0.5, 0.05, 32]} />
      <meshStandardMaterial color={C.COLOR_HERO_PORTRAIT} roughness={0.5} metalness={0.3} />
    </mesh>
  )
}

interface HeroPowerProps {
  entity: Entity
  side: PlayerSide
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
}

const HeroPower: React.FC<HeroPowerProps> = ({ entity, side, onEntityClicked }) => {
  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onEntityClicked?.(entity, side)
    },
    [entity, side, onEntityClicked]
  )

  return (
    <mesh position={heroPowerPosition(side)} onClick={handleClick} onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer' }} onPointerOut={() => { document.body.style.cursor = 'default' }}>
      <cylinderGeometry args={[0.3, 0.3, 0.04, 24]} />
      <meshStandardMaterial color={0x665544} roughness={0.6} metalness={0.2} />
    </mesh>
  )
}

interface WeaponProps {
  entity: Entity
  side: PlayerSide
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
}

const Weapon: React.FC<WeaponProps> = ({ entity, side, onEntityClicked }) => {
  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onEntityClicked?.(entity, side)
    },
    [entity, side, onEntityClicked]
  )

  return (
    <mesh position={weaponPosition(side)} onClick={handleClick} onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer' }} onPointerOut={() => { document.body.style.cursor = 'default' }}>
      <cylinderGeometry args={[0.3, 0.3, 0.04, 4]} />
      <meshStandardMaterial color={0x888888} roughness={0.3} metalness={0.7} />
    </mesh>
  )
}

interface DeckPileProps {
  side: PlayerSide
  onBoardClicked?: (side: PlayerSide, zone: ZonesMessage_Zones) => void
}

const DeckPile: React.FC<DeckPileProps> = ({ side, onBoardClicked }) => {
  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation()
      onBoardClicked?.(side, ZonesMessage_Zones.DECK)
    },
    [side, onBoardClicked]
  )

  const pos = deckPosition(side)
  return (
    <mesh position={[pos[0], 0.15, pos[2]]} onClick={handleClick} onPointerOver={(e) => { e.stopPropagation(); document.body.style.cursor = 'pointer' }} onPointerOut={() => { document.body.style.cursor = 'default' }}>
      <boxGeometry args={[C.CARD_WIDTH, 0.3, C.CARD_HEIGHT]} />
      <meshStandardMaterial color={C.COLOR_CARD_BACK} roughness={0.6} />
    </mesh>
  )
}

interface HeroesProps {
  bottom: PlayerEntities
  top: PlayerEntities
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onBoardClicked?: (side: PlayerSide, zone: ZonesMessage_Zones) => void
}

export const Heroes: React.FC<HeroesProps> = ({ bottom, top, onEntityClicked, onBoardClicked }) => (
  <group>
    {([['bottom', bottom], ['top', top]] as const).map(([side, entities]) => (
      <group key={side}>
        {entities.hero && (
          <HeroPortrait entity={entities.hero} side={side} onEntityClicked={onEntityClicked} />
        )}
        {entities.heroPower && (
          <HeroPower entity={entities.heroPower} side={side} onEntityClicked={onEntityClicked} />
        )}
        {entities.weapon && (
          <Weapon entity={entities.weapon} side={side} onEntityClicked={onEntityClicked} />
        )}
        {entities.deck.length > 0 && (
          <DeckPile side={side} onBoardClicked={onBoardClicked} />
        )}
      </group>
    ))}
  </group>
)
