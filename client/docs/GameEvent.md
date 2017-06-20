
# GameEvent

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**eventTarget** | [**Entity**](Entity.md) |  |  [optional]
**eventSource** | [**Entity**](Entity.md) |  |  [optional]
**targetPlayerId** | **Integer** |  |  [optional]
**sourcePlayerId** | **Integer** |  |  [optional]
**description** | **String** |  |  [optional]
**eventType** | [**EventTypeEnum**](#EventTypeEnum) | The game event type corresponding to this game event.  |  [optional]
**afterPhysicalAttack** | [**PhysicalAttackEvent**](PhysicalAttackEvent.md) |  |  [optional]
**drawCard** | [**GameEventDrawCard**](GameEventDrawCard.md) |  |  [optional]
**preDamage** | [**GameEventPreDamage**](GameEventPreDamage.md) |  |  [optional]
**silence** | [**GameEventSilence**](GameEventSilence.md) |  |  [optional]
**secretPlayed** | [**GameEventSecretPlayed**](GameEventSecretPlayed.md) |  |  [optional]
**beforeSummon** | [**GameEventBeforeSummon**](GameEventBeforeSummon.md) |  |  [optional]
**cardPlayed** | [**GameEventCardPlayed**](GameEventCardPlayed.md) |  |  [optional]
**armorGained** | [**GameEventArmorGained**](GameEventArmorGained.md) |  |  [optional]
**afterSummon** | [**GameEventBeforeSummon**](GameEventBeforeSummon.md) |  |  [optional]
**spellCasted** | [**GameEventSpellCasted**](GameEventSpellCasted.md) |  |  [optional]
**joust** | [**GameEventJoust**](GameEventJoust.md) |  |  [optional]
**weaponDestroyed** | [**GameEventWeaponDestroyed**](GameEventWeaponDestroyed.md) |  |  [optional]
**heroPowerUsed** | [**GameEventHeroPowerUsed**](GameEventHeroPowerUsed.md) |  |  [optional]
**cardRevealed** | [**GameEventCardRevealed**](GameEventCardRevealed.md) |  |  [optional]
**enrageChanged** | [**GameEventSilence**](GameEventSilence.md) |  |  [optional]
**targetAcquisition** | [**GameEventTargetAcquisition**](GameEventTargetAcquisition.md) |  |  [optional]
**damage** | [**GameEventDamage**](GameEventDamage.md) |  |  [optional]
**weaponEquipped** | [**GameEventWeaponDestroyed**](GameEventWeaponDestroyed.md) |  |  [optional]
**physicalAttack** | [**PhysicalAttackEvent**](PhysicalAttackEvent.md) |  |  [optional]
**overload** | [**GameEventCardPlayed**](GameEventCardPlayed.md) |  |  [optional]
**heal** | [**GameEventHeal**](GameEventHeal.md) |  |  [optional]
**secretRevealed** | [**GameEventSecretRevealed**](GameEventSecretRevealed.md) |  |  [optional]
**summon** | [**GameEventBeforeSummon**](GameEventBeforeSummon.md) |  |  [optional]
**afterSpellCasted** | [**GameEventAfterSpellCasted**](GameEventAfterSpellCasted.md) |  |  [optional]
**discard** | [**GameEventCardPlayed**](GameEventCardPlayed.md) |  |  [optional]
**kill** | [**GameEventKill**](GameEventKill.md) |  |  [optional]
**triggerFired** | [**GameEventTriggerFired**](GameEventTriggerFired.md) |  |  [optional]


<a name="EventTypeEnum"></a>
## Enum: EventTypeEnum
Name | Value
---- | -----
AFTER_PHYSICAL_ATTACK | &quot;AFTER_PHYSICAL_ATTACK&quot;
AFTER_SPELL_CASTED | &quot;AFTER_SPELL_CASTED&quot;
AFTER_SUMMON | &quot;AFTER_SUMMON&quot;
ARMOR_GAINED | &quot;ARMOR_GAINED&quot;
BEFORE_SUMMON | &quot;BEFORE_SUMMON&quot;
BOARD_CHANGED | &quot;BOARD_CHANGED&quot;
DAMAGE | &quot;DAMAGE&quot;
DISCARD | &quot;DISCARD&quot;
DRAW_CARD | &quot;DRAW_CARD&quot;
ENRAGE_CHANGED | &quot;ENRAGE_CHANGED&quot;
GAME_START | &quot;GAME_START&quot;
HEAL | &quot;HEAL&quot;
HERO_POWER_USED | &quot;HERO_POWER_USED&quot;
JOUST | &quot;JOUST&quot;
KILL | &quot;KILL&quot;
OVERLOAD | &quot;OVERLOAD&quot;
PHYSICAL_ATTACK | &quot;PHYSICAL_ATTACK&quot;
PLAY_CARD | &quot;PLAY_CARD&quot;
PRE_DAMAGE | &quot;PRE_DAMAGE&quot;
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



