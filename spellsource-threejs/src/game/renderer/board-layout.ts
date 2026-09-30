import { MathUtils } from 'three'
import type { PlayerSide } from '../types'
import { Zone } from '../types'
import * as C from './constants'

export type Vec3 = [x: number, y: number, z: number]

/** Get the world position for a card in a player's hand */
export function handPosition(side: PlayerSide, index: number, handSize: number): Vec3 {
  const z = side === 'bottom' ? C.HAND_Z_BOTTOM : C.HAND_Z_TOP
  const totalWidth = (handSize - 1) * C.HAND_CARD_SPACING
  const x = -totalWidth / 2 + index * C.HAND_CARD_SPACING
  return [x, C.HAND_Y_OFFSET, z]
}

/** Get the fan rotation (radians around Y) for a hand card */
export function handRotation(side: PlayerSide, index: number, handSize: number): number {
  const center = (handSize - 1) / 2
  const offset = index - center
  const sign = side === 'bottom' ? 1 : -1
  return MathUtils.degToRad(offset * C.HAND_FAN_ANGLE) * sign
}

/** Get the world position for a minion on the battlefield */
export function battlefieldPosition(side: PlayerSide, index: number, count: number): Vec3 {
  const z = side === 'bottom' ? C.BATTLEFIELD_Z_BOTTOM : C.BATTLEFIELD_Z_TOP
  const totalWidth = (count - 1) * C.BATTLEFIELD_SLOT_SPACING
  const x = -totalWidth / 2 + index * C.BATTLEFIELD_SLOT_SPACING
  return [x, C.BOARD_Y, z]
}

/** Get the world position for a player's hero portrait */
export function heroPosition(side: PlayerSide): Vec3 {
  const z = side === 'bottom' ? C.HERO_Z_BOTTOM : C.HERO_Z_TOP
  return [0, C.BOARD_Y, z]
}

/** Get the world position for a hero power */
export function heroPowerPosition(side: PlayerSide): Vec3 {
  const z = side === 'bottom' ? C.HERO_Z_BOTTOM : C.HERO_Z_TOP
  return [C.HERO_POWER_X_OFFSET, C.BOARD_Y, z]
}

/** Get the world position for a weapon */
export function weaponPosition(side: PlayerSide): Vec3 {
  const z = side === 'bottom' ? C.HERO_Z_BOTTOM : C.HERO_Z_TOP
  return [C.WEAPON_X_OFFSET, C.BOARD_Y, z]
}

/** Get the world position for the deck pile */
export function deckPosition(side: PlayerSide): Vec3 {
  const z = side === 'bottom' ? C.DECK_Z_BOTTOM : C.DECK_Z_TOP
  return [C.DECK_X, C.BOARD_Y, z]
}

/** Get the world position for the mana crystal display */
export function manaPosition(side: PlayerSide): Vec3 {
  const z = side === 'bottom' ? C.MANA_Z_BOTTOM : C.MANA_Z_TOP
  return [C.MANA_X, C.BOARD_Y, z]
}

/** Get the world position for the end turn button */
export function endTurnPosition(): Vec3 {
  return [C.END_TURN_X, C.BOARD_Y + 0.05, C.END_TURN_Z]
}

/** Build a full set of slot positions from a given game state shape */
export function computeSlots(
  bottomHandSize: number,
  topHandSize: number,
  bottomFieldSize: number,
  topFieldSize: number
): { zone: Zone; side: PlayerSide; index: number; worldPos: Vec3 }[] {
  const slots: { zone: Zone; side: PlayerSide; index: number; worldPos: Vec3 }[] = []

  const addSlots = (
    side: PlayerSide,
    zone: Zone,
    count: number,
    posFn: (i: number, n: number) => Vec3
  ) => {
    for (let i = 0; i < count; i++) {
      slots.push({ zone, side, index: i, worldPos: posFn(i, count) })
    }
  }

  addSlots('bottom', Zone.Hand, bottomHandSize, (i, n) => handPosition('bottom', i, n))
  addSlots('top', Zone.Hand, topHandSize, (i, n) => handPosition('top', i, n))
  addSlots('bottom', Zone.Battlefield, bottomFieldSize, (i, n) => battlefieldPosition('bottom', i, n))
  addSlots('top', Zone.Battlefield, topFieldSize, (i, n) => battlefieldPosition('top', i, n))

  for (const side of ['bottom', 'top'] as PlayerSide[]) {
    slots.push({ zone: Zone.Hero, side, index: 0, worldPos: heroPosition(side) })
    slots.push({ zone: Zone.HeroPower, side, index: 0, worldPos: heroPowerPosition(side) })
    slots.push({ zone: Zone.Weapon, side, index: 0, worldPos: weaponPosition(side) })
    slots.push({ zone: Zone.Deck, side, index: 0, worldPos: deckPosition(side) })
  }

  return slots
}
