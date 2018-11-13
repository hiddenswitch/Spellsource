
# InvitePostRequest

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**friend** | **Boolean** | When true, indicates that this request is a friend invitation.  |  [optional]
**queueId** | **String** | The queue that the player would like to 1v1 inside of. These may differ from the competitive queues.  |  [optional]
**deckId** | **String** | The deck the user is creating this invite with. Used for 1v1 queues. If this is specified, the user is automatically enqueued.  |  [optional]
**toUserId** | **String** | The user who should receive the invite  |  [optional]
**toUserNameWithToken** | **String** | The username and privacy token (#1234 part) to send the request to  |  [optional]
**message** | **String** | An optional message to add to the invite request  |  [optional]



