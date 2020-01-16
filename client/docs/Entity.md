
# Entity

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Integer** | The entity&#39;s ID in the game. |  [optional]
**cardId** | **String** | The entity&#39;s Card ID. When null, it typically should not be rendered. |  [optional]
**entityType** | [**EntityTypeEnum**](#EntityTypeEnum) | Broad categories describing this entity and how it should be rendered.  |  [optional]
**name** | **String** | The text that would go into the entity&#39;s name field.  |  [optional]
**description** | **String** | The text that would go into the entity&#39;s description field.  |  [optional]
**l** | [**EntityLocation**](EntityLocation.md) |  |  [optional]
**background** | **String** | When not null, override the default background for this entity&#39;s card ID with this key corresponding to backgrounds from the server.  |  [optional]
**portrait** | **String** | When not null, override the default portrait for this entity&#39;s card ID with this key corresponding to portraits from the server.  |  [optional]
**gold** | **Boolean** | Render this entity with a \&quot;gold\&quot; effect. |  [optional]
**boardPosition** | **Integer** | The index of the entity in its zone. |  [optional]
**owner** | **Integer** | An integer corresponding to the entity&#39;s owner. |  [optional]
**heroClass** | **String** | The string enum value that corresponds to this entity&#39;s hero class. |  [optional]
**baseHp** | **Integer** | The base hitpoints of the entity. |  [optional]
**hp** | **Integer** | The current hitpoints of the entity. Conventionally, this value should be rendered on the hitpoints token.  |  [optional]
**durability** | **Integer** | The durability (number of uses) that the weapon still has. |  [optional]
**maxHp** | **Integer** | The maximum number of hitpoints this entity can have. |  [optional]
**baseAttack** | **Integer** | The entity&#39;s base attack value. |  [optional]
**attack** | **Integer** | The entity&#39;s current attack value. Conventionally, this value should be rendered on the attack token.  |  [optional]
**baseManaCost** | **Integer** | The entity&#39;s base mana cost.  |  [optional]
**manaCost** | **Integer** | The entity&#39;s current mana cost. Conventionally, this value should be rendered on the mana token.  |  [optional]
**armor** | **Integer** | The entity&#39;s armor. Conventionally, this value should be rendered on a hero entity&#39;s armor token.  |  [optional]
**destroyed** | **Boolean** | When true, indicates that this entity is destroyed. During event evaluation, an entity can be destroyed but still in a zone other than the graveyard; render a death icon over the entity when it is so marked.  |  [optional]
**summoningSickness** | **Boolean** | When true, the entity cannot attack this turn because it has \&quot;summoning sickness,\&quot; or a disability related to the first turn the entity came into play. Typically rendered with snooze icons.  |  [optional]
**frozen** | **Boolean** | When true, the entity cannot attack because a spell casted on it prevents it so, until the next turn when it would normally be able to attack.  |  [optional]
**uncensored** | **Boolean** | When true, indicates that this entity that is ordinarily censored to this user is not. It can be \&quot;flipped\&quot; and shown to the opponent.  |  [optional]
**deflect** | **Boolean** | When true, the entity&#39;s first incoming hit will hit its owner rather than itself.  |  [optional]
**silenced** | **Boolean** | Indicates that the entity was silenced.  |  [optional]
**windfury** | **Boolean** | Indicates the entity can attack twice a turn.  |  [optional]
**permanent** | **Boolean** | Indicates the entity is an on-battlefield permanent.  |  [optional]
**collectible** | **Boolean** | Indicates the card is collectible - valid for putting into decks.  |  [optional]
**taunt** | **Boolean** | Indicates the entity and other taunt entities must be targeted by enemy actors first during an opponent&#39;s physical attack action targeting.  |  [optional]
**discarded** | **Boolean** | Indicates the entity was discarded from the hand.  |  [optional]
**roasted** | **Boolean** | Indicates the entity was roasted (removed due to excess cards or otherwise discarded from the deck).  |  [optional]
**spellDamage** | **Integer** | Indicates the amount of additional spell damage this entity gives its owning player.  |  [optional]
**charge** | **Boolean** | When true, the entity can attack the same turn it is summoned.  |  [optional]
**rush** | **Boolean** | When true, the entity can attack a minion the same turn it is summoned.  |  [optional]
**lifesteal** | **Boolean** | When true, the entity heals its owner when it deals damage.  |  [optional]
**poisonous** | **Boolean** | When true, the entity will destroy any target it damages.  |  [optional]
**enraged** | **Boolean** | When true, this entity is under the influence of \&quot;enrage,\&quot; or a bonus when it takes damage the first time.  |  [optional]
**battlecry** | **Boolean** | When true, this entity has an effect that gets triggered when it is played from the hand.  |  [optional]
**deathrattles** | **Boolean** | When true, this entity has an effect that gets triggered when it is destroyed.  |  [optional]
**immune** | **Boolean** | Indicates the entity does not take damage.  |  [optional]
**divineShield** | **Boolean** | When true, the entity will take no loss in hitpoints the first time it would ordinarily take damage.  |  [optional]
**stealth** | **Boolean** | When true, the minion cannot be targeted by the opponent until the entity attacks for the first time.  |  [optional]
**combo** | **Boolean** | Indicates this minion has a combo effect.  |  [optional]
**overload** | **Integer** | Indicates the amount of mana that would be locked if this card were played.  |  [optional]
**chooseOne** | **Boolean** | Indicates this card has a choose-one effect.  |  [optional]
**untargetableBySpells** | **Boolean** | Indicates this entity cannot be targeted by spells.  |  [optional]
**cannotAttack** | **Boolean** | When true, indicates this minion cannot attack, even though it normally can. |  [optional]
**underAura** | **Boolean** | When true, indicates this minion is benefiting from the aura of another effect. |  [optional]
**customRenderer** | **String** |  |  [optional]
**customData** | **String** |  |  [optional]
**playable** | **Boolean** | When true, indicates the card can be played, or the hero / minion can initiate a physical attack. |  [optional]
**conditionMet** | **Boolean** | When true, indicates that a condition written on the card is met and the player should be informed. |  [optional]
**mana** | **Integer** | The player&#39;s current mana. |  [optional]
**maxMana** | **Integer** | The player&#39;s maximum amount of mana. |  [optional]
**lockedMana** | **Integer** | The amount of mana that was locked due to overload. |  [optional]
**hostsTrigger** | **Boolean** | When true, indicates this entity has an effect that triggers on game events. |  [optional]
**note** | **String** | A renderable note attached to this entity. |  [optional]
**cardType** | [**CardTypeEnum**](#CardTypeEnum) | When not null, indicates this card entity has a specified type. |  [optional]
**tribe** | **String** | When not null, indicates the card&#39;s tribe/race. Typically only minions have this field set. |  [optional]
**fires** | **Integer** | The number of times this enchantment (secret, quest or trigger on card) has fired. |  [optional]
**countUntilCast** | **Integer** | The number of times this enchantment (secret, quest or trigger on card) must fire before its spell effect is triggered.  |  [optional]
**cardSet** | **String** | The card expansion set this entity belongs to.  |  [optional]
**rarity** | [**RarityEnum**](#RarityEnum) | The rarity of the card  |  [optional]
**gameStarted** | **Boolean** | For player entities, indicates whether or not the player has finished the mulligan phase and is awaiting the other player to finish mulligan or, if both players have this field as true, indicates the game has begun on turn 0.  |  [optional]
**color** | **List&lt;Float&gt;** | The color for the class  |  [optional]
**blackText** | **Boolean** | Whether the class should use black text instead of the default white  |  [optional]
**cardSets** | **List&lt;String&gt;** | The card sets listed by the card  |  [optional]


<a name="EntityTypeEnum"></a>
## Enum: EntityTypeEnum
Name | Value
---- | -----
PLAYER | &quot;PLAYER&quot;
HERO | &quot;HERO&quot;
CARD | &quot;CARD&quot;
MINION | &quot;MINION&quot;
WEAPON | &quot;WEAPON&quot;
SECRET | &quot;SECRET&quot;
QUEST | &quot;QUEST&quot;
ENCHANTMENT | &quot;ENCHANTMENT&quot;


<a name="CardTypeEnum"></a>
## Enum: CardTypeEnum
Name | Value
---- | -----
HERO | &quot;HERO&quot;
MINION | &quot;MINION&quot;
SPELL | &quot;SPELL&quot;
WEAPON | &quot;WEAPON&quot;
HERO_POWER | &quot;HERO_POWER&quot;
CHOOSE_ONE | &quot;CHOOSE_ONE&quot;
CLASS | &quot;CLASS&quot;
FORMAT | &quot;FORMAT&quot;


<a name="RarityEnum"></a>
## Enum: RarityEnum
Name | Value
---- | -----
FREE | &quot;FREE&quot;
COMMON | &quot;COMMON&quot;
RARE | &quot;RARE&quot;
EPIC | &quot;EPIC&quot;
LEGENDARY | &quot;LEGENDARY&quot;
ALLIANCE | &quot;ALLIANCE&quot;



