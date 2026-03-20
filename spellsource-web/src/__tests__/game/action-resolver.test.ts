import {
  findEndTurnAction,
  findActionsForEntity,
  findTargetedAction,
  findUntargetedActions,
  getValidTargets,
  getPlayableEntityIds,
  buildActionMessage,
  buildMulliganMessage,
} from '../../game/state/action-resolver'
import {
  type GameActions,
  type SpellAction,
  ActionTypeMessage_ActionType,
  MessageTypeMessage_MessageType,
} from 'spellsource-protos/dist/experimental/client/spellsource'

function makeAction(overrides: Partial<SpellAction>): SpellAction {
  return {
    action: 0,
    actionType: ActionTypeMessage_ActionType.SYSTEM,
    choices: [],
    description: '',
    entity: undefined,
    sourceId: 0,
    targetKeyToActions: [],
    request: '',
    ...overrides,
  }
}

function makeActions(all: SpellAction[]): GameActions {
  return { all, compatibility: [] }
}

describe('action-resolver', () => {
  describe('findEndTurnAction', () => {
    it('returns the action index for END_TURN', () => {
      const actions = makeActions([
        makeAction({ action: 0, actionType: ActionTypeMessage_ActionType.SPELL, sourceId: 1 }),
        makeAction({ action: 5, actionType: ActionTypeMessage_ActionType.END_TURN }),
      ])
      expect(findEndTurnAction(actions)).toBe(5)
    })

    it('returns undefined when no END_TURN exists', () => {
      const actions = makeActions([
        makeAction({ action: 0, actionType: ActionTypeMessage_ActionType.DISCOVER }),
      ])
      expect(findEndTurnAction(actions)).toBeUndefined()
    })
  })

  describe('findActionsForEntity', () => {
    it('returns all actions matching the source entity ID', () => {
      const actions = makeActions([
        makeAction({ action: 1, sourceId: 10, actionType: ActionTypeMessage_ActionType.PHYSICAL_ATTACK }),
        makeAction({ action: 2, sourceId: 10, actionType: ActionTypeMessage_ActionType.SPELL }),
        makeAction({ action: 3, sourceId: 20, actionType: ActionTypeMessage_ActionType.SPELL }),
      ])
      const result = findActionsForEntity(actions, 10)
      expect(result).toHaveLength(2)
      expect(result.map((a) => a.action)).toEqual([1, 2])
    })
  })

  describe('findTargetedAction', () => {
    it('returns the action index for a source→target pair', () => {
      const actions = makeActions([
        makeAction({
          action: 1,
          sourceId: 10,
          actionType: ActionTypeMessage_ActionType.PHYSICAL_ATTACK,
          targetKeyToActions: [
            { target: 20, action: 100, friendlyBattlefieldIndex: 0 },
            { target: 30, action: 101, friendlyBattlefieldIndex: 0 },
          ],
        }),
      ])
      expect(findTargetedAction(actions, 10, 30)).toBe(101)
    })

    it('returns undefined when target not found', () => {
      const actions = makeActions([
        makeAction({
          action: 1,
          sourceId: 10,
          targetKeyToActions: [
            { target: 20, action: 100, friendlyBattlefieldIndex: 0 },
          ],
        }),
      ])
      expect(findTargetedAction(actions, 10, 99)).toBeUndefined()
    })
  })

  describe('findUntargetedActions', () => {
    it('returns actions with no targets for a given source', () => {
      const actions = makeActions([
        makeAction({ action: 1, sourceId: 10, targetKeyToActions: [] }),
        makeAction({
          action: 2,
          sourceId: 10,
          targetKeyToActions: [{ target: 20, action: 100, friendlyBattlefieldIndex: 0 }],
        }),
        makeAction({ action: 3, sourceId: 10, targetKeyToActions: [] }),
      ])
      const result = findUntargetedActions(actions, 10)
      expect(result).toHaveLength(2)
      expect(result.map((a) => a.action)).toEqual([1, 3])
    })
  })

  describe('getValidTargets', () => {
    it('returns unique target IDs excluding -1', () => {
      const actions = makeActions([
        makeAction({
          sourceId: 10,
          targetKeyToActions: [
            { target: 20, action: 100, friendlyBattlefieldIndex: 0 },
            { target: 30, action: 101, friendlyBattlefieldIndex: 0 },
            { target: -1, action: 102, friendlyBattlefieldIndex: 3 },
          ],
        }),
        makeAction({
          sourceId: 10,
          targetKeyToActions: [
            { target: 20, action: 200, friendlyBattlefieldIndex: 0 },
          ],
        }),
      ])
      const targets = getValidTargets(actions, 10)
      expect(targets.sort()).toEqual([20, 30])
    })
  })

  describe('getPlayableEntityIds', () => {
    it('returns source IDs of non-END_TURN actions', () => {
      const actions = makeActions([
        makeAction({ sourceId: 10, actionType: ActionTypeMessage_ActionType.SPELL }),
        makeAction({ sourceId: 20, actionType: ActionTypeMessage_ActionType.PHYSICAL_ATTACK }),
        makeAction({ sourceId: 0, actionType: ActionTypeMessage_ActionType.END_TURN }),
        makeAction({ sourceId: 10, actionType: ActionTypeMessage_ActionType.SUMMON }),
      ])
      const ids = getPlayableEntityIds(actions)
      expect(ids.sort()).toEqual([10, 20])
    })
  })

  describe('buildActionMessage', () => {
    it('builds a valid action message', () => {
      const msg = buildActionMessage(7, 'req-123')
      expect(msg.actionIndex).toBe(7)
      expect(msg.repliesTo).toBe('req-123')
      expect(msg.messageType).toBe(MessageTypeMessage_MessageType.UPDATE_ACTION)
    })
  })

  describe('buildMulliganMessage', () => {
    it('builds a valid mulligan message', () => {
      const msg = buildMulliganMessage([0, 2, 3], 'req-mul')
      expect(msg.discardedCardIndices).toEqual([0, 2, 3])
      expect(msg.repliesTo).toBe('req-mul')
      expect(msg.messageType).toBe(MessageTypeMessage_MessageType.UPDATE_MULLIGAN)
    })
  })
})
