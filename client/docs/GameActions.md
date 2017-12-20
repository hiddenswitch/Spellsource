
# GameActions

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**compatibility** | **List&lt;Integer&gt;** | An array of game action indices. Choose one at random for compatibility purposes until the writer can support all actions  |  [optional]
**endTurn** | **Integer** | The end turn action. Not necessarily always available, because you cannot end your turn in the middle of a discover or a battlecry. The value corresponds to which integer index to reply with.  |  [optional]
**physicalAttacks** | [**List&lt;GameActionsPhysicalAttacks&gt;**](GameActionsPhysicalAttacks.md) | An array of entity ID - target IDs pairs that represent valid physical attacks.  |  [optional]
**summons** | [**List&lt;SummonAction&gt;**](SummonAction.md) | The cards in your hand that can be summoned. These are typically only minions.  |  [optional]
**heroPower** | [**SpellAction**](SpellAction.md) |  |  [optional]
**heroes** | [**List&lt;SpellAction&gt;**](SpellAction.md) | The cards in your hand that are heroes |  [optional]
**spells** | [**List&lt;SpellAction&gt;**](SpellAction.md) | The cards in your hand that are spells that take targets.  |  [optional]
**battlecries** | [**List&lt;SpellAction&gt;**](SpellAction.md) | A set of possible targetable battlecry actions.  |  [optional]
**discoveries** | [**List&lt;GameActionsDiscoveries&gt;**](GameActionsDiscoveries.md) | Card discovers.  |  [optional]
**weapons** | [**List&lt;SummonAction&gt;**](SummonAction.md) | The weapons in your hand that can be equipped. These are the equivalent of summons that only have a single index, but do not require targeting on the battlefield.  |  [optional]
**chooseOnes** | [**List&lt;ChooseOneOptions&gt;**](ChooseOneOptions.md) | An array of choose one spells or summons. Each spell/summon is represented by a different card. An entity that can be used to render a card for the option is provided. Use the spell action&#39;s card ID to figure out which options correspond to which cards in the hand.  |  [optional]



