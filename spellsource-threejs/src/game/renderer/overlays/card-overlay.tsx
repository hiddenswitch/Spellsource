import React from 'react'
import type { Entity } from '../../../__generated__/client'
import { CardType } from '../../../__generated__/client'
import { StatBadge3D, Label3D } from './stat-badge-3d'
import * as C from '../constants'

interface CardOverlayProps {
  entity: Entity
  faceDown?: boolean
}

/**
 * 3D overlay rendered as children of the card mesh.
 * Coordinates are in the card's local space (box: CARD_WIDTH x CARD_DEPTH x CARD_HEIGHT).
 * The card box: X = width, Y = thickness, Z = height.
 * From the camera: +Z = screen-top, -Z = screen-bottom, -X = screen-left, +X = screen-right.
 */
export const CardOverlay: React.FC<CardOverlayProps> = ({ entity, faceDown }) => {
  if (faceDown) return null

  const hw = C.CARD_WIDTH / 2   // half width (X)
  const hh = C.CARD_HEIGHT / 2  // half height (Z)
  const y = C.CARD_DEPTH / 2 + 0.001  // just above the card surface

  const isMinion = entity.cardType === CardType.Minion
  const isWeapon = entity.cardType === CardType.Weapon
  const showAttack = entity.attack != null && (isMinion || isWeapon || entity.attack > 0)
  const showHp = entity.hp != null && isMinion
  const showDurability = entity.durability != null && isWeapon

  const attackColor =
    entity.baseAttack != null && entity.attack != null && entity.attack > entity.baseAttack
      ? '#44ff44' : '#ffffff'
  const hpColor =
    entity.maxHp != null && entity.hp != null && entity.hp < entity.maxHp
      ? '#ff4444' : '#ffffff'
  const manaColor =
    entity.baseManaCost != null && entity.manaCost != null && entity.manaCost < entity.baseManaCost
      ? '#44ff44' : '#ffffff'

  return (
    <group>
      {/* Mana cost — top left */}
      {entity.manaCost != null && (
        <StatBadge3D
          value={entity.manaCost}
          color="#2266dd"
          textColor={manaColor}
          position={[hw - 0.1, y, hh - 0.1]}
        />
      )}

      {/* Card name — center upper area */}
      <Label3D
        text={entity.name}
        position={[0, y, hh * 0.3]}
        fontSize={0.07}
        maxWidth={C.CARD_WIDTH - 0.2}
        bgWidth={C.CARD_WIDTH - 0.05}
        bgHeight={0.12}
      />

      {/* Attack — bottom left */}
      {showAttack && (
        <StatBadge3D
          value={entity.attack!}
          color="#cc8822"
          textColor={attackColor}
          position={[hw - 0.1, y, -hh + 0.1]}
        />
      )}

      {/* HP — bottom right (minions) */}
      {showHp && (
        <StatBadge3D
          value={entity.hp!}
          color="#cc2222"
          textColor={hpColor}
          position={[-hw + 0.1, y, -hh + 0.1]}
        />
      )}

      {/* Durability — bottom right (weapons) */}
      {showDurability && (
        <StatBadge3D
          value={entity.durability!}
          color="#888888"
          position={[-hw + 0.1, y, -hh + 0.1]}
        />
      )}
    </group>
  )
}
