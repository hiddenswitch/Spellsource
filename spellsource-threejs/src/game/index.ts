// State management
export { reduceGameMessage, groupEntities, createInitialState } from './state/game-state-manager'
export {
  findEndTurnAction,
  findActionsForEntity,
  findTargetedAction,
  findUntargetedActions,
  findChooseOneAction,
  getValidTargets,
  getPlayableEntityIds,
  getSummonSlots,
  type SummonSlot,
} from './state/action-resolver'
export {
  reduceInteraction,
  createInitialInteraction,
  type InteractionState,
  type InteractionPhase,
  type InteractionAction,
  type InteractionResult,
} from './state/interaction-state'

// Hooks
export { useGameConnection, type GameConnection } from './hooks/use-game-connection'
export { useGameContext, GameContextProvider, type GameContextValue } from './hooks/use-game-context'
export { useAnimationQueue, type ActiveEffect } from './hooks/use-animation-queue'
export { useMatchmaking, type MatchmakingState, type MatchmakingActions, type MatchmakingPhase } from './hooks/use-matchmaking'

// Renderer
export { GameScene } from './renderer/game-scene'
export * as BoardLayout from './renderer/board-layout'
export * as GameConstants from './renderer/constants'

// Types
export type {
  PlayerSide,
  PlayerEntities,
  GroupedBoard,
  GamePhase,
  ManagedGameState,
  GameEngineEvents,
} from './types'

export {
  Zone,
  EntityType,
  CardType,
  ActionType,
  MessageType,
  EmoteType,
} from './types'
