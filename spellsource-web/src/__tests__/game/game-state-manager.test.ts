import { Subject } from 'rxjs'
import { GameStateManager, groupEntities } from '../../game/state/game-state-manager'
import {
  type ServerToClientMessage,
  type Entity,
  type EntityLocation,
  MessageTypeMessage_MessageType,
  ZonesMessage_Zones,
  ActionTypeMessage_ActionType,
} from 'spellsource-protos/dist/experimental/client/spellsource'

/** Minimal entity factory */
function makeEntity(id: number, zone: ZonesMessage_Zones, player: number, index = 0): Entity {
  return {
    id,
    location: { zone, player, index } as EntityLocation,
    owner: player,
    name: `entity-${id}`,
    cardId: `card-${id}`,
    // fill defaults for required fields
    battlecry: false,
    boardPosition: index,
    cannotAttack: false,
    cardSet: '',
    cardSets: [],
    cardType: 0,
    charge: false,
    chooseOne: false,
    collectible: false,
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
    entityType: 0,
    frozen: false,
    gameStarted: false,
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
}

function makeServerMessage(overrides: Partial<ServerToClientMessage>): ServerToClientMessage {
  return {
    actions: undefined,
    changes: undefined,
    emote: undefined,
    event: undefined,
    gameOver: undefined,
    gameState: undefined,
    id: '',
    isReplayMessage: false,
    localPlayerId: 1,
    messageType: MessageTypeMessage_MessageType.ON_UPDATE,
    startingCards: [],
    timers: undefined,
    ...overrides,
  }
}

describe('groupEntities', () => {
  it('groups entities by zone and player', () => {
    const entities = [
      makeEntity(1, ZonesMessage_Zones.HERO, 1),
      makeEntity(2, ZonesMessage_Zones.HERO, 2),
      makeEntity(3, ZonesMessage_Zones.HAND, 1, 0),
      makeEntity(4, ZonesMessage_Zones.HAND, 1, 1),
      makeEntity(5, ZonesMessage_Zones.BATTLEFIELD, 1, 0),
      makeEntity(6, ZonesMessage_Zones.BATTLEFIELD, 2, 0),
      makeEntity(7, ZonesMessage_Zones.HERO_POWER, 1),
      makeEntity(8, ZonesMessage_Zones.WEAPON, 2),
      makeEntity(9, ZonesMessage_Zones.SECRET, 2),
      makeEntity(10, ZonesMessage_Zones.DECK, 1),
      makeEntity(11, ZonesMessage_Zones.PLAYER, 1),
      makeEntity(12, ZonesMessage_Zones.PLAYER, 2),
      makeEntity(13, ZonesMessage_Zones.DISCOVER, 1, 0),
    ]

    const board = groupEntities(entities, 1)

    // Bottom (local player 1)
    expect(board.bottom.hero?.id).toBe(1)
    expect(board.bottom.hand).toHaveLength(2)
    expect(board.bottom.hand[0].id).toBe(3)
    expect(board.bottom.hand[1].id).toBe(4)
    expect(board.bottom.battlefield).toHaveLength(1)
    expect(board.bottom.battlefield[0].id).toBe(5)
    expect(board.bottom.heroPower?.id).toBe(7)
    expect(board.bottom.deck).toHaveLength(1)
    expect(board.bottom.player?.id).toBe(11)
    expect(board.bottom.discover).toHaveLength(1)
    expect(board.bottom.discover[0].id).toBe(13)

    // Top (opponent player 2)
    expect(board.top.hero?.id).toBe(2)
    expect(board.top.battlefield).toHaveLength(1)
    expect(board.top.battlefield[0].id).toBe(6)
    expect(board.top.weapon?.id).toBe(8)
    expect(board.top.secrets).toHaveLength(1)
    expect(board.top.player?.id).toBe(12)
  })

  it('sorts hand and battlefield by index', () => {
    const entities = [
      makeEntity(3, ZonesMessage_Zones.HAND, 1, 2),
      makeEntity(1, ZonesMessage_Zones.HAND, 1, 0),
      makeEntity(2, ZonesMessage_Zones.HAND, 1, 1),
    ]
    const board = groupEntities(entities, 1)
    expect(board.bottom.hand.map((e) => e.id)).toEqual([1, 2, 3])
  })
})

describe('GameStateManager', () => {
  let manager: GameStateManager
  let messages$: Subject<ServerToClientMessage>

  beforeEach(() => {
    manager = new GameStateManager()
    messages$ = new Subject()
    manager.subscribe(messages$)
  })

  afterEach(() => {
    manager.dispose()
  })

  it('starts in loading phase', () => {
    expect(manager.currentState.phase).toBe('loading')
    expect(manager.currentState.board).toBeUndefined()
  })

  describe('ON_UPDATE', () => {
    it('updates game state and transitions to playing', () => {
      const entities = [
        makeEntity(1, ZonesMessage_Zones.HERO, 1),
        makeEntity(2, ZonesMessage_Zones.HERO, 2),
      ]

      messages$.next(makeServerMessage({
        messageType: MessageTypeMessage_MessageType.ON_UPDATE,
        localPlayerId: 1,
        gameState: {
          entities,
          isLocalPlayerTurn: true,
          powerHistory: [],
          timestamp: 0,
          turnNumber: 1,
          turnState: '',
          hasPowerHistory: false,
        },
      }))

      const state = manager.currentState
      expect(state.phase).toBe('playing')
      expect(state.localPlayerId).toBe(1)
      expect(state.isLocalPlayerTurn).toBe(true)
      expect(state.turnNumber).toBe(1)
      expect(state.board?.bottom.hero?.id).toBe(1)
      expect(state.board?.top.hero?.id).toBe(2)
    })
  })

  describe('ON_REQUEST_ACTION', () => {
    it('sets available actions and message ID', () => {
      const actions = {
        all: [
          {
            action: 5,
            actionType: ActionTypeMessage_ActionType.END_TURN,
            choices: [],
            description: 'End Turn',
            entity: undefined,
            sourceId: 0,
            targetKeyToActions: [],
            request: '',
          },
        ],
        compatibility: [5],
      }

      messages$.next(makeServerMessage({
        messageType: MessageTypeMessage_MessageType.ON_REQUEST_ACTION,
        localPlayerId: 1,
        id: 'req-123',
        actions,
        gameState: {
          entities: [makeEntity(1, ZonesMessage_Zones.HERO, 1)],
          isLocalPlayerTurn: true,
          powerHistory: [],
          timestamp: 0,
          turnNumber: 2,
          turnState: '',
          hasPowerHistory: false,
        },
      }))

      const state = manager.currentState
      expect(state.phase).toBe('playing')
      expect(state.actions?.all).toHaveLength(1)
      expect(state.actionsMessageId).toBe('req-123')
      expect(state.turnNumber).toBe(2)
    })
  })

  describe('ON_MULLIGAN', () => {
    it('sets mulligan phase with starting cards', () => {
      const startingCards = [
        makeEntity(10, ZonesMessage_Zones.HAND, 1, 0),
        makeEntity(11, ZonesMessage_Zones.HAND, 1, 1),
        makeEntity(12, ZonesMessage_Zones.HAND, 1, 2),
      ]

      messages$.next(makeServerMessage({
        messageType: MessageTypeMessage_MessageType.ON_MULLIGAN,
        localPlayerId: 1,
        id: 'mul-1',
        startingCards,
      }))

      const state = manager.currentState
      expect(state.phase).toBe('mulligan')
      expect(state.mulliganCards).toHaveLength(3)
      expect(state.mulliganMessageId).toBe('mul-1')
    })
  })

  describe('ON_GAME_EVENT', () => {
    it('sets the last event', () => {
      const event = {
        description: 'Minion attacked',
        eventType: 0,
        id: 1,
        isPowerHistory: true,
        isSourcePlayerLocal: true,
        isTargetPlayerLocal: false,
        entityTouched: 0,
        entityUntouched: 0,
        cardEvent: undefined,
        damage: undefined,
        destroy: undefined,
        joust: undefined,
        performedGameAction: undefined,
        source: undefined,
        target: undefined,
        targets: [],
        triggerFired: undefined,
      }

      messages$.next(makeServerMessage({
        messageType: MessageTypeMessage_MessageType.ON_GAME_EVENT,
        event,
      }))

      expect(manager.currentState.lastEvent?.description).toBe('Minion attacked')
    })
  })

  describe('ON_GAME_END', () => {
    it('transitions to game_over with result', () => {
      messages$.next(makeServerMessage({
        messageType: MessageTypeMessage_MessageType.ON_GAME_END,
        gameOver: { localPlayerWon: true, winningPlayerId: 1 },
      }))

      const state = manager.currentState
      expect(state.phase).toBe('game_over')
      expect(state.gameOver?.localPlayerWon).toBe(true)
    })
  })

  describe('observable', () => {
    it('emits state changes to subscribers', () => {
      const states: string[] = []
      manager.managedState$.subscribe((s) => states.push(s.phase))

      messages$.next(makeServerMessage({
        messageType: MessageTypeMessage_MessageType.ON_MULLIGAN,
        id: 'mul-1',
        startingCards: [],
      }))

      messages$.next(makeServerMessage({
        messageType: MessageTypeMessage_MessageType.ON_UPDATE,
        gameState: {
          entities: [],
          isLocalPlayerTurn: true,
          powerHistory: [],
          timestamp: 0,
          turnNumber: 1,
          turnState: '',
          hasPowerHistory: false,
        },
      }))

      // Initial 'loading' + 'mulligan' + 'playing' (ON_UPDATE keeps phase if not loading)
      expect(states).toContain('loading')
      expect(states).toContain('mulligan')
    })
  })

  describe('resubscribe', () => {
    it('resets state when subscribing to a new stream', () => {
      messages$.next(makeServerMessage({
        messageType: MessageTypeMessage_MessageType.ON_GAME_END,
        gameOver: { localPlayerWon: false },
      }))
      expect(manager.currentState.phase).toBe('game_over')

      const newMessages$ = new Subject<ServerToClientMessage>()
      manager.subscribe(newMessages$)
      expect(manager.currentState.phase).toBe('loading')
      expect(manager.currentState.gameOver).toBeUndefined()
    })
  })
})
