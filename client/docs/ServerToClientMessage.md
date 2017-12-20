
# ServerToClientMessage

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** | An optional ID used to disambiguate multiple writer replies. Include this ID in the repliesTo field of your ClientToServerMessage if this field is not null.  |  [optional]
**localPlayerId** | **Integer** | The ID of the player that corresponds to the local player (the recipient).  |  [optional]
**messageType** | [**MessageType**](MessageType.md) |  |  [optional]
**changes** | [**EntityChangeSet**](EntityChangeSet.md) |  |  [optional]
**gameState** | [**GameState**](GameState.md) |  |  [optional]
**actions** | [**GameActions**](GameActions.md) |  |  [optional]
**emote** | [**Emote**](Emote.md) |  |  [optional]
**gameOver** | [**GameOver**](GameOver.md) |  |  [optional]
**startingCards** | [**List&lt;Entity&gt;**](Entity.md) | Used for a mulligan request. An array of entities representing the cards you may mulligan.  |  [optional]
**event** | [**GameEvent**](GameEvent.md) |  |  [optional]



