import React, { useState, useCallback, type FunctionComponent } from 'react'
import dynamic from 'next/dynamic'
import Head from 'next/head'
import type { Entity, ManagedGameState, PlayerSide } from '../game/types'
import {
  ZonesMessage_Zones,
  CardTypeMessage_CardType,
  EntityTypeMessage_EntityType,
} from 'spellsource-protos/dist/experimental/client/spellsource'
import type { EntityLocation, PlayerEntities } from '../game/types'

// Load GameScene client-side only — Three.js requires the DOM
const GameScene = dynamic(() => import('../game/renderer/game-scene').then((m) => m.GameScene), {
  ssr: false,
})

// ── Demo entity helpers ─────────────────────────────────────

const ENTITY_DEFAULTS: Omit<Entity, 'id' | 'name' | 'cardId' | 'location' | 'owner' | 'boardPosition'> = {
  battlecry: false,
  cannotAttack: false,
  cardSet: 'CUSTOM',
  cardSets: ['CUSTOM'],
  cardType: CardTypeMessage_CardType.MINION,
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
  entityType: EntityTypeMessage_EntityType.CARD,
  frozen: false,
  gameStarted: true,
  gold: false,
  heroClasses: [],
  host: 0,
  hostsTrigger: false,
  immune: false,
  isStartingTurn: false,
  art: undefined,
  lifesteal: false,
  lockedMana: 0,
  mana: 0,
  maxMana: 0,
  note: '',
  permanent: false,
  playable: false,
  poisonous: false,
  rarity: 0,
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
  zone: ZonesMessage_Zones,
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
    location: { zone, player, index } as EntityLocation,
    ...overrides,
  }
}

// ── Build demo board ────────────────────────────────────────

function buildDemoState(): ManagedGameState {
  const LOCAL = 1
  const OPPONENT = 2

  const bottom: PlayerEntities = {
    player: entity(100, ZonesMessage_Zones.PLAYER, LOCAL, 0, {
      name: 'Player 1',
      entityType: EntityTypeMessage_EntityType.PLAYER,
      mana: 7,
      maxMana: 10,
    }),
    hero: entity(1, ZonesMessage_Zones.HERO, LOCAL, 0, {
      name: 'Jaina Proudmoore',
      cardType: CardTypeMessage_CardType.HERO,
      entityType: EntityTypeMessage_EntityType.HERO,
      hp: 28,
      maxHp: 30,
      armor: 0,
    }),
    heroPower: entity(2, ZonesMessage_Zones.HERO_POWER, LOCAL, 0, {
      name: 'Fireblast',
      cardType: CardTypeMessage_CardType.HERO_POWER,
      manaCost: 2,
      description: 'Deal 1 damage.',
      playable: true,
    }),
    weapon: undefined,
    hand: [
      entity(10, ZonesMessage_Zones.HAND, LOCAL, 0, {
        name: 'Arcane Intellect',
        cardType: CardTypeMessage_CardType.SPELL,
        manaCost: 3,
        description: 'Draw 2 cards.',
        playable: true,
      }),
      entity(11, ZonesMessage_Zones.HAND, LOCAL, 1, {
        name: 'Fireball',
        cardType: CardTypeMessage_CardType.SPELL,
        manaCost: 4,
        description: 'Deal 6 damage.',
        playable: true,
        attack: 6,
      }),
      entity(12, ZonesMessage_Zones.HAND, LOCAL, 2, {
        name: 'Water Elemental',
        manaCost: 4,
        attack: 3,
        hp: 6,
        maxHp: 6,
        description: 'Freeze any character damaged by this minion.',
        playable: true,
      }),
      entity(13, ZonesMessage_Zones.HAND, LOCAL, 3, {
        name: 'Flamestrike',
        cardType: CardTypeMessage_CardType.SPELL,
        manaCost: 7,
        description: 'Deal 4 damage to all enemy minions.',
        playable: true,
      }),
    ],
    battlefield: [
      entity(20, ZonesMessage_Zones.BATTLEFIELD, LOCAL, 0, {
        name: 'Mana Wyrm',
        entityType: EntityTypeMessage_EntityType.MINION,
        attack: 3,
        baseAttack: 1,
        hp: 3,
        maxHp: 3,
        manaCost: 1,
        playable: true,
      }),
      entity(21, ZonesMessage_Zones.BATTLEFIELD, LOCAL, 1, {
        name: 'Azure Drake',
        entityType: EntityTypeMessage_EntityType.MINION,
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
      entity(50 + i, ZonesMessage_Zones.DECK, LOCAL, i, { name: 'Card in Deck' })
    ),
    graveyard: [],
    discover: [],
  }

  const top: PlayerEntities = {
    player: entity(200, ZonesMessage_Zones.PLAYER, OPPONENT, 0, {
      name: 'Player 2',
      entityType: EntityTypeMessage_EntityType.PLAYER,
      mana: 6,
      maxMana: 10,
    }),
    hero: entity(3, ZonesMessage_Zones.HERO, OPPONENT, 0, {
      name: 'Garrosh Hellscream',
      cardType: CardTypeMessage_CardType.HERO,
      entityType: EntityTypeMessage_EntityType.HERO,
      hp: 22,
      maxHp: 30,
      armor: 5,
    }),
    heroPower: entity(4, ZonesMessage_Zones.HERO_POWER, OPPONENT, 0, {
      name: 'Armor Up!',
      cardType: CardTypeMessage_CardType.HERO_POWER,
      manaCost: 2,
      description: 'Gain 2 Armor.',
    }),
    weapon: entity(5, ZonesMessage_Zones.WEAPON, OPPONENT, 0, {
      name: "Fiery War Axe",
      cardType: CardTypeMessage_CardType.WEAPON,
      entityType: EntityTypeMessage_EntityType.WEAPON,
      attack: 3,
      durability: 1,
    }),
    hand: [
      entity(30, ZonesMessage_Zones.HAND, OPPONENT, 0, { name: 'Unknown' }),
      entity(31, ZonesMessage_Zones.HAND, OPPONENT, 1, { name: 'Unknown' }),
      entity(32, ZonesMessage_Zones.HAND, OPPONENT, 2, { name: 'Unknown' }),
      entity(33, ZonesMessage_Zones.HAND, OPPONENT, 3, { name: 'Unknown' }),
    ],
    battlefield: [
      entity(40, ZonesMessage_Zones.BATTLEFIELD, OPPONENT, 0, {
        name: 'Frothing Berserker',
        entityType: EntityTypeMessage_EntityType.MINION,
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
      entity(70 + i, ZonesMessage_Zones.DECK, OPPONENT, i, { name: 'Card in Deck' })
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

// ── Page component ──────────────────────────────────────────

const GamePage: FunctionComponent = () => {
  const [state] = useState<ManagedGameState>(buildDemoState)

  const onEntityClicked = useCallback((clickedEntity: Entity, side: PlayerSide) => {
    console.log('Entity clicked:', clickedEntity.name, `(id=${clickedEntity.id})`, side)
  }, [])

  const onEndTurnClicked = useCallback(() => {
    console.log('End turn clicked')
  }, [])

  return (
    <>
      <Head>
        <title>Spellsource — Game</title>
      </Head>
      <div style={{ width: '100vw', height: '100vh', background: '#000', position: 'relative' }}>
        <GameScene
          state={state}
          onEntityClicked={onEntityClicked}
          onEndTurnClicked={onEndTurnClicked}
        />
        <div style={{
          position: 'absolute',
          top: 8,
          left: 8,
          color: '#aaa',
          fontFamily: 'monospace',
          fontSize: 12,
          pointerEvents: 'none',
        }}>
          Demo mode — Turn {state.turnNumber}
        </div>
      </div>
    </>
  )
}

export default GamePage
