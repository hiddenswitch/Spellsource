import React, { useState, useCallback, useEffect, type FunctionComponent } from 'react'
import dynamic from 'next/dynamic'
import type { Entity, ManagedGameState, PlayerSide } from '../types'
import { CardTooltip } from '../renderer/overlays/card-tooltip'
import { buildDemoState } from './demo-state'

const GameScene = dynamic(() => import('../renderer/game-scene').then((m) => m.GameScene), {
  ssr: false,
})

export const DemoGameView: FunctionComponent = () => {
  const [state] = useState<ManagedGameState>(buildDemoState)
  const [hoveredEntity, setHoveredEntity] = useState<Entity | null>(null)
  const [mousePos, setMousePos] = useState({ x: 0, y: 0 })

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => setMousePos({ x: e.clientX, y: e.clientY })
    window.addEventListener('mousemove', handleMouseMove)
    return () => window.removeEventListener('mousemove', handleMouseMove)
  }, [])

  const onEntityClicked = useCallback((clickedEntity: Entity, side: PlayerSide) => {
    console.log('Entity clicked:', clickedEntity.name, `(id=${clickedEntity.id})`, side)
  }, [])

  const onEndTurnClicked = useCallback(() => {
    console.log('End turn clicked')
  }, [])

  return (
    <>
      <GameScene
        state={state}
        onEntityClicked={onEntityClicked}
        onEndTurnClicked={onEndTurnClicked}
        onHoverStart={setHoveredEntity}
        onHoverEnd={() => setHoveredEntity(null)}
      />
      <CardTooltip
        entity={hoveredEntity}
        mouseX={mousePos.x}
        mouseY={mousePos.y}
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
    </>
  )
}
