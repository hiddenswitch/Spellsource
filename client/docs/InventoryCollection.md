
# InventoryCollection

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** |  |  [optional]
**userId** | **String** |  |  [optional]
**name** | **String** |  |  [optional]
**heroClass** | **String** |  |  [optional]
**type** | [**TypeEnum**](#TypeEnum) |  |  [optional]
**deckType** | [**DeckTypeEnum**](#DeckTypeEnum) |  |  [optional]
**inventory** | [**List&lt;CardRecord&gt;**](CardRecord.md) |  |  [optional]


<a name="TypeEnum"></a>
## Enum: TypeEnum
Name | Value
---- | -----
USER | &quot;USER&quot;
ALLIANCE | &quot;ALLIANCE&quot;
DECK | &quot;DECK&quot;


<a name="DeckTypeEnum"></a>
## Enum: DeckTypeEnum
Name | Value
---- | -----
DRAFT | &quot;DRAFT&quot;
CONSTRUCTED | &quot;CONSTRUCTED&quot;



