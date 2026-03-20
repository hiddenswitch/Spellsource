// Client facade (primary entry point)
export { Client } from './client/client'

// Individual clients (for advanced use)
export { AuthClient } from './client/auth-client'
export { CollectionClient } from './client/collection-client'
export { MatchmakingClient } from './client/matchmaking-client'
export { GameClient } from './client/game-client'
export { createTokenStore } from './client/token-store'
export { createHiddenswitchRpc, createSpellsourceRpc, createAuthMetadata, DEFAULT_ENDPOINT } from './client/grpc-transport'

// State management
export { GameStateManager, groupEntities } from './state/game-state-manager'
export {
  findEndTurnAction,
  findActionsForEntity,
  findTargetedAction,
  findUntargetedActions,
  getValidTargets,
  getPlayableEntityIds,
  buildActionMessage,
  buildMulliganMessage,
} from './state/action-resolver'

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
} from './types'
