import React from 'react'
import { Text } from '@react-three/drei'
import type { Entity } from '../../../__generated__/client'
import { StatBadge3D, Label3D } from './stat-badge-3d'

/**
 * Hero portrait overlay — 3D badges positioned in the hero mesh's local space.
 * Hero is a cylinder with radius 0.5, height 0.05.
 */
interface HeroOverlayProps {
  entity: Entity
}

export const HeroOverlay: React.FC<HeroOverlayProps> = ({ entity }) => {
  const y = 0.026 // just above cylinder surface
  const r = 0.5   // hero portrait radius
  const showAttack = entity.attack != null && entity.attack > 0
  const hpColor =
    entity.maxHp != null && entity.hp != null && entity.hp < entity.maxHp
      ? '#ff4444' : '#ffffff'

  return (
    <group>
      {/* Attack — bottom left */}
      {showAttack && (
        <StatBadge3D
          value={entity.attack!}
          color="#cc8822"
          position={[r - 0.05, y, -(r - 0.05)]}
          radius={0.12}
        />
      )}

      {/* HP — bottom right */}
      <StatBadge3D
        value={entity.hp ?? 0}
        color="#cc2222"
        textColor={hpColor}
        position={[-(r - 0.05), y, -(r - 0.05)]}
        radius={0.12}
      />

      {/* Armor — above HP */}
      {entity.armor != null && entity.armor > 0 && (
        <StatBadge3D
          value={entity.armor}
          color="#888888"
          position={[-(r - 0.05), y, -(r - 0.3)]}
          radius={0.1}
        />
      )}
    </group>
  )
}

/**
 * Hero power overlay — mana cost + name.
 * Hero power is a cylinder with radius 0.3, height 0.04.
 */
interface HeroPowerOverlayProps {
  entity: Entity
}

export const HeroPowerOverlay: React.FC<HeroPowerOverlayProps> = ({ entity }) => {
  const y = 0.021

  const manaColor =
    entity.baseManaCost != null && entity.manaCost != null && entity.manaCost < entity.baseManaCost
      ? '#44ff44' : '#ffffff'

  return (
    <group>
      {/* Mana cost — top center */}
      {entity.manaCost != null && (
        <StatBadge3D
          value={entity.manaCost}
          color="#2266dd"
          textColor={manaColor}
          position={[0, y, 0.2]}
          radius={0.09}
        />
      )}

      {/* Name — center */}
      <Label3D
        text={entity.name}
        position={[0, y, -0.05]}
        fontSize={0.05}
        maxWidth={0.5}
      />
    </group>
  )
}

/**
 * Weapon overlay — attack + durability.
 * Weapon is a cylinder with radius 0.3, height 0.04, 4 segments (diamond).
 */
interface WeaponOverlayProps {
  entity: Entity
}

export const WeaponOverlay: React.FC<WeaponOverlayProps> = ({ entity }) => {
  const y = 0.021
  const r = 0.3

  return (
    <group>
      {/* Attack — bottom left */}
      {entity.attack != null && (
        <StatBadge3D
          value={entity.attack}
          color="#cc8822"
          position={[r - 0.05, y, -(r - 0.05)]}
          radius={0.09}
        />
      )}

      {/* Durability — bottom right */}
      {entity.durability != null && (
        <StatBadge3D
          value={entity.durability}
          color="#888888"
          position={[-(r - 0.05), y, -(r - 0.05)]}
          radius={0.09}
        />
      )}
    </group>
  )
}

/**
 * Deck count — number displayed on the deck pile.
 */
interface DeckCountOverlayProps {
  count: number
}

export const DeckCountOverlay: React.FC<DeckCountOverlayProps> = ({ count }) => {
  return (
    <Text
      position={[0, 0.16, 0]}
      rotation={[-Math.PI / 2, 0, Math.PI]}
      fontSize={0.2}
      color="#ffffff"
      anchorX="center"
      anchorY="middle"
      fontWeight={700}
      outlineWidth={0.008}
      outlineColor="#000000"
      depthOffset={-1}
    >
      {String(count)}
    </Text>
  )
}

/**
 * Mana text readout — "current/max" displayed near mana crystals.
 */
interface ManaTextOverlayProps {
  mana: number
  maxMana: number
}

export const ManaTextOverlay: React.FC<ManaTextOverlayProps> = ({ mana, maxMana }) => {
  return (
    <Text
      rotation={[-Math.PI / 2, 0, Math.PI]}
      fontSize={0.15}
      color="#88aaff"
      anchorX="center"
      anchorY="middle"
      fontWeight={700}
      outlineWidth={0.005}
      outlineColor="#000000"
      depthOffset={-1}
    >
      {`${mana}/${maxMana}`}
    </Text>
  )
}
