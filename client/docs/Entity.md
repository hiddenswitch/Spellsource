
# Entity

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Integer** | The entity&#39;s ID in the game. | 
**cardId** | **String** | The entity&#39;s Card ID. When null, it typically should not be rendered. |  [optional]
**entityType** | [**EntityTypeEnum**](#EntityTypeEnum) | Broad categories describing this entity and how it should be rendered.  |  [optional]
**name** | **String** | The text that would go into the entity&#39;s name field.  |  [optional]
**description** | **String** | The text that would go into the entity&#39;s description field.  |  [optional]
**state** | [**EntityState**](EntityState.md) |  |  [optional]


<a name="EntityTypeEnum"></a>
## Enum: EntityTypeEnum
Name | Value
---- | -----
PLAYER | &quot;PLAYER&quot;
HERO | &quot;HERO&quot;
CARD | &quot;CARD&quot;
MINION | &quot;MINION&quot;
WEAPON | &quot;WEAPON&quot;
SECRET | &quot;SECRET&quot;
QUEST | &quot;QUEST&quot;
ENCHANTMENT | &quot;ENCHANTMENT&quot;



