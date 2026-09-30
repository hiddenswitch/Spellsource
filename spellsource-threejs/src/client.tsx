import React, { useState, useCallback, useEffect, useMemo, type FunctionComponent } from 'react';
import { ApolloClient, ApolloProvider, InMemoryCache, HttpLink, split } from '@apollo/client';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { getMainDefinition } from '@apollo/client/utilities';
import { createClient } from 'graphql-ws';
import { GameScene } from './game/renderer/game-scene';
import { GameContextProvider, useGameContext } from './game/hooks/use-game-context';
import { useGameConnection } from './game/hooks/use-game-connection';
import { useMatchmaking } from './game/hooks/use-matchmaking';
import { DemoGameView } from './game/demo/demo-game-view';
import styles from './game/renderer/overlays/overlay.module.css';
import { MulliganOverlay } from './game/renderer/overlays/mulligan-overlay';
import { DiscoverOverlay } from './game/renderer/overlays/discover-overlay';
import { ChooseOneOverlay } from './game/renderer/overlays/choose-one-overlay';
import { CardTooltip } from './game/renderer/overlays/card-tooltip';
import { PowerHistory } from './game/renderer/overlays/power-history';
import { TurnTimer } from './game/renderer/overlays/turn-timer';
import { GameOverScreen } from './game/renderer/overlays/game-over-screen';
import { QueueScreen } from './game/renderer/overlays/queue-screen';
import { CardReveal } from './game/renderer/overlays/card-reveal';
const ConnectedGameView: FunctionComponent<{ onExit: () => void }> = ({ onExit }) => {
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
    onExit();
  }, [onExit]);

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

const LiveGameView: FunctionComponent<{ onExit: () => void; onDemo: () => void }> = ({ onExit, onDemo }) => {
  const { state: mmState, actions: mmActions } = useMatchmaking();
  const gameReady = mmState.phase === "ready";
  const { state, activeEffects, revealedCard, sendAction, sendMulligan, concedeGame, error } = useGameConnection();

  if (!gameReady) {
    return (
      <QueueScreen
        state={mmState}
        actions={mmActions}
        onDemo={onDemo}
      />
    );
  }

  return (
    <GameContextProvider state={state} activeEffects={activeEffects} revealedCard={revealedCard} sendAction={sendAction} sendMulligan={sendMulligan} concedeGame={concedeGame}>
      {error && <div role="alert">{error}</div>}
      <ConnectedGameView onExit={onExit} />
    </GameContextProvider>
  );
};


export interface SpellsourceThreeClientProps {
  graphqlUrl: string;
  subscriptionsUrl: string;
  accessToken?: string;
  mode?: 'live' | 'demo';
  onExit: () => void;
}

export function SpellsourceThreeClient({ graphqlUrl, subscriptionsUrl, accessToken, mode = 'live', onExit }: SpellsourceThreeClientProps) {
  const [demo, setDemo] = useState(mode === 'demo');
  const transport = useMemo(() => {
    const socket = createClient({ url: subscriptionsUrl, lazy: true,
      connectionParams: () => ({ Authorization: accessToken ? `Bearer ${accessToken}` : '' }),
      retryAttempts: 5 });
    const link = split(({ query }) => {
      const definition = getMainDefinition(query);
      return definition.kind === 'OperationDefinition' && definition.operation === 'subscription';
    }, new GraphQLWsLink(socket), new HttpLink({ uri: graphqlUrl, headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {} }));
    return { socket, client: new ApolloClient({ link, cache: new InMemoryCache() }) };
  }, [graphqlUrl, subscriptionsUrl, accessToken]);
  useEffect(() => () => { transport.client.stop(); void transport.socket.dispose(); }, [transport]);
  return <div style={{ position: 'relative', width: '100%', height: '100%', minHeight: 480, background: '#101018' }}>
    {demo ? <DemoGameView /> : <ApolloProvider client={transport.client}><LiveGameView onExit={onExit} onDemo={() => setDemo(true)} /></ApolloProvider>}
  </div>;
}
