
# ClientToServerMessage

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**messageType** | [**MessageType**](MessageType.md) |  |  [optional]
**repliesTo** | **String** | The ID of the server message this client message is replying to.  |  [optional]
**firstMessage** | [**ClientToServerMessageFirstMessage**](ClientToServerMessageFirstMessage.md) |  |  [optional]
**actionIndex** | **Integer** | The index of the available actions to use.  |  [optional]
**discardedCardIndices** | **List&lt;Integer&gt;** | The indices of cards to discard in a mulligan. |  [optional]
**emote** | [**Emote**](Emote.md) |  |  [optional]
**entityTouch** | **Integer** | When specified with an entity ID, indicates the client is \&quot;touching\&quot; this entity.  |  [optional]
**entityUntouch** | **Integer** | When specified with an entity ID, indicates the client is no longer touching the specified entity.  |  [optional]



