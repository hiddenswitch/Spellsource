
# SpellAction

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sourceId** | **Integer** | The ID of the entity (minion or card) that is the source of the action. The client is guaranteed to have this entity in its entities array. For a SpellAction whose actionType is DISCOVER, the source is the entity in the acting player&#39;s discover zone. In the engine, the source is the entity that is prompting the discover (e.g. a minion if an opener is causing the discover, or the spell card being played). Sometimes ENDTURN will not be available, this is because some actions like DISCOVER and BATTLECRY cannot be interrupted. Running out of time will result in ENDTURN being chosen or a random DISCOVER or BATTLECRY action. This will occur on the server, not the client.  |  [optional]
**actionType** | [**ActionType**](ActionType.md) |  |  [optional]
**description** | **String** | A user-readable description of this action. This is typically not rendered in the client except in logs.  |  [optional]
**entity** | [**Entity**](Entity.md) |  |  [optional]
**choices** | [**List&lt;SpellAction&gt;**](SpellAction.md) | When set, represents a choose one action with entities set to render. Those entities&#39; id property corresponds to the choices&#39;s sourceId property. The parent/root action&#39;s sourceId corresponds to the actual entity that reveals the choices.  |  [optional]
**action** | **Integer** | The action index corresponding to this action.  If targetKeyToActions is length zero or null, the action is valid and set, corresponding to an action that does not take a user-specified target. This includes all DISCOVER actions, ENDTURN, but *never* a summon, even if no minions are on the board.  |  [optional]
**targetKeyToActions** | [**List&lt;TargetActionPair&gt;**](TargetActionPair.md) | An array of entity ID-action pairs that let you convert a valid target to an action index to respond with. Defined if this spell is targetable.  This is null or length zero if the target does not have targeted actions. Use the action property instead for that situation.  A SpellAction with actionType SUMMON will have a targetKeyToActions entry with a target of -1 corresponding to the *last* (rightmost) minion position to summon, while all other targets correspond to minions on the board.  |  [optional]



