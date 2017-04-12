
# ServerToClientMessage

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** | An optional ID used to disambiguate multiple client replies. Include this ID in the repliesTo field of your ClientToServerMessage if this field is not null.  |  [optional]
**messageType** | [**MessageType**](MessageType.md) |  |  [optional]
**gameState** | [**GameState**](GameState.md) |  |  [optional]
**actions** | [**GameActions**](GameActions.md) |  |  [optional]
**startingCards** | [**List&lt;Entity&gt;**](Entity.md) | Used for a mulligan request. An array of entities representing the cards you may mulligan.  |  [optional]
**event** | [**GameEvent**](GameEvent.md) |  |  [optional]



