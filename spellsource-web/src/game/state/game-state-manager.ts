import type {
  Entity,
  ServerGameMessage,
  GameState,
  GameActions,
  GameOver,
  GameEvent,
  Timers,
} from '../../__generated__/client'
import { Zone, MessageType } from '../../__generated__/client'
import type { ManagedGameState, GroupedBoard, PlayerEntities, GamePhase } from '../types'

function emptyPlayerEntities(): PlayerEntities {
  return {
    player: undefined,
    hero: undefined,
    heroPower: undefined,
    weapon: undefined,
    hand: [],
    battlefield: [],
    secrets: [],
    quests: [],
    deck: [],
    graveyard: [],
    discover: [],
  }
}

function createInitialState(): ManagedGameState {
  return {
    phase: 'loading',
    gameState: undefined,
    board: undefined,
    actions: undefined,
    actionsMessageId: undefined,
    mulliganCards: [],
    mulliganMessageId: undefined,
    gameOver: undefined,
    lastEvent: undefined,
    timers: undefined,
    localPlayerId: 0,
    isLocalPlayerTurn: false,
    turnNumber: 0,
  }
}

/**
 * Group a flat entity array into two sides (local player = bottom, opponent = top),
 * organized by zone.
 */
export function groupEntities(entities: Entity[], localPlayerId: number): GroupedBoard {
  const bottom = emptyPlayerEntities()
  const top = emptyPlayerEntities()

  for (const entity of entities) {
    if (!entity.location) continue

    const isLocal = entity.location.player === localPlayerId
      || entity.owner === localPlayerId
    const side = isLocal ? bottom : top
    const zone = entity.location.zone

    switch (zone) {
      case Zone.Hand:
        side.hand.push(entity)
        break
      case Zone.Battlefield:
        side.battlefield.push(entity)
        break
      case Zone.Hero:
        side.hero = entity
        break
      case Zone.HeroPower:
        side.heroPower = entity
        break
      case Zone.Weapon:
        side.weapon = entity
        break
      case Zone.Secret:
        side.secrets.push(entity)
        break
      case Zone.Quest:
        side.quests.push(entity)
        break
      case Zone.Deck:
        side.deck.push(entity)
        break
      case Zone.Graveyard:
        side.graveyard.push(entity)
        break
      case Zone.Player:
        side.player = entity
        break
      case Zone.Discover:
        side.discover.push(entity)
        break
    }
  }

  // Sort entities within zones by their board position / index
  const sortByIndex = (a: Entity, b: Entity) =>
    (a.location?.index ?? 0) - (b.location?.index ?? 0)

  bottom.hand.sort(sortByIndex)
  bottom.battlefield.sort(sortByIndex)
  top.hand.sort(sortByIndex)
  top.battlefield.sort(sortByIndex)

  return { bottom, top }
}

/**
 * Processes ServerGameMessage from the GraphQL subscription
 * into a coherent ManagedGameState.
 *
 * Unlike the previous RxJS-based version, this is now a simple reducer
 * that can be driven by React state (useReducer) or called imperatively.
 */
export function reduceGameMessage(current: ManagedGameState, msg: ServerGameMessage): ManagedGameState {
  const localPlayerId = msg.localPlayerId || current.localPlayerId

  switch (msg.messageType) {
    case MessageType.OnUpdate: {
      const gameState = msg.gameState ?? undefined
      const board = gameState
        ? groupEntities(gameState.entities, localPlayerId)
        : current.board

      return {
        ...current,
        phase: current.phase === 'loading' ? 'playing' : current.phase,
        gameState,
        board,
        localPlayerId,
        isLocalPlayerTurn: gameState?.isLocalPlayerTurn ?? current.isLocalPlayerTurn,
        turnNumber: gameState?.turnNumber ?? current.turnNumber,
        timers: msg.timers ?? current.timers,
      }
    }

    case MessageType.OnRequestAction: {
      const gameState = msg.gameState ?? undefined
      const board = gameState
        ? groupEntities(gameState.entities, localPlayerId)
        : current.board

      return {
        ...current,
        phase: 'playing',
        gameState: gameState ?? current.gameState,
        board,
        actions: msg.actions ?? undefined,
        actionsMessageId: msg.id ?? undefined,
        localPlayerId,
        isLocalPlayerTurn: gameState?.isLocalPlayerTurn ?? current.isLocalPlayerTurn,
        turnNumber: gameState?.turnNumber ?? current.turnNumber,
        timers: msg.timers ?? current.timers,
      }
    }

    case MessageType.OnMulligan: {
      const gameState = msg.gameState ?? undefined
      const board = gameState
        ? groupEntities(gameState.entities, localPlayerId)
        : current.board

      return {
        ...current,
        phase: 'mulligan',
        gameState: gameState ?? current.gameState,
        board,
        mulliganCards: msg.startingCards ?? [],
        mulliganMessageId: msg.id ?? undefined,
        localPlayerId,
        timers: msg.timers ?? current.timers,
      }
    }

    case MessageType.OnGameEvent:
      return {
        ...current,
        lastEvent: msg.event ?? undefined,
      }

    case MessageType.OnGameEnd:
      return {
        ...current,
        phase: 'game_over',
        gameOver: msg.gameOver ?? undefined,
      }

    case MessageType.Timer:
      return {
        ...current,
        timers: msg.timers ?? current.timers,
      }

    default:
      return current
  }
}

/** Create a fresh initial state for a new game. */
export { createInitialState }
