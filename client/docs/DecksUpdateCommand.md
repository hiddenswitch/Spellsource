
# DecksUpdateCommand

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**setHeroClass** | **String** | Sets the hero class of the deck in this command. If the deck now contains cards that no longer belong to this hero class, the deck becomes invalid under standard rules.  |  [optional]
**setName** | **String** | Sets the name of the deck in this command. If the name is null, the deck becomes invalid.  |  [optional]
**setInventoryIds** | **List&lt;String&gt;** | Sets the entire deck&#39;s inventory IDs in this command. Duplicate inventory IDs will cause the update to be rejected. If the user does not own these inventory IDs, the deck becomes invalid. Under standard rules, duplicate card IDs also make the deck invalid. Finally, adding cards whose hero class isn&#39;t neutral or the same as the deck&#39;s hero class marks the deck as invalid.  |  [optional]
**pushInventoryIds** | [**DecksUpdateCommandPushInventoryIds**](DecksUpdateCommandPushInventoryIds.md) |  |  [optional]
**pushCardIds** | [**DecksUpdateCommandPushCardIds**](DecksUpdateCommandPushCardIds.md) |  |  [optional]
**pullAllInventoryIds** | **List&lt;String&gt;** | Removes all the specified inventory IDs from the user&#39;s deck. Does nothing if the deck does not contain any of the specified inventory IDs. This method will still succeed for inventory IDs that are found.  |  [optional]
**pullAllCardIds** | **List&lt;String&gt;** | Removes all the specified card IDs from the user&#39;s deck. Does nothing if the deck does not contain any of the specified card IDs. This method will still succeed for deck IDs that are found.  |  [optional]



