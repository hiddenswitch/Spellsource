
# DraftState

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**heroClassChoices** | [**List&lt;Entity&gt;**](Entity.md) | When not null, contains three choices you should reply with to choose the hero of your draft.  |  [optional]
**currentCardChoices** | [**List&lt;Entity&gt;**](Entity.md) | When not null, contains the cards that correspond to your choices for the next draft selection.  |  [optional]
**heroClass** | [**Entity**](Entity.md) |  |  [optional]
**status** | [**StatusEnum**](#StatusEnum) | Gets the status of the draft.  |  [optional]
**selectedCardIds** | **List&lt;String&gt;** |  |  [optional]
**cardsRemaining** | **Integer** | Gets the number of card choices remaining to make.  |  [optional]
**draftIndex** | **Integer** | Gets the current draft index.  |  [optional]
**wins** | **Integer** | The number of wins you have achieved with your current draft deck.  |  [optional]
**losses** | **Integer** | The number of losses you have suffered with your current draft deck.  |  [optional]
**deckId** | **String** | The deck that corresponds to your finished draft deck.  |  [optional]


<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
IN_PROGRESS | &quot;IN_PROGRESS&quot;
SELECT_HERO | &quot;SELECT_HERO&quot;
COMPLETE | &quot;COMPLETE&quot;
RETIRED | &quot;RETIRED&quot;



