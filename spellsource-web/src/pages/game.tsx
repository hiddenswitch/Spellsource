import React, { type FunctionComponent, useCallback, useEffect, useState } from "react";
import dynamic from "next/dynamic";
import Head from "next/head";
import { useRouter } from "next/router";
import { GameContextProvider, useGameContext } from "../game/hooks/use-game-context";
import { useGameConnection } from "../game/hooks/use-game-connection";
import { useMatchmaking } from "../game/hooks/use-matchmaking";
import { MulliganOverlay } from "../game/renderer/overlays/mulligan-overlay";
import { DiscoverOverlay } from "../game/renderer/overlays/discover-overlay";
import { ChooseOneOverlay } from "../game/renderer/overlays/choose-one-overlay";
import { CardTooltip } from "../game/renderer/overlays/card-tooltip";
import { PowerHistory } from "../game/renderer/overlays/power-history";
import { TurnTimer } from "../game/renderer/overlays/turn-timer";
import { GameOverScreen } from "../game/renderer/overlays/game-over-screen";
import { QueueScreen } from "../game/renderer/overlays/queue-screen";
import { CardReveal } from "../game/renderer/overlays/card-reveal";
import { DemoGameView } from "../game/demo/demo-game-view";
import styles from "../game/renderer/overlays/overlay.module.css";
import Layout from "../components/creative-layout";

const GameScene = dynamic(() => import("../game/renderer/game-scene").then((m) => m.GameScene), {
  ssr: false,
});

// ── Connected game view ──────────────────────────────────────

const ConnectedGameView: FunctionComponent = () => {
  const ctx = useGameContext();
  const { state, interaction, activeEffects, revealedCard, onEntityClicked, onEndTurnClicked, onSummonSlotClicked, onChoicePicked, onCancel, sendAction, sendMulligan, concedeGame, hoveredEntity, setHoveredEntity } = ctx;

  const [mousePos, setMousePos] = useState({ x: 0, y: 0 });

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => setMousePos({ x: e.clientX, y: e.clientY });
    window.addEventListener("mousemove", handleMouseMove);
    return () => window.removeEventListener("mousemove", handleMouseMove);
  }, []);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onCancel();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onCancel]);

  useEffect(() => {
    const handleContextMenu = (e: MouseEvent) => {
      if (interaction.phase !== "idle") {
        e.preventDefault();
        onCancel();
      }
    };
    window.addEventListener("contextmenu", handleContextMenu);
    return () => window.removeEventListener("contextmenu", handleContextMenu);
  }, [interaction.phase, onCancel]);

  const handleReturn = useCallback(() => {
    window.location.href = "/game";
  }, []);

  const discoverCards = state.board?.bottom.discover ?? [];

  return (
    <>
      <GameScene state={state} interaction={interaction} activeEffects={activeEffects} onEntityClicked={onEntityClicked} onEndTurnClicked={onEndTurnClicked} onSummonSlotClicked={onSummonSlotClicked} onHoverStart={setHoveredEntity} onHoverEnd={() => setHoveredEntity(null)} />

      <CardReveal revealedCard={revealedCard} />
      <TurnTimer timers={state.timers} isLocalPlayerTurn={state.isLocalPlayerTurn} />

      {state.phase === "mulligan" && state.mulliganCards.length > 0 && <MulliganOverlay cards={state.mulliganCards} onConfirm={sendMulligan} onHoverStart={setHoveredEntity} onHoverEnd={() => setHoveredEntity(null)} />}

      {discoverCards.length > 0 && <DiscoverOverlay cards={discoverCards} actions={state.actions} onAction={sendAction} onHoverStart={setHoveredEntity} onHoverEnd={() => setHoveredEntity(null)} />}

      {interaction.phase === "awaiting_choice" && interaction.pendingChoices.length > 0 && <ChooseOneOverlay choices={interaction.pendingChoices} onPick={onChoicePicked} onCancel={onCancel} />}

      <CardTooltip entity={hoveredEntity} mouseX={mousePos.x} mouseY={mousePos.y} />
      <PowerHistory lastEvent={state.lastEvent} />

      {state.phase === "game_over" && <GameOverScreen gameOver={state.gameOver} localPlayerId={state.localPlayerId} onReturn={handleReturn} />}

      {state.phase === "playing" && (
        <button className={styles.concedeButton} onClick={concedeGame}>
          Concede
        </button>
      )}
    </>
  );
};

// ── Live wrapper (server-connected) ──────────────────────────

const LiveGameView: FunctionComponent = () => {
  const { state: mmState, actions: mmActions } = useMatchmaking();
  const gameReady = mmState.phase === "ready";
  const { state, activeEffects, revealedCard, sendAction, sendMulligan, concedeGame } = useGameConnection({ skip: !gameReady });

  if (!gameReady) {
    return (
      <QueueScreen
        state={mmState}
        actions={mmActions}
        onDemo={() => {
          window.location.href = "/game?demo=true";
        }}
      />
    );
  }

  return (
    <GameContextProvider state={state} activeEffects={activeEffects} revealedCard={revealedCard} sendAction={sendAction} sendMulligan={sendMulligan} concedeGame={concedeGame}>
      <ConnectedGameView />
    </GameContextProvider>
  );
};

// ── Page component ──────────────────────────────────────────

const GamePage: FunctionComponent = () => {
  const router = useRouter();
  const isDemo = router.query.demo === "true";

  return (
    <Layout>
      <Head>
        <title>Spellsource — Game</title>
      </Head>
      <div
        className="bg-black d-flex flex-grow-1 position-relative"
        style={{ minHeight: 0, flex: "1 1 0%" }}
      >
        {isDemo ? <DemoGameView /> : <LiveGameView />}
      </div>
    </Layout>
  );
};

export default GamePage;
