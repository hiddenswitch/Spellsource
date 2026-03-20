import React, { useEffect, type FunctionComponent } from 'react'
import { Canvas, useThree } from '@react-three/fiber'
import type { OrthographicCamera } from 'three'
import type { Entity } from '../../__generated__/client'
import type { Zone } from '../../__generated__/client'
import type { ManagedGameState, PlayerSide } from '../types'
import * as C from './constants'
import { BoardMesh } from './board-mesh'
import { Hand, Battlefield } from './card-mesh'
import { Heroes } from './hero-mesh'
import { ManaDisplay } from './mana-display'
import { EndTurnButton } from './end-turn-button'

interface GameSceneProps {
  state?: ManagedGameState
  style?: React.CSSProperties
  className?: string
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void
  onBoardClicked?: (side: PlayerSide, zone: Zone) => void
  onEndTurnClicked?: () => void
  onHoverStart?: (entity: Entity) => void
  onHoverEnd?: () => void
}

/** Keeps the orthographic frustum sized to show CAMERA_FRUSTUM_SIZE world units vertically */
const CameraRig: React.FC = () => {
  const { camera, size } = useThree()

  useEffect(() => {
    const cam = camera as OrthographicCamera
    const d = C.CAMERA_FRUSTUM_SIZE
    const aspect = size.width / size.height
    cam.left = -d * aspect
    cam.right = d * aspect
    cam.top = d
    cam.bottom = -d
    cam.updateProjectionMatrix()
  }, [camera, size])

  return null
}

const SceneContents: FunctionComponent<Omit<GameSceneProps, 'style' | 'className'>> = ({
  state,
  onEntityClicked,
  onBoardClicked,
  onEndTurnClicked,
  onHoverStart,
  onHoverEnd,
}) => {
  const board = state?.board

  return (
    <>
      <CameraRig />

      {/* Lighting */}
      <ambientLight color={C.COLOR_AMBIENT_LIGHT} intensity={0.6} />
      <directionalLight
        color={C.COLOR_DIR_LIGHT}
        intensity={0.8}
        position={[3, 10, -4]}
        castShadow
        shadow-mapSize-width={2048}
        shadow-mapSize-height={2048}
        shadow-camera-near={1}
        shadow-camera-far={20}
        shadow-camera-left={-8}
        shadow-camera-right={8}
        shadow-camera-top={6}
        shadow-camera-bottom={-6}
      />
      <directionalLight color={0xaabbcc} intensity={0.2} position={[-2, 3, -8]} />

      {/* Static board */}
      <BoardMesh />

      {/* End turn button */}
      <EndTurnButton active={state?.isLocalPlayerTurn ?? false} onClick={onEndTurnClicked} />

      {/* Dynamic game entities */}
      {board && (
        <>
          <Hand
            side="bottom"
            hand={board.bottom.hand}
            onEntityClicked={onEntityClicked}
            onHoverStart={onHoverStart}
            onHoverEnd={onHoverEnd}
          />
          <Hand
            side="top"
            hand={board.top.hand}
            onEntityClicked={onEntityClicked}
            onHoverStart={onHoverStart}
            onHoverEnd={onHoverEnd}
          />
          <Battlefield
            side="bottom"
            entities={board.bottom.battlefield}
            onEntityClicked={onEntityClicked}
            onHoverStart={onHoverStart}
            onHoverEnd={onHoverEnd}
          />
          <Battlefield
            side="top"
            entities={board.top.battlefield}
            onEntityClicked={onEntityClicked}
            onHoverStart={onHoverStart}
            onHoverEnd={onHoverEnd}
          />
          <Heroes
            bottom={board.bottom}
            top={board.top}
            onEntityClicked={onEntityClicked}
            onBoardClicked={onBoardClicked}
          />
          <ManaDisplay bottom={board.bottom} top={board.top} />
        </>
      )}
    </>
  )
}

export const GameScene: FunctionComponent<GameSceneProps> = ({
  state,
  style,
  className,
  onEntityClicked,
  onBoardClicked,
  onEndTurnClicked,
  onHoverStart,
  onHoverEnd,
}) => (
  <div
    className={className}
    style={{
      width: '100%',
      height: '100%',
      position: 'relative',
      overflow: 'hidden',
      ...style,
    }}
  >
    <Canvas
      orthographic
      camera={{
        near: 0.1,
        far: 100,
        position: [C.CAMERA_POSITION.x, C.CAMERA_POSITION.y, C.CAMERA_POSITION.z],
      }}
      onCreated={({ camera }) => {
        camera.lookAt(C.CAMERA_LOOK_AT.x, C.CAMERA_LOOK_AT.y, C.CAMERA_LOOK_AT.z)
      }}
      shadows
      dpr={[1, 2]}
      gl={{ antialias: true }}
      style={{ background: `#${C.COLOR_BACKGROUND.toString(16).padStart(6, '0')}` }}
    >
      <SceneContents
        state={state}
        onEntityClicked={onEntityClicked}
        onBoardClicked={onBoardClicked}
        onEndTurnClicked={onEndTurnClicked}
        onHoverStart={onHoverStart}
        onHoverEnd={onHoverEnd}
      />
    </Canvas>
  </div>
)
