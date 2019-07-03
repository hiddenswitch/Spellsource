
# DecksPutRequest

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**heroClass** | **String** | A valid hero class for creating the deck. The appropriate hero card will be chosen for this deck unless otherwise specified.  |  [optional]
**name** | **String** | The name of the deck as it will appear in the collections view. Typically, your opponent will not be able to see this name.  Some custom cards interact with specific named decks in your collection. For those purposes, the deck names are case sensitive. When multiple decks share a name, one will be chosen arbitrarily (not at random).  |  [optional]
**inventoryIds** | **List&lt;String&gt;** |  |  [optional]
**format** | [**FormatEnum**](#FormatEnum) | The format of this deck. Format specifies which cards are allowable in this deck for validation. It also specifies which cards will appear in discovers during matchmaking.  Currenly, matchmaking occurs between decks of all formats, regardless of your choice of format. The smallest possible format encompassing both decks in a match is selected when the formats of the decks do not match.  Certain queues only support certain formats. Typically, when requesting the listing of queues with matchmakingGet, the queues will specify which current decks can be chosen.  |  [optional]
**deckList** | **String** | A community-standard decklist.  |  [optional]


<a name="FormatEnum"></a>
## Enum: FormatEnum
Name | Value
---- | -----
STANDARD | &quot;Standard&quot;
WILD | &quot;Wild&quot;
CUSTOM | &quot;Custom&quot;
SPELLSOURCE | &quot;Spellsource&quot;
ALL | &quot;All&quot;



