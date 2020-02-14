
# TargetActionPair

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**target** | **Integer** | The corresponding target for this action. When -1, this indicates there is no target for this particular pair. This is relevant for summon actions which would ordinarily have a null value. A -1 here indicates the rightmost position on the battlefield.  |  [optional]
**friendlyBattlefieldIndex** | **Integer** | The corresponding index on the friendly side of the battlefield for a summon action. The minion will be summoned to the left of this index.  |  [optional]
**action** | **Integer** |  |  [optional]



