
# Action

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**actionType** | [**ActionType**](ActionType.md) |  |  [optional]
**targetRequirement** | [**TargetRequirementEnum**](#TargetRequirementEnum) | The target requirements of this action.  |  [optional]
**source** | **Integer** | An entity corresponding to the source of this action. For battlecries, the source is the minion with the battlecry. Otherwise, it is the player.  |  [optional]
**targetKey** | **Integer** | The target of the action. For summons, it is the index to summon into. For spell-like actions without predefined targets, like playing a spell card or a battlecry that has a user-selected target, it corresponds to the entity that can be targeted. For discovers, it corresponds to the choice&#39;s target.  |  [optional]
**actionSuffix** | **String** | A human-readable string describing the action.  |  [optional]
**canBeExecutedOnEntities** | **List&lt;Integer&gt;** | A list of entitiy IDs for valid targets of a battlecry, physical attack or discover action.  |  [optional]
**playCardCardReference** | **Integer** | The entity ID of the card that generated this play card action. Play card actions include summons, spells, weapon equips and hero powers.  |  [optional]
**discoverActionEntityId** | **Integer** |  |  [optional]
**physicalAttackActionAttackerReference** | **Integer** |  |  [optional]
**setAsideEntities** | [**List&lt;Entity&gt;**](Entity.md) |  |  [optional]


<a name="TargetRequirementEnum"></a>
## Enum: TargetRequirementEnum
Name | Value
---- | -----
NONE | &quot;NONE&quot;
AUTO | &quot;AUTO&quot;
ANY | &quot;ANY&quot;
MINIONS | &quot;MINIONS&quot;
ENEMY_CHARACTERS | &quot;ENEMY_CHARACTERS&quot;
FRIENDLY_CHARACTERS | &quot;FRIENDLY_CHARACTERS&quot;
ENEMY_MINIONS | &quot;ENEMY_MINIONS&quot;
FRIENDLY_MINIONS | &quot;FRIENDLY_MINIONS&quot;
HEROES | &quot;HEROES&quot;
ENEMY_HERO | &quot;ENEMY_HERO&quot;
FRIENDLY_HERO | &quot;FRIENDLY_HERO&quot;



