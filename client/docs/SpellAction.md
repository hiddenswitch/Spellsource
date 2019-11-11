
# SpellAction

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sourceId** | **Integer** | The ID of the entity (minion or card) that originates this action  |  [optional]
**actionType** | [**ActionType**](ActionType.md) |  |  [optional]
**description** | **String** | A user-readable description of this action.  |  [optional]
**entity** | [**Entity**](Entity.md) |  |  [optional]
**choices** | [**List&lt;SpellAction&gt;**](SpellAction.md) | When set, represents a choose one action with entities set to render. Those entities&#39; id property corresponds to the choices&#39;s sourceId property. The parent/root action&#39;s sourceId corresponds to the actual entity that reveals the choices.  |  [optional]
**action** | **Integer** | The action for this spell.  |  [optional]
**targetKeyToActions** | [**List&lt;TargetActionPair&gt;**](TargetActionPair.md) | An array of entity ID-action pairs that let you convert a valid target to an action index to respond with. Defined if this spell is targetable.  |  [optional]



