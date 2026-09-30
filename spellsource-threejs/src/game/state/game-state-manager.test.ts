import { createInitialState, reduceGameMessage } from './game-state-manager';
import { buildDemoState } from '../demo/demo-state';
import { MessageType, type ServerGameMessage } from '../../__generated__/client';

const message = (data: Partial<ServerGameMessage>) => ({ localPlayerId: 0, isReplayMessage: false, ...data } as ServerGameMessage);

test('mulligan and action requests retain reply IDs and player zero', () => {
  const demo = buildDemoState();
  const mulligan = reduceGameMessage(createInitialState(), message({ messageType: MessageType.OnMulligan, id: 'mulligan-1', startingCards: demo.board!.bottom.hand }));
  expect(mulligan.phase).toBe('mulligan');
  expect(mulligan.mulliganMessageId).toBe('mulligan-1');
  const state = reduceGameMessage(mulligan, message({ messageType: MessageType.OnRequestAction, id: 'action-1', gameState: demo.gameState, actions: demo.actions }));
  expect(state.phase).toBe('playing');
  expect(state.localPlayerId).toBe(0);
  expect(state.actionsMessageId).toBe('action-1');
  expect(state.actions).toBe(demo.actions);
});

test('timers and game over preserve the board', () => {
  const demo = buildDemoState();
  const timed = reduceGameMessage(demo, message({ messageType: MessageType.Timer, timers: { millisRemaining: "5000" } }));
  expect(timed.board).toBe(demo.board);
  expect(timed.timers!.millisRemaining).toBe("5000");
  const ended = reduceGameMessage(timed, message({ messageType: MessageType.OnGameEnd, gameOver: { localPlayerWon: true, winningPlayerId: 0 } }));
  expect(ended.phase).toBe('game_over');
  expect(ended.gameOver!.winningPlayerId).toBe(0);
  expect(ended.board).toBe(demo.board);
});
