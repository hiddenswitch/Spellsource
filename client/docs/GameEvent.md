
# GameEvent

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Integer** | An integer ID corresponding to the order of this event from the client&#39;s point of view.  |  [optional]
**description** | **String** | A plaintext description of this event.  |  [optional]
**isPowerHistory** | **Boolean** | Should this event be rendered in the power history?  |  [optional]
**eventType** | [**EventTypeEnum**](#EventTypeEnum) | The game event type corresponding to this game event.  |  [optional]
**afterPhysicalAttack** | [**PhysicalAttackEvent**](PhysicalAttackEvent.md) |  |  [optional]
**drawCard** | [**CardEvent**](CardEvent.md) |  |  [optional]
**entityTouched** | **Integer** |  |  [optional]
**entityUntouched** | **Integer** |  |  [optional]
**preDamage** | [**GameEventPreDamage**](GameEventPreDamage.md) |  |  [optional]
**silence** | [**GameEventSilence**](GameEventSilence.md) |  |  [optional]
**secretPlayed** | [**GameEventSecretPlayed**](GameEventSecretPlayed.md) |  |  [optional]
**beforeSummon** | [**GameEventBeforeSummon**](GameEventBeforeSummon.md) |  |  [optional]
**cardPlayed** | [**CardEvent**](CardEvent.md) |  |  [optional]
**armorGained** | [**GameEventArmorGained**](GameEventArmorGained.md) |  |  [optional]
**afterSummon** | [**GameEventBeforeSummon**](GameEventBeforeSummon.md) |  |  [optional]
**spellCasted** | [**CardEvent**](CardEvent.md) |  |  [optional]
**joust** | [**GameEventJoust**](GameEventJoust.md) |  |  [optional]
**weaponDestroyed** | [**GameEventWeaponDestroyed**](GameEventWeaponDestroyed.md) |  |  [optional]
**heroPowerUsed** | [**GameEventHeroPowerUsed**](GameEventHeroPowerUsed.md) |  |  [optional]
**enrageChanged** | [**GameEventSilence**](GameEventSilence.md) |  |  [optional]
**targetAcquisition** | [**GameEventTargetAcquisition**](GameEventTargetAcquisition.md) |  |  [optional]
**damage** | [**GameEventDamage**](GameEventDamage.md) |  |  [optional]
**weaponEquipped** | [**GameEventWeaponDestroyed**](GameEventWeaponDestroyed.md) |  |  [optional]
**performedGameAction** | [**GameEventPerformedGameAction**](GameEventPerformedGameAction.md) |  |  [optional]
**physicalAttack** | [**PhysicalAttackEvent**](PhysicalAttackEvent.md) |  |  [optional]
**overload** | [**GameEventOverload**](GameEventOverload.md) |  |  [optional]
**heal** | [**GameEventHeal**](GameEventHeal.md) |  |  [optional]
**secretRevealed** | [**GameEventSecretRevealed**](GameEventSecretRevealed.md) |  |  [optional]
**questSuccessful** | [**GameEventQuestSuccessful**](GameEventQuestSuccessful.md) |  |  [optional]
**questPlayed** | [**GameEventQuestSuccessful**](GameEventQuestSuccessful.md) |  |  [optional]
**summon** | [**GameEventBeforeSummon**](GameEventBeforeSummon.md) |  |  [optional]
**afterSpellCasted** | [**GameEventAfterSpellCasted**](GameEventAfterSpellCasted.md) |  |  [optional]
**discard** | [**CardEvent**](CardEvent.md) |  |  [optional]
**mill** | [**CardEvent**](CardEvent.md) |  |  [optional]
**kill** | [**GameEventKill**](GameEventKill.md) |  |  [optional]
**triggerFired** | [**GameEventTriggerFired**](GameEventTriggerFired.md) |  |  [optional]


<a name="EventTypeEnum"></a>
## Enum: EventTypeEnum
Name | Value
---- | -----
ALL | &quot;ALL&quot;
AFTER_PHYSICAL_ATTACK | &quot;AFTER_PHYSICAL_ATTACK&quot;
AFTER_SPELL_CASTED | &quot;AFTER_SPELL_CASTED&quot;
AFTER_SUMMON | &quot;AFTER_SUMMON&quot;
ATTRIBUTE_APPLIED | &quot;ATTRIBUTE_APPLIED&quot;
ARMOR_GAINED | &quot;ARMOR_GAINED&quot;
BEFORE_SUMMON | &quot;BEFORE_SUMMON&quot;
BOARD_CHANGED | &quot;BOARD_CHANGED&quot;
DAMAGE | &quot;DAMAGE&quot;
DISCARD | &quot;DISCARD&quot;
DRAW_CARD | &quot;DRAW_CARD&quot;
ENRAGE_CHANGED | &quot;ENRAGE_CHANGED&quot;
ENTITY_TOUCHED | &quot;ENTITY_TOUCHED&quot;
ENTITY_UNTOUCHED | &quot;ENTITY_UNTOUCHED&quot;
GAME_START | &quot;GAME_START&quot;
HEAL | &quot;HEAL&quot;
HERO_POWER_USED | &quot;HERO_POWER_USED&quot;
JOUST | &quot;JOUST&quot;
KILL | &quot;KILL&quot;
LOSE_DIVINE_SHIELD | &quot;LOSE_DIVINE_SHIELD&quot;
MILL | &quot;MILL&quot;
MAX_MANA | &quot;MAX_MANA&quot;
OVERLOAD | &quot;OVERLOAD&quot;
PERFORMED_GAME_ACTION | &quot;PERFORMED_GAME_ACTION&quot;
PHYSICAL_ATTACK | &quot;PHYSICAL_ATTACK&quot;
PLAY_CARD | &quot;PLAY_CARD&quot;
PRE_DAMAGE | &quot;PRE_DAMAGE&quot;
QUEST_PLAYED | &quot;QUEST_PLAYED&quot;
QUEST_SUCCESSFUL | &quot;QUEST_SUCCESSFUL&quot;
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



