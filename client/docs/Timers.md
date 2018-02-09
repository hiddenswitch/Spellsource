
# Timers

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**millisRemaining** | **Long** | The number of milliseconds remaining before the server will end the mulligan or turn. When null or less than zero, no timer is set. This property will be valid with respect to the last timestamped message from the server. Since typically emotes and touches are not timestamped, while other game state messages are, this property will be updated with actions and data. It is the responsibility of the client to lerp the millis-remaining values with the actual animated timer to prevent choppy animation.  |  [optional]



