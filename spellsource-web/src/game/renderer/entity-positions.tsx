import React, { createContext, useContext, useRef } from 'react'
import type { Vec3 } from './board-layout'

export interface PositionRecord {
  pos: Vec3
  zone: string
}

/**
 * Shared map tracking the last known world position of each entity by ID.
 * Also tracks the last removed hand card position so battlefield entries
 * that come from a different entity ID (card→minion) can animate from hand.
 */
export interface EntityPositionStore {
  /** Per-entity last known position and zone */
  entities: Map<number, PositionRecord>
  /** Queue of recently removed hand card positions per side (FIFO).
   *  When a card is played (hand→battlefield with different entity ID),
   *  the new minion pops the oldest position from the queue. */
  removedHandPositions: { bottom: Vec3[]; top: Vec3[] }
  /** False until the first render pass completes. Cards appearing before
   *  this is set skip their entrance animation (initial hand deal). */
  initialRenderComplete: boolean
}

const EntityPositionContext = createContext<React.MutableRefObject<EntityPositionStore> | null>(null)

export const EntityPositionProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
  const storeRef = useRef<EntityPositionStore>({
    entities: new Map(),
    removedHandPositions: { bottom: [], top: [] },
    initialRenderComplete: false,
  })
  return (
    <EntityPositionContext.Provider value={storeRef}>
      {children}
    </EntityPositionContext.Provider>
  )
}

export function useEntityPositionStore(): React.MutableRefObject<EntityPositionStore> {
  const ref = useContext(EntityPositionContext)
  if (!ref) throw new Error('useEntityPositionStore must be inside EntityPositionProvider')
  return ref
}
