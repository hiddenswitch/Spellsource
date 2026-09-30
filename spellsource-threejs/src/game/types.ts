/**
 * Shared types bridging the GraphQL layer, state management, and renderer.
 *
 * The generated GraphQL types (Entity, GameState, SpellAction, etc.) are the
 * canonical game data types. This module re-exports them and adds thin
 * helper types for the renderer and UI layers.
 */

export type {
  Entity,
  EntityLocation,
  GameState,
  GameActions,
  GameEvent,
  GameOver,
  SpellAction,
  TargetActionPair,
  ServerGameMessage,
  Timers,
  Emote,
} from '../__generated__/client'

export {
  Zone,
  EntityType,
  CardType,
  ActionType,
  MessageType,
  EmoteType,
} from '../__generated__/client'

/** Which side of the board a player is on from the local player's perspective */
export type PlayerSide = 'top' | 'bottom'

/** Entities grouped by zone for one player */
export interface PlayerEntities {
  /** The player entity (mana, game state flags) */
  player: import('../__generated__/client').Entity | undefined
  hero: import('../__generated__/client').Entity | undefined
  heroPower: import('../__generated__/client').Entity | undefined
  weapon: import('../__generated__/client').Entity | undefined
  hand: import('../__generated__/client').Entity[]
  battlefield: import('../__generated__/client').Entity[]
  secrets: import('../__generated__/client').Entity[]
  quests: import('../__generated__/client').Entity[]
  deck: import('../__generated__/client').Entity[]
  graveyard: import('../__generated__/client').Entity[]
  discover: import('../__generated__/client').Entity[]
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
 * Built from ServerGameMessage stream by GameStateManager.
 */
export interface ManagedGameState {
  /** Current game phase */
  phase: GamePhase
  /** The GameState from the latest ON_UPDATE */
  gameState: import('../__generated__/client').GameState | undefined
  /** Entities grouped by player and zone */
  board: GroupedBoard | undefined
  /** Available actions from the latest ON_REQUEST_ACTION */
  actions: import('../__generated__/client').GameActions | undefined
  /** The message ID to reply to for the current action request */
  actionsMessageId: string | undefined
  /** Starting cards for mulligan from the latest ON_MULLIGAN */
  mulliganCards: import('../__generated__/client').Entity[]
  /** The message ID to reply to for the current mulligan */
  mulliganMessageId: string | undefined
  /** Game over result */
  gameOver: import('../__generated__/client').GameOver | undefined
  /** Most recent game event for animation */
  lastEvent: import('../__generated__/client').GameEvent | undefined
  /** Timer state */
  timers: import('../__generated__/client').Timers | undefined
  /** Local player ID */
  localPlayerId: number
  /** Whether it's the local player's turn */
  isLocalPlayerTurn: boolean
  /** Current turn number */
  turnNumber: number
}

/** Events the engine can emit */
export interface GameEngineEvents {
  entityClicked: (entity: import('../__generated__/client').Entity, side: PlayerSide) => void
  boardClicked: (side: PlayerSide, zone: import('../__generated__/client').Zone) => void
  endTurnClicked: () => void
  hoverStart: (entity: import('../__generated__/client').Entity) => void
  hoverEnd: () => void
}
