/**
 * Shared types bridging the gRPC client layer, state management, and renderer.
 *
 * The proto-generated types (Entity, GameState, SpellAction, etc.) are the
 * canonical game data types. This module re-exports them and adds thin
 * helper types for the renderer and UI layers.
 */

import type {
  Entity as ProtoEntity,
  GameState as ProtoGameState,
  GameActions as ProtoGameActions,
  GameEvent as ProtoGameEvent,
  GameOver as ProtoGameOver,
  Timers as ProtoTimers,
  ZonesMessage_Zones,
} from 'spellsource-protos/dist/experimental/client/spellsource'

// Re-export proto types that the rest of the game module uses
export type {
  Entity,
  EntityLocation,
  GameState,
  GameActions,
  GameEvent,
  GameOver,
  SpellAction,
  TargetActionPair,
  ServerToClientMessage,
  ClientToServerMessage,
  Timers,
  EntityChangeSet,
  Emote,
  Art,
} from 'spellsource-protos/dist/experimental/client/spellsource'

export {
  ZonesMessage_Zones as Zone,
  EntityTypeMessage_EntityType as EntityType,
  CardTypeMessage_CardType as CardType,
  ActionTypeMessage_ActionType as ActionType,
  MessageTypeMessage_MessageType as MessageType,
} from 'spellsource-protos/dist/experimental/client/spellsource'

/** Which side of the board a player is on from the local player's perspective */
export type PlayerSide = 'top' | 'bottom'

/** Entities grouped by zone for one player */
export interface PlayerEntities {
  /** The player entity (mana, game state flags) */
  player: ProtoEntity | undefined
  hero: ProtoEntity | undefined
  heroPower: ProtoEntity | undefined
  weapon: ProtoEntity | undefined
  hand: ProtoEntity[]
  battlefield: ProtoEntity[]
  secrets: ProtoEntity[]
  quests: ProtoEntity[]
  deck: ProtoEntity[]
  graveyard: ProtoEntity[]
  discover: ProtoEntity[]
}

/** The full board state grouped for rendering */
export interface GroupedBoard {
  bottom: PlayerEntities
  top: PlayerEntities
}

/**
 * The current game phase as seen by the state manager.
 */
export type GamePhase = 'loading' | 'mulligan' | 'playing' | 'game_over'

/**
 * Snapshot of game state for the renderer and UI.
 * Built from ServerToClientMessage stream by GameStateManager.
 */
export interface ManagedGameState {
  /** Current game phase */
  phase: GamePhase
  /** The proto GameState from the latest ON_UPDATE */
  gameState: ProtoGameState | undefined
  /** Entities grouped by player and zone */
  board: GroupedBoard | undefined
  /** Available actions from the latest ON_REQUEST_ACTION */
  actions: ProtoGameActions | undefined
  /** The message ID to reply to for the current action request */
  actionsMessageId: string | undefined
  /** Starting cards for mulligan from the latest ON_MULLIGAN */
  mulliganCards: ProtoEntity[]
  /** The message ID to reply to for the current mulligan */
  mulliganMessageId: string | undefined
  /** Game over result */
  gameOver: ProtoGameOver | undefined
  /** Most recent game event for animation */
  lastEvent: ProtoGameEvent | undefined
  /** Timer state */
  timers: ProtoTimers | undefined
  /** Local player ID */
  localPlayerId: number
  /** Whether it's the local player's turn */
  isLocalPlayerTurn: boolean
  /** Current turn number */
  turnNumber: number
}

/** Events the engine can emit */
export interface GameEngineEvents {
  entityClicked: (entity: ProtoEntity, side: PlayerSide) => void
  boardClicked: (side: PlayerSide, zone: ZonesMessage_Zones) => void
  endTurnClicked: () => void
  hoverStart: (entity: ProtoEntity) => void
  hoverEnd: () => void
}
