import { findEndTurnAction, findTargetedAction, getSummonSlots, getValidTargets } from './action-resolver';
import { ActionType, type GameActions } from '../../__generated__/client';

const actions = { all: [
  { actionType: ActionType.EndTurn, action: 0, sourceId: -1 },
  { actionType: ActionType.PhysicalAttack, sourceId: 10, targetKeyToActions: [{ target: 20, action: 3 }] },
  { actionType: ActionType.Summon, sourceId: 11, targetKeyToActions: [{ target: -1, action: 4, friendlyBattlefieldIndex: 0 }] },
], compatibility: [] } as unknown as GameActions;

test('uses server action indices including zero', () => {
  expect(findEndTurnAction(actions)).toBe(0);
  expect(findTargetedAction(actions, 10, 20)).toBe(3);
  expect(findTargetedAction(actions, 10, 99)).toBeUndefined();
});

test('summon positions are not attack targets', () => {
  expect(getValidTargets(actions, 10)).toEqual([20]);
  expect(getValidTargets(actions, 11)).toEqual([]);
  expect(getSummonSlots(actions, 11)).toEqual([{ targetId: -1, actionIndex: 4, battlefieldIndex: 0 }]);
});
