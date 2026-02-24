# Spell Reference

Spell classes live in `spellsource-game/src/main/java/net/demilich/metastone/game/spells/`. In JSON the `"class"` field is the class name without package prefix. Custom spells in the `spells/custom/` package use a `"custom."` prefix.

All spell args are written in camelCase in JSON. `SpellArg.ATTACK_BONUS` becomes `"attackBonus"`. `SpellArg.RANDOM_TARGET` becomes `"randomTarget"`.

## Damage and Removal

**DamageSpell** — deals damage. Args: `value` (amount), `target`. Set `"ignoreSpellDamage": true` to bypass spell damage bonuses.
```json
{ "class": "DamageSpell", "target": "ALL_ENEMY_MINIONS", "value": 2 }
```

**DestroySpell** — destroys an actor without dealing damage. Triggers deathrattles.
```json
{ "class": "DestroySpell", "target": "ENEMY_MINIONS", "randomTarget": true }
```

**SilenceSpell** — removes all enchantments and attributes from an actor.
```json
{ "class": "SilenceSpell", "target": "ALL_MINIONS" }
```

## Summoning

**SummonSpell** — summons minions. `card`/`cards` specify what to summon. `value` controls count. `spell` is a subspell cast on each summoned minion (use `OUTPUT` to reference it). `boardPositionRelative` places it relative to the source. `exclusive` prevents duplicates already on board.
```json
{ "class": "SummonSpell", "card": "token_spiderling", "value": 2 }
```
```json
{
  "class": "SummonSpell",
  "card": "minion_bloodfen_raptor",
  "spell": {
    "class": "BuffSpell",
    "target": "OUTPUT",
    "attackBonus": 2
  }
}
```

## Buffs and Stat Changes

**BuffSpell** — adds attack/HP/armor. Args: `attackBonus`, `hpBonus`, `armorBonus`, or `value` for both attack and HP.
```json
{ "class": "BuffSpell", "target": "FRIENDLY_MINIONS", "attackBonus": 1, "hpBonus": 1 }
```

**SetAttackSpell** — sets attack to a fixed value. `value` is the target attack.
```json
{ "class": "SetAttackSpell", "value": 1 }
```

**SetHpSpell** — sets HP to a fixed value. `value` is the target HP.
```json
{ "class": "SetHpSpell", "value": 1 }
```

**AddAttributeSpell** — adds an attribute flag. `revertTrigger` removes it on an event.
```json
{ "class": "AddAttributeSpell", "attribute": "STEALTH" }
```
With temporary duration:
```json
{
  "class": "AddAttributeSpell",
  "attribute": "STEALTH",
  "revertTrigger": { "class": "TurnStartTrigger", "targetPlayer": "SELF" }
}
```

**RemoveAttributeSpell** — removes an attribute.
```json
{ "class": "RemoveAttributeSpell", "target": "ALL_MINIONS", "attribute": "DIVINE_SHIELD" }
```

## Card Generation

**ReceiveCardSpell** — puts cards into the player's hand. `card`/`cards` for specific cards. `cardFilter`/`cardSource` for random cards. `value` for copy count. Subspell on each card via `spell` with `OUTPUT`.
```json
{ "class": "ReceiveCardSpell", "cards": ["token_lil_bro"] }
```
Random card:
```json
{
  "class": "ReceiveCardSpell",
  "cardFilter": { "class": "CardFilter", "cardType": "MINION", "rarity": "LEGENDARY" },
  "cardSource": { "class": "UnweightedCatalogueSource" }
}
```

**DrawCardSpell** — draws from the player's deck. `value` is the number of cards.
```json
{ "class": "DrawCardSpell", "value": 2 }
```

**ShuffleToDeckSpell** — shuffles cards into a deck. `card`/`cards` for specifics. `howMany` for copies.
```json
{ "class": "ShuffleToDeckSpell", "card": "spell_some_card", "howMany": 3 }
```

**DiscoverSpell** — presents the player with choices. `howMany` controls the number of options (default 3). `spell` is cast on the chosen card. `spell2` is cast on unchosen cards.
```json
{
  "class": "DiscoverSpell",
  "cardFilter": { "class": "CardFilter", "cardType": "SPELL" },
  "cardSource": { "class": "CatalogueSource" },
  "spell": { "class": "ReceiveCardSpell" }
}
```

**CopyCardSpell** — copies a card including its enchantments and cost modifiers.
```json
{ "class": "CopyCardSpell", "target": "ENEMY_HAND", "randomTarget": true }
```

## Card Manipulation

**TransformMinionSpell** — transforms a minion into another card.
```json
{ "class": "TransformMinionSpell", "card": "minion_sheep" }
```

**ReturnTargetToHandSpell** — bounces a minion back to its owner's hand.
```json
{ "class": "ReturnTargetToHandSpell" }
```

**CardCostModifierSpell** — applies a cost modifier. Requires a `cardCostModifier` object.
```json
{
  "class": "CardCostModifierSpell",
  "target": "FRIENDLY_HAND",
  "cardCostModifier": {
    "class": "CardCostModifier",
    "target": "FRIENDLY_HAND",
    "value": 1,
    "operation": "SUBTRACT"
  }
}
```

## Control Flow

**MetaSpell** — executes multiple spells in sequence. `spells` is the array. `howMany` repeats the sequence.
```json
{
  "class": "MetaSpell",
  "spells": [
    { "class": "DamageSpell", "target": "ENEMY_HERO", "value": 2 },
    { "class": "DrawCardSpell", "value": 1 }
  ]
}
```

**ConditionalSpell** — casts `spell` only if `condition` is met.
```json
{
  "class": "ConditionalSpell",
  "condition": { "class": "MinionCountCondition", "targetPlayer": "SELF", "value": 3, "operation": "GREATER_OR_EQUAL" },
  "spell": { "class": "DrawCardSpell", "value": 1 }
}
```

**RandomlyCastSpell** — randomly picks one spell from `spells`.
```json
{
  "class": "RandomlyCastSpell",
  "spells": [
    { "class": "DamageSpell", "value": 3 },
    { "class": "BuffSpell", "attackBonus": 3 }
  ]
}
```

**NullSpell** — does nothing. Placeholder when a spell field is required but no effect is wanted.

## Enchantments

**AddEnchantmentSpell** — attaches a trigger or aura to a target entity. `trigger` for an EnchantmentDesc. `aura` for an AuraDesc.
```json
{
  "class": "AddEnchantmentSpell",
  "target": "FRIENDLY_PLAYER",
  "trigger": {
    "eventTrigger": { "class": "TurnStartTrigger", "targetPlayer": "SELF" },
    "spell": { "class": "DrawCardSpell", "value": 1 }
  }
}
```

**AddDeathrattleSpell** — adds a deathrattle to a minion. The deathrattle is a SpellDesc in the `spell` arg.

## Hero

**ChangeHeroPowerSpell** — replaces the hero power. `card` is the new hero power card ID.
```json
{ "class": "ChangeHeroPowerSpell", "card": "hero_power_arido" }
```

**ChangeHeroSpell** — replaces the hero entirely. `card` is the hero card ID.

## Event Triggers

These go in the `"class"` field inside `"eventTrigger"`. Common args on all triggers: `targetPlayer` (`"SELF"`, `"OPPONENT"`, `"BOTH"`), `sourcePlayer`, `fireCondition`, `queueCondition`.

**TurnStartTrigger** / **TurnEndTrigger** — fires at start/end of a turn.
```json
{ "class": "TurnEndTrigger", "targetPlayer": "SELF" }
```

**MinionSummonedTrigger** — fires after a minion is summoned.

**MinionDeathTrigger** — fires when a minion dies. Filter by `race` or other properties.

**AfterPhysicalAttackTrigger** — fires after a physical attack resolves.

**CardPlayedTrigger** — fires when a card is played from hand. Filter by `cardType`.

**AfterSpellCastedTrigger** — fires after a spell finishes. `EVENT_TARGET` is the spell's target. `EVENT_SOURCE` is the spell card.

**DamageReceivedTrigger** — fires when an entity takes damage.

**HealingTrigger** — fires when an entity is healed.

**CardDrawnTrigger** — fires when a card is drawn from deck.

**HeroPowerUsedTrigger** — fires when a hero power is used.

**GameStartTrigger** — fires once at game start. Use with `gameTriggers` on the card.

**TargetAcquisitionTrigger** — fires when an attack target is selected. Filter by `actionType`, `sourceEntityType`, `sourcePlayer`.

Trigger args (from `EventTriggerArg`): `class`, `targetPlayer`, `sourcePlayer`, `cardType`, `sourceType`, `sourceEntityType`, `targetEntityType`, `race`, `actionType`, `hostTargetType`, `requiredAttribute`, `target`, `fireCondition`, `queueCondition`.

## Value Providers

Value providers compute dynamic integers. Use them wherever an int is expected by writing an object with `"class"` instead of a plain number.

**EntityCountValueProvider** — counts entities matching a target and filter.
```json
{ "class": "EntityCountValueProvider", "target": "FRIENDLY_MINIONS" }
```

**BoardCountValueProvider** — counts minions on the board for the specified player.
```json
{ "class": "BoardCountValueProvider", "targetPlayer": "SELF" }
```

**AttributeValueProvider** — reads an attribute value from an entity.
```json
{ "class": "AttributeValueProvider", "target": "FRIENDLY_HERO", "attribute": "ARMOR" }
```

**PlayerAttributeValueProvider** — reads a player-level attribute like `HAND_COUNT`, `DECK_COUNT`, `MINION_COUNT`.
```json
{ "class": "PlayerAttributeValueProvider", "playerAttribute": "HAND_COUNT", "targetPlayer": "SELF" }
```

**AlgebraicValueProvider** — does math on two values. `operation` is `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MODULO`, `MINIMUM`, `MAXIMUM`, `NEGATE`.
```json
{
  "class": "AlgebraicValueProvider",
  "operation": "SUBTRACT",
  "value1": 7,
  "value2": { "class": "BoardCountValueProvider" }
}
```

**GameValueProvider** — reads game-level values like `SPELL_VALUE` (the value computed by a parent MetaSpell).

## Filters

Filters go in `"filter"` on spells (filters targets) or `"cardFilter"` (filters cards from a source).

**CardFilter** — filters by card properties: `cardType`, `manaCost`, `rarity`, `race`, `heroClass`, `attribute`. Supports `operation` for numeric comparisons (`EQUAL`, `GREATER`, `LESS`, `HAS`).
```json
{ "class": "CardFilter", "cardType": "MINION", "race": "BEAST" }
```

**RaceFilter** — filters entities by race/tribe.
```json
{ "class": "RaceFilter", "race": "MURLOC" }
```

**AttributeFilter** — filters entities that have a specific attribute.
```json
{ "class": "AttributeFilter", "attribute": "TAUNT" }
```

**SpecificCardFilter** — filters for a specific card ID.
```json
{ "class": "SpecificCardFilter", "card": "minion_some_card" }
```

**ManaCostFilter** — filters by mana cost with comparison.
```json
{ "class": "ManaCostFilter", "value": 3, "operation": "LESS_OR_EQUAL" }
```

**AndFilter** / **OrFilter** — combines multiple filters.
```json
{ "class": "AndFilter", "filters": [{ "class": "CardFilter", "cardType": "MINION" }, { "class": "RaceFilter", "race": "DRAGON" }] }
```
