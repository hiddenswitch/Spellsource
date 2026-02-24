# Card Validation Errors Reference

Analysis of 215 CardValidationTests failures in `spellsource-cards-private` (internalcontent).

## Error Categories

### 1. Invalid Card Type: LOCATION (28 cards)
`"type": "LOCATION"` does not exist in the protobuf `CardType` enum.

Valid `CardType` values: `HERO`, `MINION`, `SPELL`, `WEAPON`, `HERO_POWER`, `GROUP`, `CHOOSE_ONE`, `ENCHANTMENT`, `CLASS`, `FORMAT`.

**Fix**: Convert to `"type": "SPELL"` with appropriate spell/trigger implementation.

**Affected sets**: murder_at_castle_nathria (10), perils_in_paradise (6), whizbangs_workshop (5), festival_of_legends (3), titans (2), march_of_the_lich_king (1), showdown_in_the_badlands (1).

### 2. Wrong Quest Format (16 cards)
The `quest` field in `CardDesc` expects an `EventTriggerDesc`, not an `EnchantmentDesc`.

**Wrong format** (used in failing cards):
```json
"quest": {
  "class": "Enchantment",
  "eventTrigger": { "class": "...", ... },
  "spell": { ... },
  "countUntilCast": 4
}
```

**Correct format**:
```json
"quest": {
  "class": "TurnEndTrigger",
  "targetPlayer": "SELF"
},
"countUntilCast": 4,
"spell": { ... }
```

The `quest` field is just the trigger; `countUntilCast` and `spell` are top-level CardDesc fields.

### 3. Unrecognized Fields (21 cards)
Fields that don't exist on the expected descriptor classes:

| Field | Count | Context | Fix |
|-------|-------|---------|-----|
| `condition` on EventTriggerDesc | 16 | Quest triggers | Move to `fireCondition` |
| `turnExpiration` | 2 | Unknown context | Remove or restructure |
| `chooseOneOverride` | 1 | Not a CardDesc field | Use ChooseOneOverrideAura |
| `class` on EventTriggerDesc | 2 | `"class": "Enchantment"` | Part of quest format fix |
| `filter` on CardCostModifier | 1 | Not a valid CCM arg | Use `FILTER` via proper arg |

### 4. Invalid Spell/Condition/Filter/Trigger Class Names (45 cards)

| Invalid Class | Count | Correct Class |
|---------------|-------|---------------|
| `NoDuplicatesCondition` | 7 | `HighlanderDeckCondition` |
| `CardCostModifierAura` | 6 | `CardCostInsteadAura` (or restructure with `CardCostModifierSpell`) |
| `EntityCountCondition` | 6 | Use `MinionCountCondition` or `CardCountCondition` |
| `MissileSpell` | 5 | `MissilesSpell` |
| `Secret` (as trigger class) | 4 | Use correct trigger class (e.g., `SpellCastedTrigger`) |
| `ForEachSpell` | 3 | Use `CastRepeatedlySpell` or restructure with MetaSpell |
| `NegateCondition` | 3 | Use `condition.invert: true` on parent, or wrap in `NotCondition` if it exists |
| `SummonCardFromHandSpell` | 2 | Use `SummonSpell` with card source |
| `NotFilter` | 2 | Use `filter.invert: true` or `AndFilter` with invert |
| `RepeatedSpell` | 1 | `CastRepeatedlySpell` |
| `CastRandomSpell` | 1 | `CastRandomSpellSpell` |
| `CardPlayedLastTurnCondition` | 1 | `PlayedLastTurnCondition` |
| `IsDamagedFilter` | 1 | `DamagedFilter` |
| `DeadFilter` | 1 | `IsDestroyedFilter` |
| `ModifyCurrentManaSpell` | 1 | `GainManaSpell` |
| `GraveyardCardSource` | 1 | `GraveyardCardsSource` |
| `GraveyardSource` | 1 | `GraveyardCardsSource` |
| `DamageEventTrigger` | 1 | `DamageCausedTrigger` or `DamageReceivedTrigger` |
| `DamageTrigger` | 1 | `DamageCausedTrigger` or `DamageReceivedTrigger` |
| `KillTrigger` | 1 | `MinionDeathTrigger` |
| `CopyCardSpellsSpell` | 1 | `CopyCardSpell` or `CopyCardEnchantmentsSpell` |
| `StoreEntitiesSpell` | 1 | `custom.StoreEntitySpell` |
| `SwapHandAndDeckSpell` | 1 | `custom.SwapCardsInHandAndDeckSpell` |
| `SummonCardFromDeckSpell` | 1 | Use `SummonSpell` with deck source |
| `SummonCardsFromHandSpell` | 1 | Use `SummonSpell` with hand source |
| `CreateSummonTriggerSpell` | 1 | `CreateSummonSpell` |
| `ManaCrystalCondition` | 1 | `ManaMaxedCondition` or `AttributeCondition` |
| `OwnedByClassFilter` | 1 | `SameHeroClassFilter` or `OwnedByPlayerFilter` |
| `HasQuestCondition` | 1 | Does not exist; use `ControlsSecretCondition` variant or attribute check |
| `CardPlayedThisTurnCondition` | 1 | `SurgeCondition` (combo) or `PlayedLastTurnCondition` |
| `CardCostModifier` (as aura class) | 1 | Not a valid aura class |

### 5. Invalid Enum Values (55 cards)

#### SpellArg (missing values)
| Invalid Value | Count | Fix |
|---------------|-------|-----|
| `MANA_COST_MODIFIER` | 9 | Restructure; use `CARD_COST_MODIFIER` arg or top-level `cardCostModifier` |
| `ATTACK_OVERRIDE` | 5 | Use BuffSpell with `ATTACK_BONUS` or `SetAttributeSpell` |
| `HIGHEST_COST` | 3 | Not a SpellArg; restructure filter logic |
| `SPELLELSECAST` | 2 | Use `ConditionalSpell` with `spell` and `spell2` |
| `RANDOM_COUNT` | 2 | Not a SpellArg; use `HOW_MANY` with random value provider |
| `LOWEST_COST` | 1 | Not a SpellArg; restructure filter logic |
| `SWAP_COST_AND_ATTACK` | 1 | Not a SpellArg; implement differently |
| `ELSE_SPELL` | 1 | Use `ConditionalSpell` with `spell` and `spell2` |
| `BATTLEFIELD_POSITION_RELATIVE` | 1 | Use `BOARD_POSITION_RELATIVE` |
| `MAX_FIRES` | 1 | Not a SpellArg (it's on EnchantmentDesc); restructure |
| `HOW_MANY_TARGETS` | 1 | Not a SpellArg; use filter or multiple casts |
| `CARD_SORTING_DIRECTION` | 1 | Not a SpellArg |

#### Attribute (missing values)
| Invalid Value | Count | Fix |
|---------------|-------|-----|
| `DAMAGED` | 4 | Not an attribute; use `DamagedFilter` or `IsDamagedCondition` |
| `LIBRAM` | 2 | Add custom attribute or use card name filter |
| `MAX_MANA` | 1 | Not an attribute; use `PlayerAttributeValueProvider` with `MAX_MANA` |
| `IGNORES_TAUNT` | 1 | Not an attribute; use `AURA_CANNOT_ATTACK_HEROES` or restructure |
| `WOUNDED` | 1 | Not an attribute; use `DamagedFilter` |
| `HERO_POWER_CAN_TARGET_MINIONS` | 1 | Does not exist |
| `LIFESTEAL_DAMAGES_OPPONENT` | 1 | Does not exist |
| `DAMAGE_PREVENTED` | 1 | Does not exist |
| `HERO_POWER_DAMAGE_DEALT` | 1 | Not an Attribute; it's a `PlayerAttribute` |
| `CASTS_WHEN_DISCARDED` | 1 | Does not exist; implement via trigger |
| `PERMANENT_STEALTH` | 1 | Does not exist; `STEALTH` is already permanent unless removed |

#### CardCostModifierArg (missing values)
| Invalid Value | Count | Fix |
|---------------|-------|-----|
| `EXPIRES_AFTER_PLAYED` | 8 | Use `EXPIRATION_TRIGGER` with `CardPlayedTrigger` |
| `ATTRIBUTE` | 2 | Use `REQUIRED_ATTRIBUTE` |
| `SET_INSTEAD_OF_MODIFY` | 1 | Does not exist |
| `MAX_VALUE` | 1 | Use `MIN_VALUE` with negation logic |
| `EXPIRES_AFTER_TURN_COUNT` | 1 | Use `EXPIRATION_TRIGGER` with turn-based trigger |

#### Other Missing Enums
| Invalid Value | Count | Fix |
|---------------|-------|-----|
| `EventTriggerArg.EVENT_TRIGGER` | 16 | Part of quest format fix |
| `EventTriggerArg.ACTIVE_DURING_TURN` | 1 | Does not exist |
| `EntityFilterArg.HERO_CLASS_NOT` | 1 | Use `AndFilter` with inverted `SameHeroClassFilter` |
| `EntityFilterArg.REQUIRED_ATTRIBUTE` | 1 | Use `ATTRIBUTE` instead |
| `ConditionArg.ENTITY_TYPE` | 1 | Does not exist |
| `AuraArg.CARD_FILTER` | 1 | Use `AuraArg.FILTER` |
| `CardSourceArg.HOW_MANY` | 1 | Does not exist |
| `ComparisonOperation.HAS_NOT` | 1 | Use `HAS` with `invert: true` |
| `ComparisonOperation.MODULO` | 1 | Does not exist; use `AlgebraicOperation.MODULO` in value provider |
| `AlgebraicOperation.MAX` | 1 | Use `MAXIMUM` |
| `ValueProviderArg.DISTINCT_RACES` | 1 | Does not exist |
| `ValueProviderArg.RANDOM_TARGET` | 1 | Not a VP arg; use `RANDOM_TARGET` on SpellArg |
| `PlayerAttribute.HAND_CAPACITY` | 1 | Does not exist |
| `PlayerAttribute.HEALING_THIS_TURN` | 1 | Does not exist |
| `TargetType.IGNORE_HOST` | 2 | Does not exist |

### 6. Missing Attributes on Cards (8 cards)
- 6 cards have description starting with `"Battlecry:"` but missing `"BATTLECRY": true` in attributes
- 2 cards have description starting with `"Deathrattle:"` but missing `"DEATHRATTLES": true` in attributes

### 7. Generic Failures (2 cards)
- `minion_dragon_breeder.json` - unknown crash
- `minion_manari_mosher.json` - unknown crash

## Valid Enum Reference

### CardType
`HERO`, `MINION`, `SPELL`, `WEAPON`, `HERO_POWER`, `GROUP`, `CHOOSE_ONE`, `ENCHANTMENT`, `CLASS`, `FORMAT`

### SpellArg
`AFTERMATH_ID`, `ARMOR_BONUS`, `ATTACK_BONUS`, `ATTRIBUTE`, `AURA`, `BATTLECRY`, `BOARD_POSITION_ABSOLUTE`, `BOARD_POSITION_RELATIVE`, `CANNOT_RECEIVE_OWNED`, `CARD`, `CARD_COST_MODIFIER`, `CARD_DESC_TYPE`, `CARD_FILTER`, `CARD_FILTERS`, `CARD_LOCATION`, `CARDS`, `CARD_SOURCE`, `CARD_SOURCES`, `CARD_TYPE`, `CLASS`, `CONDITION`, `CONDITIONS`, `DESCRIPTION`, `EXCLUSIVE`, `FILTER`, `FULL_MANA_CRYSTALS`, `GROUP`, `HOW_MANY`, `HP_BONUS`, `IGNORE_SPELL_DAMAGE`, `MANA`, `NAME`, `OPERATION`, `PACT`, `QUEST`, `RACE`, `RANDOM_TARGET`, `REVERT_TRIGGER`, `SECONDARY_NAME`, `SECONDARY_TARGET`, `SECONDARY_VALUE`, `SECOND_REVERT_TRIGGER`, `SECRET`, `SOURCE`, `SPELL`, `SPELLS`, `SUMMON_AURA`, `SUMMON_BASE_ATTACK`, `SUMMON_BASE_HP`, `SUMMON_BATTLECRY`, `SUMMON_CHARGE`, `SUMMON_DEATHRATTLE`, `SUMMON_DIVINE_SHIELD`, `SUMMON_STEALTH`, `SUMMON_TAUNT`, `SUMMON_TRIGGERS`, `SUMMON_WINDFURY`, `TARGET`, `TARGET_PLAYER`, `TARGET_SELECTION`, `TRIGGER`, `TRIGGERS`, `VALUE`, `ZONES`

### CardCostModifierArg
`CLASS`, `CARD_TYPE`, `REQUIRED_ATTRIBUTE`, `EXPIRATION_TRIGGER`, `EXPIRATION_TRIGGERS`, `MIN_VALUE`, `VALUE`, `RACE`, `TARGET_PLAYER`, `TOGGLE_ON_TRIGGER`, `TOGGLE_OFF_TRIGGER`, `TARGET`, `FILTER`, `OPERATION`, `CONDITION`

### EventTriggerArg
`CLASS`, `TARGET_PLAYER`, `SOURCE_PLAYER`, `CARD_TYPE`, `SOURCE_TYPE`, `SOURCE_ENTITY_TYPE`, `TARGET_ENTITY_TYPE`, `RACE`, `ACTION_TYPE`, `HOST_TARGET_TYPE`, `REQUIRED_ATTRIBUTE`, `TARGET`, `FIRE_CONDITION`, `QUEUE_CONDITION`, `TARGET_SELECTION`, `VALUE`

### EntityFilterArg
`CLASS`, `TARGET_PLAYER`, `VALUE`, `RACE`, `OPERATION`, `ATTRIBUTE`, `CARD_TYPE`, `RARITY`, `MANA_COST`, `HERO_CLASS`, `HERO_CLASSES`, `CARD`, `CARDS`, `FILTERS`, `INVERT`, `CARD_SET`, `TARGET`, `SECONDARY_TARGET`, `TARGET_SELECTION`, `AND_CONDITION`, `SPELL`, `ENTITY_TYPE`

### AuraArg
`CLASS`, `FILTER`, `TARGET`, `SECONDARY_TARGET`, `ATTRIBUTE`, `VALUE`, `ATTACK_BONUS`, `HP_BONUS`, `APPLY_EFFECT`, `PAY_EFFECT`, `SECONDARY_TRIGGER`, `TRIGGERS`, `ALWAYS_APPLY`, `REVERT_TRIGGER`, `SPELL_CONDITION`, `CONDITION`, `CARD`, `RACES`, `CAN_AFFORD_CONDITION`, `AMOUNT_OF_CURRENCY`, `TARGET_SELECTION`, `CHOOSE_ONE_OVERRIDE`, `PERSISTENT_OWNER`, `SECONDARY_FILTER`, `REMOVE_EFFECT`, `DESCRIPTION`, `NAME`, `SPELL`, `ZONES`

### ValueProviderArg
`CLASS`, `TARGET`, `ATTRIBUTE`, `PLAYER_ATTRIBUTE`, `VALUE`, `OFFSET`, `MULTIPLIER`, `RACE`, `TARGET_PLAYER`, `IF_TRUE`, `IF_FALSE`, `CONDITION`, `FILTER`, `OPERATION`, `VALUE1`, `VALUE2`, `GAME_VALUE`, `MIN`, `MAX`, `CARD_SOURCE`, `CARD_FILTER`, `EVALUATE_ONCE`

### PlayerAttribute
`MANA`, `MAX_MANA`, `HAND_COUNT`, `HERO_POWER_USED`, `DECK_COUNT`, `LAST_MANA_COST`, `SECRET_COUNT`, `SPELLS_CAST`, `OVERLOADED_THIS_GAME`, `CARDS_DISCARDED`, `INVOKED_CARDS`, `SUPREMACIES_THIS_GAME`, `DAMAGE_THIS_TURN`, `LOCKED_MANA`, `HERO_POWER_DAMAGE_DEALT`, `CARDS_DRAWN`, `HEALING_DONE`, `ARMOR_LOST`

### ComparisonOperation
`HAS`, `EQUAL`, `LESS`, `LESS_OR_EQUAL`, `GREATER`, `GREATER_OR_EQUAL`

### AlgebraicOperation
`ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `DIVIDE_ROUNDED`, `POWER`, `SET`, `NEGATE`, `MODULO`, `MINIMUM`, `MAXIMUM`

## Valid Spell Classes (partial list, commonly used)

Located in `spellsource-game/src/main/java/net/demilich/metastone/game/spells/`:

- `BuffSpell`, `DamageSpell`, `DestroySpell`, `DrawCardSpell`, `DiscoverSpell`
- `MissilesSpell`, `HealingMissilesSpell`, `CastRepeatedlySpell`, `CastRandomSpellSpell`
- `MetaSpell`, `ConditionalSpell`, `SummonSpell`, `ReceiveCardSpell`
- `TransformMinionSpell`, `SilenceSpell`, `GainArmorSpell`, `GainManaSpell`
- `HealSpell`, `SetHpSpell`, `SwapAttackAndHpSpell`, `ModifyDurabilitySpell`
- `CardCostModifierSpell`, `AddEnchantmentSpell`, `RemoveEnchantmentSpell`
- `ShuffleToDeckSpell`, `ShuffleMinionToDeckSpell`, `PutOnBottomOfDeckSpell`
- `CopyCardSpell`, `CloneMinionSpell`, `RandomCardTargetSpell`
- `SetAttributeSpell`, `RemoveAttributeSpell`, `ModifyAttributeSpell`
- `CreateSummonSpell`, `ResummonStoredEntitiesSpell`
- `DiscardSpell`, `RevealCardSpell`, `AdjacentEffectSpell`, `FightSpell`
- `ForEachSpell` does NOT exist - use `CastRepeatedlySpell`
- `ModifyCurrentManaSpell` does NOT exist - use `GainManaSpell`

### Custom Spell Classes (in `spells/custom/`)
- `TransformInHandSpell`, `StoreEntitySpell`, `SwapCardsInHandAndDeckSpell`
- `CopyCardEnchantmentsSpell`, `SwapWithHandSpell`

## Valid Condition Classes

`AdjacentToPermanentCondition`, `AllMatchFilterCondition`, `AndCondition`, `AttributeCondition`, `AttributeExistsCondition`, `CardCountCondition`, `CardPropertyCondition`, `ComboCondition`, `ComparisonCondition`, `ConditionalSpell`, `CountCondition`, `DeckContainsCondition`, `EntityEqualsCondition`, `GraveyardCountCondition`, `HasAttackedCondition`, `HasHeroPowerCondition`, `HasWeaponCondition`, `HeroClassCondition`, `HighlanderDeckCondition`, `HighlanderHandCondition`, `HoldsCardCondition`, `InvokeCondition`, `IsAdjacentCondition`, `IsDamagedCondition`, `IsDeadCondition`, `ManaCostCondition`, `ManaMaxedCondition`, `MinionCountCondition`, `MinionOnBoardCondition`, `OrCondition`, `OverloadedCondition`, `PlayedLastTurnCondition`, `RaceCondition`, `RandomCondition`, `SurgeCondition`

- `NoDuplicatesCondition` does NOT exist -> use `HighlanderDeckCondition`
- `NegateCondition` does NOT exist -> conditions support an `invert` key
- `EntityCountCondition` does NOT exist -> use `MinionCountCondition` or `CardCountCondition`
- `HasQuestCondition` does NOT exist
- `ManaCrystalCondition` does NOT exist -> use `ManaMaxedCondition` or `AttributeCondition`
- `CardPlayedLastTurnCondition` -> use `PlayedLastTurnCondition`
- `CardPlayedThisTurnCondition` -> use `SurgeCondition` or `ComboCondition`

## Valid Filter Classes

`AmalgamRaceFilter`, `AndFilter`, `AttributeFilter`, `CardFilter`, `CollectibleFilter`, `DamagedFilter`, `DeckContainsFilter`, `EntityEqualsFilter`, `EntityTypeFilter`, `EvenCostFilter`, `HandContainsFilter`, `HasAttackedFilter`, `HasEffectsFilter`, `HasEnchantmentFilter`, `HasTextFilter`, `HighestAttributeFilter`, `IsDestroyedFilter`, `ManaCostFilter`, `NullFilter`, `OddCostFilter`, `OrFilter`, `OwnedByPlayerFilter`, `RaceFilter`, `SameHeroClassFilter`, `SpecificCardFilter`, `SpellFilter`

- `NotFilter` does NOT exist -> use `invert: true` on `CardFilter` or use `AndFilter`
- `IsDamagedFilter` -> use `DamagedFilter`
- `DeadFilter` -> use `IsDestroyedFilter`
- `OwnedByClassFilter` -> use `SameHeroClassFilter`

## Valid Trigger Classes

See full list in `spells/trigger/`. Key ones:
- `CardPlayedTrigger`, `SpellCastedTrigger`, `MinionSummonedTrigger`
- `DamageCausedTrigger`, `DamageReceivedTrigger`, `PhysicalAttackTrigger`
- `TurnStartTrigger`, `TurnEndTrigger`, `CardDrawnTrigger`
- `HealingTrigger`, `MinionDeathTrigger`, `InspireTrigger`

- `DamageEventTrigger` does NOT exist -> use `DamageCausedTrigger`
- `DamageTrigger` does NOT exist -> use `DamageCausedTrigger`
- `KillTrigger` does NOT exist -> use `MinionDeathTrigger`
- `Secret` is NOT a trigger class -> use the appropriate trigger (e.g., `SpellCastedTrigger`)
