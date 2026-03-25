import React, { useCallback, useEffect, useRef, useState, type FunctionComponent } from "react";
import { Canvas, useThree, useFrame } from "@react-three/fiber";
import type { OrthographicCamera } from "three";
import type { Entity } from "../../__generated__/client";
import type { Zone } from "../../__generated__/client";
import type { ManagedGameState, PlayerSide } from "../types";
import type { InteractionState } from "../state/interaction-state";
import type { ActiveEffect } from "../hooks/use-animation-queue";
import type { SummonSlot } from "../state/action-resolver";
import type { Vec3 } from "./board-layout";
import * as C from "./constants";
import { BoardMesh } from "./board-mesh";
import { Hand, Battlefield } from "./card-mesh";
import { Heroes } from "./hero-mesh";
import { ManaDisplay } from "./mana-display";
import { EndTurnButton } from "./end-turn-button";
import { DamageNumberLayer } from "./effects/damage-number";
import { SummonSlots } from "./summon-slots";

interface GameSceneProps {
  state?: ManagedGameState;
  interaction?: InteractionState;
  activeEffects?: ActiveEffect[];
  style?: React.CSSProperties;
  className?: string;
  onEntityClicked?: (entity: Entity, side: PlayerSide) => void;
  onBoardClicked?: (side: PlayerSide, zone: Zone) => void;
  onEndTurnClicked?: () => void;
  onSummonSlotClicked?: (slot: SummonSlot) => void;
  onHoverStart?: (entity: Entity) => void;
  onHoverEnd?: () => void;
}

/** Keeps the orthographic frustum sized to show CAMERA_FRUSTUM_SIZE world units vertically */
const CameraRig: React.FC = () => {
  const { camera, size } = useThree();

  useEffect(() => {
    const cam = camera as OrthographicCamera;
    const d = C.CAMERA_FRUSTUM_SIZE;
    const aspect = size.width / size.height;
    cam.left = -d * aspect;
    cam.right = d * aspect;
    cam.top = d;
    cam.bottom = -d;
    cam.updateProjectionMatrix();
  }, [camera, size]);

  return null;
};

/** Forces re-render each frame so damage number floats animate */
const EffectTicker: React.FC<{ hasEffects: boolean }> = ({ hasEffects }) => {
  const [, setTick] = useState(0);
  useFrame(() => {
    if (hasEffects) setTick((t) => t + 1);
  });
  return null;
};

const SceneContents: FunctionComponent<Omit<GameSceneProps, "style" | "className">> = ({ state, interaction, activeEffects, onEntityClicked, onBoardClicked, onEndTurnClicked, onSummonSlotClicked, onHoverStart, onHoverEnd }) => {
  const board = state?.board;
  const entityPositionsRef = useRef<Map<number, Vec3>>(new Map());
  const [entityPositions, setEntityPositions] = useState<Map<number, Vec3>>(new Map());

  const onPositionReady = useCallback((entityId: number, pos: Vec3) => {
    entityPositionsRef.current.set(entityId, pos);
    // Batch position updates
    setEntityPositions(new Map(entityPositionsRef.current));
  }, []);

  const hasEffects = (activeEffects?.length ?? 0) > 0;

  return (
    <>
      <CameraRig />
      <EffectTicker hasEffects={hasEffects} />

      {/* Lighting */}
      <ambientLight color={C.COLOR_AMBIENT_LIGHT} intensity={0.6} />
      <directionalLight color={C.COLOR_DIR_LIGHT} intensity={0.8} position={[3, 10, -4]} castShadow shadow-mapSize-width={2048} shadow-mapSize-height={2048} shadow-camera-near={1} shadow-camera-far={20} shadow-camera-left={-8} shadow-camera-right={8} shadow-camera-top={6} shadow-camera-bottom={-6} />
      <directionalLight color={0xaabbcc} intensity={0.2} position={[-2, 3, -8]} />

      {/* Static board */}
      <BoardMesh />

      {/* End turn button */}
      <EndTurnButton active={state?.isLocalPlayerTurn ?? false} onClick={onEndTurnClicked} />

      {/* Dynamic game entities */}
      {board && (
        <>
          <Hand side="bottom" hand={board.bottom.hand} interaction={interaction} effects={activeEffects} onEntityClicked={onEntityClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} onPositionReady={onPositionReady} />
          <Hand side="top" hand={board.top.hand} interaction={interaction} effects={activeEffects} onEntityClicked={onEntityClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} onPositionReady={onPositionReady} />
          <Battlefield side="bottom" entities={board.bottom.battlefield} interaction={interaction} effects={activeEffects} onEntityClicked={onEntityClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} onPositionReady={onPositionReady} />
          <Battlefield side="top" entities={board.top.battlefield} interaction={interaction} effects={activeEffects} onEntityClicked={onEntityClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} onPositionReady={onPositionReady} />
          <Heroes bottom={board.bottom} top={board.top} interaction={interaction} effects={activeEffects} entityPositions={entityPositions} onEntityClicked={onEntityClicked} onBoardClicked={onBoardClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} onPositionReady={onPositionReady} />
          <ManaDisplay bottom={board.bottom} top={board.top} />

          {/* Summon position slots */}
          {interaction?.phase === "awaiting_summon_position" && onSummonSlotClicked && <SummonSlots slots={interaction.summonSlots} fieldEntities={board.bottom.battlefield} onSlotClicked={onSummonSlotClicked} />}
        </>
      )}

      {/* Floating damage/heal numbers */}
      {activeEffects && activeEffects.length > 0 && <DamageNumberLayer effects={activeEffects} entityPositions={entityPositions} />}
    </>
  );
};

export const GameScene: FunctionComponent<GameSceneProps> = ({ state, interaction, activeEffects, style, className, onEntityClicked, onBoardClicked, onEndTurnClicked, onSummonSlotClicked, onHoverStart, onHoverEnd }) => (
  <div
    className={className}
    style={{
      overflow: "hidden",
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
        camera.lookAt(C.CAMERA_LOOK_AT.x, C.CAMERA_LOOK_AT.y, C.CAMERA_LOOK_AT.z);
      }}
      shadows
      dpr={[1, 2]}
      gl={{ antialias: true }}
      style={{ background: `#${C.COLOR_BACKGROUND.toString(16).padStart(6, "0")}` }}
    >
      <SceneContents state={state} interaction={interaction} activeEffects={activeEffects} onEntityClicked={onEntityClicked} onBoardClicked={onBoardClicked} onEndTurnClicked={onEndTurnClicked} onSummonSlotClicked={onSummonSlotClicked} onHoverStart={onHoverStart} onHoverEnd={onHoverEnd} />
    </Canvas>
  </div>
);
