
# InventoryCollection

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** | The identifier of this collection. Corresponds to a deckId when this is a deck collection.  |  [optional]
**userId** | **String** | The owner of this collection.  |  [optional]
**name** | **String** | The name of this collection. Corresponds to the deck name when this is a deck collection.  |  [optional]
**heroClass** | **String** | The hero class when this is a deck collection.  |  [optional]
**format** | **String** | The format when this is a deck collection.  |  [optional]
**type** | [**TypeEnum**](#TypeEnum) | The type of collection this object is. A user&#39;s personal collection is of type USER. A deck is of type DECK.  |  [optional]
**deckType** | [**DeckTypeEnum**](#DeckTypeEnum) | Indicates whether this is a deck meant for draft or constructed play.  |  [optional]
**isStandardDeck** | **Boolean** | When true, indicates this is a standard deck provided by the server.  |  [optional]
**inventory** | [**List&lt;CardRecord&gt;**](CardRecord.md) |  |  [optional]
**validationReport** | [**ValidationReport**](ValidationReport.md) |  |  [optional]


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



