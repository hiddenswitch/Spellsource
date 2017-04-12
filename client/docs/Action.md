
# Action

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**targetRequirement** | [**TargetRequirementEnum**](#TargetRequirementEnum) |  |  [optional]
**actionType** | [**ActionType**](ActionType.md) |  |  [optional]
**source** | **Integer** |  |  [optional]
**targetKey** | **Integer** |  |  [optional]
**actionSuffix** | **String** |  |  [optional]
**canBeExecutedOnEntities** | **List&lt;Integer&gt;** |  |  [optional]
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



