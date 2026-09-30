import type { Entity, ManagedGameState, PlayerEntities } from '../types'
import { Zone, CardType, EntityType } from '../types'
import type { EntityLocation } from '../../__generated__/client'

const ENTITY_DEFAULTS: Omit<Entity, 'id' | 'name' | 'cardId' | 'location' | 'owner' | 'boardPosition'> = {
  battlecry: false,
  cannotAttack: false,
  cardSet: 'CUSTOM',
  cardSets: ['CUSTOM'],
  cardType: CardType.Minion,
  charge: false,
  chooseOne: false,
  collectible: true,
  combo: false,
  conditionMet: false,
  deathrattles: false,
  deflect: false,
  description: '',
  destroyed: false,
  discarded: false,
  divineShield: false,
  enchantmentType: '',
  enraged: false,
  entityType: EntityType.Card,
  frozen: false,
  gameStarted: true,
  gold: false,
  heroClasses: [],
  host: 0,
  hostsTrigger: false,
  immune: false,
  isStartingTurn: false,
  lifesteal: false,
  lockedMana: 0,
  mana: 0,
  maxMana: 0,
  note: '',
  permanent: false,
  playable: false,
  poisonous: false,
  rarity: 'FREE',
  roasted: false,
  rush: false,
  silenced: false,
  stealth: false,
  summoningSickness: false,
  taunt: false,
  tooltips: [],
  tribes: [],
  uncensored: false,
  underAura: false,
  untargetableBySpells: false,
  windfury: false,
}

function entity(
  id: number,
  zone: string,
  player: number,
  index: number,
  overrides: Partial<Entity> = {}
): Entity {
  return {
    ...ENTITY_DEFAULTS,
    id,
    name: `Entity ${id}`,
    cardId: `card_${id}`,
    owner: player,
    boardPosition: index,
    location: { zone: zone as any, player, index } as EntityLocation,
    ...overrides,
  } as Entity
}

export function buildDemoState(): ManagedGameState {
  const LOCAL = 1
  const OPPONENT = 2

  const bottom: PlayerEntities = {
    player: entity(100, Zone.Player, LOCAL, 0, {
      name: 'Player 1',
      entityType: EntityType.Player,
      mana: 7,
      maxMana: 10,
    }),
    hero: entity(1, Zone.Hero, LOCAL, 0, {
      name: 'Jaina Proudmoore',
      cardType: CardType.Hero,
      entityType: EntityType.Hero,
      hp: 28,
      maxHp: 30,
      armor: 0,
    }),
    heroPower: entity(2, Zone.HeroPower, LOCAL, 0, {
      name: 'Fireblast',
      cardType: CardType.HeroPower,
      manaCost: 2,
      description: 'Deal 1 damage.',
      playable: true,
    }),
    weapon: undefined,
    hand: [
      entity(10, Zone.Hand, LOCAL, 0, {
        name: 'Arcane Intellect',
        cardType: CardType.Spell,
        manaCost: 3,
        description: 'Draw 2 cards.',
        playable: true,
      }),
      entity(11, Zone.Hand, LOCAL, 1, {
        name: 'Fireball',
        cardType: CardType.Spell,
        manaCost: 4,
        description: 'Deal 6 damage.',
        playable: true,
        attack: 6,
      }),
      entity(12, Zone.Hand, LOCAL, 2, {
        name: 'Water Elemental',
        manaCost: 4,
        attack: 3,
        hp: 6,
        maxHp: 6,
        description: 'Freeze any character damaged by this minion.',
        playable: true,
      }),
      entity(13, Zone.Hand, LOCAL, 3, {
        name: 'Flamestrike',
        cardType: CardType.Spell,
        manaCost: 7,
        description: 'Deal 4 damage to all enemy minions.',
        playable: true,
      }),
    ],
    battlefield: [
      entity(20, Zone.Battlefield, LOCAL, 0, {
        name: 'Mana Wyrm',
        entityType: EntityType.Minion,
        attack: 3,
        baseAttack: 1,
        hp: 3,
        maxHp: 3,
        manaCost: 1,
        playable: true,
      }),
      entity(21, Zone.Battlefield, LOCAL, 1, {
        name: 'Azure Drake',
        entityType: EntityType.Minion,
        attack: 4,
        hp: 4,
        maxHp: 4,
        manaCost: 5,
        spellDamage: 1,
        description: 'Spell Damage +1. Battlecry: Draw a card.',
        summoningSickness: true,
      }),
    ],
    secrets: [],
    quests: [],
    deck: Array.from({ length: 15 }, (_, i) =>
      entity(50 + i, Zone.Deck, LOCAL, i, { name: 'Card in Deck' })
    ),
    graveyard: [],
    discover: [],
  }

  const top: PlayerEntities = {
    player: entity(200, Zone.Player, OPPONENT, 0, {
      name: 'Player 2',
      entityType: EntityType.Player,
      mana: 6,
      maxMana: 10,
    }),
    hero: entity(3, Zone.Hero, OPPONENT, 0, {
      name: 'Garrosh Hellscream',
      cardType: CardType.Hero,
      entityType: EntityType.Hero,
      hp: 22,
      maxHp: 30,
      armor: 5,
    }),
    heroPower: entity(4, Zone.HeroPower, OPPONENT, 0, {
      name: 'Armor Up!',
      cardType: CardType.HeroPower,
      manaCost: 2,
      description: 'Gain 2 Armor.',
    }),
    weapon: entity(5, Zone.Weapon, OPPONENT, 0, {
      name: "Fiery War Axe",
      cardType: CardType.Weapon,
      entityType: EntityType.Weapon,
      attack: 3,
      durability: 1,
    }),
    hand: [
      entity(30, Zone.Hand, OPPONENT, 0, { name: 'Unknown' }),
      entity(31, Zone.Hand, OPPONENT, 1, { name: 'Unknown' }),
      entity(32, Zone.Hand, OPPONENT, 2, { name: 'Unknown' }),
      entity(33, Zone.Hand, OPPONENT, 3, { name: 'Unknown' }),
    ],
    battlefield: [
      entity(40, Zone.Battlefield, OPPONENT, 0, {
        name: 'Frothing Berserker',
        entityType: EntityType.Minion,
        attack: 5,
        baseAttack: 2,
        hp: 4,
        maxHp: 4,
        manaCost: 3,
        description: 'Whenever a minion takes damage, gain +1 Attack.',
      }),
    ],
    secrets: [],
    quests: [],
    deck: Array.from({ length: 20 }, (_, i) =>
      entity(70 + i, Zone.Deck, OPPONENT, i, { name: 'Card in Deck' })
    ),
    graveyard: [],
    discover: [],
  }

  return {
    phase: 'playing',
    gameState: undefined,
    board: { bottom, top },
    actions: undefined,
    actionsMessageId: undefined,
    mulliganCards: [],
    mulliganMessageId: undefined,
    gameOver: undefined,
    lastEvent: undefined,
    timers: undefined,
    localPlayerId: LOCAL,
    isLocalPlayerTurn: true,
    turnNumber: 5,
  }
}
