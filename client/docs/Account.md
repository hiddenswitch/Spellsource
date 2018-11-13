
# Account

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** | The user ID |  [optional]
**name** | **String** | The username that is displayed to toher players |  [optional]
**privacyToken** | **String** | The token that is appended to the end of the user&#39;s name to allow friending without sharing an e-mail address.  |  [optional]
**email** | **String** | The user&#39;s email address |  [optional]
**friends** | [**List&lt;Friend&gt;**](Friend.md) | The user&#39;s friends at the moment of receiving this account document. This may be out of date as the latest friends information will come from receiving friend documents.  |  [optional]
**decks** | [**List&lt;InventoryCollection&gt;**](InventoryCollection.md) | A list of decks belonging to the player |  [optional]
**inMatch** | **Boolean** | True if the client should attempt to connect to a match with its token.  |  [optional]
**personalCollection** | [**InventoryCollection**](InventoryCollection.md) |  |  [optional]



