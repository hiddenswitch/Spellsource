
# Invite

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** | The ID of the invite.  |  [optional]
**fromUserId** | **String** | The user ID from whom the invite originates  |  [optional]
**toUserId** | **String** | The user ID to whom the invite is addressed  |  [optional]
**toName** | **String** | The name of the user to whom the invite is addressed  |  [optional]
**fromName** | **String** | The user from whom the invite originates  |  [optional]
**message** | **String** | The description of this invite. Typically includes the queue contents and possibly a note from the user.  |  [optional]
**queueId** | **String** | When set, indicates this is an invitation to play a game. The queue ID to put into the matchmaking request to fulfill this invite.  |  [optional]
**friendId** | **String** | When set, indicates this is an invitation to become friends.  |  [optional]
**expiresAt** | **Long** | An expiration timestamp.  |  [optional]
**status** | [**StatusEnum**](#StatusEnum) | Indicates the status of the invite.  * UNDELIVERED: The invitation was created and is awaiting delivery, either due to ordinary networking delay    or because the recipient is not yet online.  * PENDING: The invitation is delivered and awaiting a response.  * TIMEOUT: The recipient did not respond by the expiration time and the invitation expired.  * ACCEPTED: The recipient accepted the invitation. The sender should enter the queue if they haven&#39;t already    done so.  * REJECTED: The recipient rejected the invitation.  * CANCELLED: The sender cancelled the invitation.  |  [optional]


<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
UNDELIVERED | &quot;UNDELIVERED&quot;
PENDING | &quot;PENDING&quot;
TIMEOUT | &quot;TIMEOUT&quot;
ACCEPTED | &quot;ACCEPTED&quot;
REJECTED | &quot;REJECTED&quot;
CANCELLED | &quot;CANCELLED&quot;



