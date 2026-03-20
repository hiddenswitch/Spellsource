import { BehaviorSubject, Observable, Subscription } from 'rxjs'
import {
  type ServerToClientMessage,
  type Entity,
  type GameState,
  type GameActions,
  type GameOver,
  type GameEvent,
  type Timers,
  MessageTypeMessage_MessageType,
  ZonesMessage_Zones,
} from 'spellsource-protos/dist/experimental/client/spellsource'
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
      case ZonesMessage_Zones.HAND:
        side.hand.push(entity)
        break
      case ZonesMessage_Zones.BATTLEFIELD:
        side.battlefield.push(entity)
        break
      case ZonesMessage_Zones.HERO:
        side.hero = entity
        break
      case ZonesMessage_Zones.HERO_POWER:
        side.heroPower = entity
        break
      case ZonesMessage_Zones.WEAPON:
        side.weapon = entity
        break
      case ZonesMessage_Zones.SECRET:
        side.secrets.push(entity)
        break
      case ZonesMessage_Zones.QUEST:
        side.quests.push(entity)
        break
      case ZonesMessage_Zones.DECK:
        side.deck.push(entity)
        break
      case ZonesMessage_Zones.GRAVEYARD:
        side.graveyard.push(entity)
        break
      case ZonesMessage_Zones.PLAYER:
        side.player = entity
        break
      case ZonesMessage_Zones.DISCOVER:
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
 * Processes the ServerToClientMessage stream from the GameClient
 * into a coherent, observable ManagedGameState.
 */
export class GameStateManager {
  private state$ = new BehaviorSubject<ManagedGameState>(createInitialState())
  private subscription: Subscription | null = null

  /** Observable of the current managed game state. */
  readonly managedState$: Observable<ManagedGameState> = this.state$.asObservable()

  /** Get the current state snapshot. */
  get currentState(): ManagedGameState {
    return this.state$.value
  }

  /**
   * Subscribe to an incoming message stream (from GameClient.messages$).
   */
  subscribe(messages$: Observable<ServerToClientMessage>): void {
    this.unsubscribe()
    this.state$.next(createInitialState())
    this.subscription = messages$.subscribe({
      next: (msg) => this.processMessage(msg),
      error: () => {
        // Stream error — keep current state, renderer can show disconnected
      },
      complete: () => {
        // Stream ended normally
      },
    })
  }

  /**
   * Unsubscribe from the message stream.
   */
  unsubscribe(): void {
    this.subscription?.unsubscribe()
    this.subscription = null
  }

  /**
   * Clean up resources.
   */
  dispose(): void {
    this.unsubscribe()
    this.state$.complete()
  }

  private processMessage(msg: ServerToClientMessage): void {
    const current = this.state$.value
    const localPlayerId = msg.localPlayerId || current.localPlayerId

    switch (msg.messageType) {
      case MessageTypeMessage_MessageType.ON_UPDATE:
        this.handleUpdate(msg, localPlayerId)
        break
      case MessageTypeMessage_MessageType.ON_REQUEST_ACTION:
        this.handleRequestAction(msg, localPlayerId)
        break
      case MessageTypeMessage_MessageType.ON_MULLIGAN:
        this.handleMulligan(msg, localPlayerId)
        break
      case MessageTypeMessage_MessageType.ON_GAME_EVENT:
        this.handleGameEvent(msg)
        break
      case MessageTypeMessage_MessageType.ON_GAME_END:
        this.handleGameEnd(msg)
        break
      case MessageTypeMessage_MessageType.TIMER:
        this.handleTimer(msg)
        break
    }
  }

  private handleUpdate(msg: ServerToClientMessage, localPlayerId: number): void {
    const gameState = msg.gameState
    const board = gameState
      ? groupEntities(gameState.entities, localPlayerId)
      : this.state$.value.board

    this.state$.next({
      ...this.state$.value,
      phase: this.state$.value.phase === 'loading' ? 'playing' : this.state$.value.phase,
      gameState,
      board,
      localPlayerId,
      isLocalPlayerTurn: gameState?.isLocalPlayerTurn ?? this.state$.value.isLocalPlayerTurn,
      turnNumber: gameState?.turnNumber ?? this.state$.value.turnNumber,
      timers: msg.timers ?? this.state$.value.timers,
    })
  }

  private handleRequestAction(msg: ServerToClientMessage, localPlayerId: number): void {
    const gameState = msg.gameState
    const board = gameState
      ? groupEntities(gameState.entities, localPlayerId)
      : this.state$.value.board

    this.state$.next({
      ...this.state$.value,
      phase: 'playing',
      gameState: gameState ?? this.state$.value.gameState,
      board,
      actions: msg.actions ?? undefined,
      actionsMessageId: msg.id || undefined,
      localPlayerId,
      isLocalPlayerTurn: gameState?.isLocalPlayerTurn ?? this.state$.value.isLocalPlayerTurn,
      turnNumber: gameState?.turnNumber ?? this.state$.value.turnNumber,
      timers: msg.timers ?? this.state$.value.timers,
    })
  }

  private handleMulligan(msg: ServerToClientMessage, localPlayerId: number): void {
    const gameState = msg.gameState
    const board = gameState
      ? groupEntities(gameState.entities, localPlayerId)
      : this.state$.value.board

    this.state$.next({
      ...this.state$.value,
      phase: 'mulligan',
      gameState: gameState ?? this.state$.value.gameState,
      board,
      mulliganCards: msg.startingCards,
      mulliganMessageId: msg.id || undefined,
      localPlayerId,
      timers: msg.timers ?? this.state$.value.timers,
    })
  }

  private handleGameEvent(msg: ServerToClientMessage): void {
    this.state$.next({
      ...this.state$.value,
      lastEvent: msg.event ?? undefined,
    })
  }

  private handleGameEnd(msg: ServerToClientMessage): void {
    this.state$.next({
      ...this.state$.value,
      phase: 'game_over',
      gameOver: msg.gameOver ?? undefined,
    })
  }

  private handleTimer(msg: ServerToClientMessage): void {
    this.state$.next({
      ...this.state$.value,
      timers: msg.timers ?? this.state$.value.timers,
    })
  }
}
