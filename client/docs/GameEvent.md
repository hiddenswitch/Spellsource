
# GameEvent

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Integer** | An integer ID corresponding to the order of this event from the client&#39;s point of view.  |  [optional]
**isSourcePlayerLocal** | **Boolean** | Stores the source player according to the game event data. Typically this is the player who is casting the card or otherwise the source of an event.  |  [optional]
**isTargetPlayerLocal** | **Boolean** | Stores the target player according to the game event data.  |  [optional]
**entityTouched** | **Integer** | The ID of the entity that has starting being touched by the opponent. |  [optional]
**entityUntouched** | **Integer** | The ID of the entity that is no longer being touched by the opponent. |  [optional]
**source** | [**Entity**](Entity.md) |  |  [optional]
**target** | [**Entity**](Entity.md) |  |  [optional]
**value** | **Integer** | When not null, indicates this game event comes with a value. This is typically the damage dealt, the amount of healing, etc.  |  [optional]
**description** | **String** | A plain text description of this event that should be shown to the user.  |  [optional]
**isPowerHistory** | **Boolean** | Should this event be rendered in the power history?  |  [optional]
**eventType** | [**EventTypeEnum**](#EventTypeEnum) | The game event type corresponding to this game event.  |  [optional]
**cardEvent** | [**CardEvent**](CardEvent.md) |  |  [optional]
**performedGameAction** | [**GameEventPerformedGameAction**](GameEventPerformedGameAction.md) |  |  [optional]
**joust** | [**GameEventJoust**](GameEventJoust.md) |  |  [optional]
**damage** | [**GameEventDamage**](GameEventDamage.md) |  |  [optional]
**triggerFired** | [**GameEventTriggerFired**](GameEventTriggerFired.md) |  |  [optional]


<a name="EventTypeEnum"></a>
## Enum: EventTypeEnum
Name | Value
---- | -----
ALL | &quot;ALL&quot;
AFTER_PHYSICAL_ATTACK | &quot;AFTER_PHYSICAL_ATTACK&quot;
AFTER_PLAY_CARD | &quot;AFTER_PLAY_CARD&quot;
AFTER_SPELL_CASTED | &quot;AFTER_SPELL_CASTED&quot;
AFTER_SUMMON | &quot;AFTER_SUMMON&quot;
ATTRIBUTE_APPLIED | &quot;ATTRIBUTE_APPLIED&quot;
ARMOR_GAINED | &quot;ARMOR_GAINED&quot;
BEFORE_PHYSICAL_ATTACK | &quot;BEFORE_PHYSICAL_ATTACK&quot;
BEFORE_SUMMON | &quot;BEFORE_SUMMON&quot;
BOARD_CHANGED | &quot;BOARD_CHANGED&quot;
CARD_ADDED_TO_DECK | &quot;CARD_ADDED_TO_DECK&quot;
CARD_SHUFFLED | &quot;CARD_SHUFFLED&quot;
DAMAGE | &quot;DAMAGE&quot;
DECAY | &quot;DECAY&quot;
DID_END_SEQUENCE | &quot;DID_END_SEQUENCE&quot;
DISCARD | &quot;DISCARD&quot;
DISCOVER | &quot;DISCOVER&quot;
DRAIN | &quot;DRAIN&quot;
DRAW_CARD | &quot;DRAW_CARD&quot;
ENRAGE_CHANGED | &quot;ENRAGE_CHANGED&quot;
ENTITY_TOUCHED | &quot;ENTITY_TOUCHED&quot;
ENTITY_UNTOUCHED | &quot;ENTITY_UNTOUCHED&quot;
FATIGUE | &quot;FATIGUE&quot;
GAME_START | &quot;GAME_START&quot;
HEAL | &quot;HEAL&quot;
HERO_POWER_USED | &quot;HERO_POWER_USED&quot;
INVOKED | &quot;INVOKED&quot;
JOUST | &quot;JOUST&quot;
KILL | &quot;KILL&quot;
LOSE_DIVINE_SHIELD | &quot;LOSE_DIVINE_SHIELD&quot;
LOSE_DEFLECT | &quot;LOSE_DEFLECT&quot;
LOSE_STEALTH | &quot;LOSE_STEALTH&quot;
MAX_HP_INCREASED | &quot;MAX_HP_INCREASED&quot;
MAX_MANA | &quot;MAX_MANA&quot;
MANA_MODIFIED | &quot;MANA_MODIFIED&quot;
OVERLOAD | &quot;OVERLOAD&quot;
PERFORMED_GAME_ACTION | &quot;PERFORMED_GAME_ACTION&quot;
PHYSICAL_ATTACK | &quot;PHYSICAL_ATTACK&quot;
PLAY_CARD | &quot;PLAY_CARD&quot;
PRE_DAMAGE | &quot;PRE_DAMAGE&quot;
PRE_GAME_START | &quot;PRE_GAME_START&quot;
QUEST_PLAYED | &quot;QUEST_PLAYED&quot;
QUEST_SUCCESSFUL | &quot;QUEST_SUCCESSFUL&quot;
RETURNED_TO_HAND | &quot;RETURNED_TO_HAND&quot;
ROASTED | &quot;ROASTED&quot;
REVEAL_CARD | &quot;REVEAL_CARD&quot;
SECRET_PLAYED | &quot;SECRET_PLAYED&quot;
SECRET_REVEALED | &quot;SECRET_REVEALED&quot;
SPELL_CASTED | &quot;SPELL_CASTED&quot;
SUMMON | &quot;SUMMON&quot;
TARGET_ACQUISITION | &quot;TARGET_ACQUISITION&quot;
TRIGGER_FIRED | &quot;TRIGGER_FIRED&quot;
TURN_END | &quot;TURN_END&quot;
TURN_START | &quot;TURN_START&quot;
SILENCE | &quot;SILENCE&quot;
WEAPON_DESTROYED | &quot;WEAPON_DESTROYED&quot;
WEAPON_EQUIPPED | &quot;WEAPON_EQUIPPED&quot;
WILL_END_SEQUENCE | &quot;WILL_END_SEQUENCE&quot;



