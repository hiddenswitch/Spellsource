# Card Authoring

## CardDesc Field Reference

Every card JSON file deserializes into a `CardDesc`. These are all the fields:

| Field | Type | Used by | Purpose |
|-------|------|---------|---------|
| `name` | string | all | Display name |
| `baseManaCost` | int | all | Mana cost. Set to 0 for tokens. |
| `type` | string | all | `MINION`, `SPELL`, `WEAPON`, `HERO`, `HERO_POWER`, `CLASS`, `CHOOSE_ONE`, `ENCHANTMENT` |
| `heroClass` | string | all | Uppercase color code (`"AMBER"`, `"NAVY"`) or `"ANY"` for neutral |
| `heroClasses` | string[] | multi-class | For tri-class cards |
| `rarity` | string | all | `FREE`, `COMMON`, `RARE`, `EPIC`, `LEGENDARY`, `ALLIANCE` |
| `description` | string | all | Card text shown to the player. Use Spellsource keywords (Opener, Guard, etc.) |
| `race` | string | minions | Tribe: `"BEAST"`, `"DRAGON"`, `"MURLOC"`, `"MECH"`, etc. |
| `baseAttack` | int | minions | Base attack value |
| `baseHp` | int | minions | Base health value |
| `damage` | int | weapons | Weapon attack value |
| `durability` | int | weapons | Weapon durability |
| `heroPower` | string | heroes | Card ID of the hero power |
| `hero` | string | CLASS cards | Card ID of the default hero |
| `collectible` | bool | all | Whether it appears in discovers/collection. Default `true`. |
| `set` | string | all | Always `"CUSTOM"` for community cards |
| `fileFormatVersion` | int | all | Always `1` |
| `attributes` | object | all | Map of attribute names to `true` or a number |
| `battlecry` | OpenerDesc | minions/heroes/weapons | Played-from-hand effect |
| `deathrattle` | SpellDesc | minions/heroes/weapons | On-death effect (direct SpellDesc, NOT wrapped) |
| `spell` | SpellDesc | spells/hero powers | The spell effect |
| `targetSelection` | string | spells/hero powers | Player targeting prompt |
| `condition` | ConditionDesc | spells | Condition to be playable |
| `trigger` | EnchantmentDesc | minions/weapons | Single in-play trigger |
| `triggers` | EnchantmentDesc[] | minions/weapons | Multiple in-play triggers |
| `aura` | AuraDesc | minions/weapons | Single in-play aura |
| `auras` | AuraDesc[] | minions/weapons | Multiple in-play auras |
| `passiveTrigger` | EnchantmentDesc | cards | Active while card is in hand |
| `passiveTriggers` | EnchantmentDesc[] | cards | Multiple hand triggers |
| `deckTrigger` | EnchantmentDesc | cards | Active while card is in deck |
| `deckTriggers` | EnchantmentDesc[] | cards | Multiple deck triggers |
| `gameTriggers` | EnchantmentDesc[] | cards | Active from game start |
| `manaCostModifier` | ValueProviderDesc | cards | Dynamic cost reduction while in hand |
| `cardCostModifier` | CardCostModifierDesc | minions | Cost modifier active while in play |
| `secret` | EventTriggerDesc | spells | Secret trigger |
| `quest` | EventTriggerDesc | spells | Quest trigger |
| `countUntilCast` | int | quests | Times quest trigger must fire |
| `chooseOneBattlecries` | OpenerDesc[] | minions | Choose-one battlecry options |
| `chooseBothBattlecry` | OpenerDesc | minions | Fandral-style combined battlecry |
| `chooseOneCardIds` | string[] | choose-one spells | Card IDs for spell choices |
| `onEquip` | SpellDesc | weapons | Cast when weapon enters play |
| `onUnequip` | SpellDesc | weapons | Cast when weapon leaves play |
| `author` | string | all | Card author name |
| `flavor` | string | all | Flavor text |
| `wiki` | string | all | Implementation notes |
| `art` | object | all | Art/sprite configuration |
| `tooltips` | object[] | all | Keyword tooltips |

## Complex Field Structures

**Battlecry (OpenerDesc)** — wraps a spell with target selection:
```json
"battlecry": {
  "targetSelection": "ENEMY_MINIONS",
  "spell": {
    "class": "DamageSpell",
    "value": 3
  }
}
```
The `targetSelection` on a battlecry determines what the player picks. The spell receives the chosen target. You must also set `"BATTLECRY": true` in `attributes`.

**Deathrattle (SpellDesc)** — a direct spell object. Do NOT wrap it in `{"spell": ...}`:
```json
"deathrattle": {
  "class": "SummonSpell",
  "card": "token_skeletal_enforcer"
}
```

**Trigger (EnchantmentDesc)** — listens for game events:
```json
"trigger": {
  "eventTrigger": {
    "class": "MinionDeathTrigger",
    "targetPlayer": "BOTH"
  },
  "spell": {
    "class": "SummonSpell",
    "card": "token_spiderling"
  },
  "maxFiresPerSequence": 7
}
```
Key EnchantmentDesc fields: `eventTrigger`, `spell`, `maxFires`, `maxFiresPerSequence`, `oneTurn`, `countUntilCast`, `expirationTriggers`.

**Aura (AuraDesc)** — ongoing effect while in play:
```json
"aura": {
  "class": "BuffAura",
  "target": "FRIENDLY_MINIONS",
  "attackBonus": 1,
  "hpBonus": 1
}
```
Aura classes include `BuffAura`, `AttributeAura`, `CardCostModifierAura`, `SpellOverrideAura`.

**Spell card** — uses `targetSelection` on the card itself and a top-level `spell` field:
```json
{
  "name": "Beetle Bash",
  "baseManaCost": 4,
  "type": "SPELL",
  "heroClass": "AMBER",
  "rarity": "RARE",
  "description": "Gain 4 Armor. Summon a 1/4 Blue Beetle with Guard.",
  "targetSelection": "NONE",
  "spell": {
    "class": "MetaSpell",
    "spells": [
      {
        "class": "BuffSpell",
        "target": "FRIENDLY_HERO",
        "armorBonus": 4
      },
      {
        "class": "SummonSpell",
        "card": "minion_blue_beetle"
      }
    ]
  },
  "collectible": true,
  "set": "CUSTOM",
  "fileFormatVersion": 1
}
```

## File Naming and Placement

File names: `{type}_{name}.json`, all lowercase, underscores for spaces.

Type prefixes: `minion_`, `spell_`, `weapon_`, `hero_`, `hero_power_`, `token_`, `enchantment_`, `permanent_`.

Card ID = filename without `.json`. Never change a card's filename after release.

Collectible cards go in `cards/collectible/{heroClass}/custom/`. Tokens and uncollectible cards go in `cards/uncollectible/{heroClass}/custom/`.

## Engineering Rules

- Never rename existing card files.
- Never delete cards. Set `"collectible": false` instead.
- Always include `"set": "CUSTOM"` and `"fileFormatVersion": 1`.
- Deathrattle is a direct SpellDesc. Wrong: `"deathrattle": {"spell": {...}}`. Right: `"deathrattle": {"class": "...", ...}`.
- Battlecry needs both the `battlecry` field and `"BATTLECRY": true` in attributes.

## TargetSelection Values

`NONE` — no target prompt. `ANY` — any character. `MINIONS` — any minion. `ENEMY_CHARACTERS` — enemy hero or minions. `FRIENDLY_CHARACTERS` — friendly hero or minions. `ENEMY_MINIONS` — enemy minions only. `FRIENDLY_MINIONS` — friendly minions only. `HEROES` — either hero. `ENEMY_HERO` — enemy hero only. `FRIENDLY_HERO` — friendly hero only.

## Common EntityReference Values

Use these as string values for `"target"` in spell JSON.

| Reference | Resolves to |
|-----------|------------|
| `FRIENDLY_HERO` | Your hero |
| `ENEMY_HERO` | Opponent's hero |
| `FRIENDLY_MINIONS` | All your minions |
| `ENEMY_MINIONS` | All enemy minions |
| `ALL_MINIONS` | All minions on board |
| `ALL_CHARACTERS` | Both heroes and all minions |
| `OTHER_FRIENDLY_MINIONS` | Your minions except the source |
| `ALL_OTHER_MINIONS` | All minions except the source |
| `ALL_OTHER_CHARACTERS` | All characters except the source |
| `ADJACENT_MINIONS` | Minions adjacent to the source |
| `SELF` | The source entity itself |
| `OUTPUT` | Entity created by the parent spell (summoned minion, received card) |
| `EVENT_TARGET` | Target of the triggering event |
| `EVENT_SOURCE` | Source of the triggering event |
| `TRIGGER_HOST` | Entity hosting the trigger |
| `FRIENDLY_HAND` | All cards in your hand |
| `ENEMY_HAND` | All cards in opponent's hand |
| `FRIENDLY_DECK` | All cards in your deck |
| `ENEMY_DECK` | All cards in opponent's deck |
| `FRIENDLY_WEAPON` | Your weapon |
| `ENEMY_WEAPON` | Opponent's weapon |
| `FRIENDLY_HERO_POWER` | Your hero power card |
| `FRIENDLY_TOP_CARD` | Top card of your deck |
| `TARGET` | The player's chosen target |
| `SPELL_TARGET` | Current iteration target when spell iterates over a group |
| `NONE` | No target (null) |

## Common Attributes

| Attribute | Type | Purpose |
|-----------|------|---------|
| `BATTLECRY` | true | Marks card as having a battlecry |
| `DEATHRATTLES` | true | Marks card as having a deathrattle |
| `TAUNT` | true | Guard |
| `CHARGE` | true | Blitz |
| `RUSH` | true | Dash |
| `DIVINE_SHIELD` | true | Dodge |
| `STEALTH` | true | Hidden |
| `POISONOUS` | true | Toxic |
| `LIFESTEAL` | true | Drain |
| `WINDFURY` | true | Extra Strike |
| `UNTARGETABLE_BY_SPELLS` | true | Elusive |
| `DECAY` | true | Loses 1 HP/durability each turn |
| `COMBO` | true | Surge |
| `IMMUNE` | true | Cannot be damaged |
| `CANNOT_ATTACK` | true | Cannot attack |
| `SPELL_DAMAGE` | int | Spellpower bonus |
| `OVERLOAD` | int | Locks mana crystals next turn |
| `ARMOR` | int | Armor on hero cards |
| `MAX_HP` | int | Override max HP (used on hero cards, usually 30) |
| `HP_BONUS` | int | Extra HP from buffs |
| `ATTACK_BONUS` | int | Extra attack from buffs |

## Complete Card Examples

**Minion with battlecry:**
```json
{
  "name": "Big Bro",
  "baseManaCost": 3,
  "type": "MINION",
  "heroClass": "AMBER",
  "baseAttack": 3,
  "baseHp": 3,
  "rarity": "RARE",
  "race": "DRAGON",
  "description": "Opener: Receive a 1/1 Lil Bro.",
  "battlecry": {
    "spell": {
      "class": "ReceiveCardSpell",
      "cards": ["token_lil_bro"]
    }
  },
  "attributes": {
    "BATTLECRY": true
  },
  "collectible": true,
  "set": "CUSTOM",
  "fileFormatVersion": 1
}
```

**Minion with trigger:**
```json
{
  "name": "Broodmother Narvina",
  "baseManaCost": 6,
  "type": "MINION",
  "heroClass": "AMBER",
  "baseAttack": 5,
  "baseHp": 5,
  "rarity": "LEGENDARY",
  "race": "BEAST",
  "description": "Whenever a unit dies, summon two 1/1 Spiderlings. (Up to 14)",
  "trigger": {
    "eventTrigger": {
      "class": "MinionDeathTrigger",
      "targetPlayer": "BOTH"
    },
    "spell": {
      "class": "SummonSpell",
      "card": "token_spiderling",
      "value": 2
    },
    "maxFiresPerSequence": 7
  },
  "collectible": false,
  "set": "CUSTOM",
  "fileFormatVersion": 1
}
```

**Weapon with trigger:**
```json
{
  "name": "Spawning Scepter",
  "baseManaCost": 7,
  "type": "WEAPON",
  "heroClass": "AMBER",
  "damage": 2,
  "durability": 2,
  "rarity": "EPIC",
  "description": "After your champion attacks, summon a 1/1 Larva for each card in your hand.",
  "trigger": {
    "eventTrigger": {
      "class": "TargetAcquisitionTrigger",
      "actionType": "PHYSICAL_ATTACK",
      "sourceEntityType": "HERO",
      "sourcePlayer": "SELF",
      "targetPlayer": "BOTH"
    },
    "spell": {
      "class": "SummonSpell",
      "value": {
        "class": "PlayerAttributeValueProvider",
        "playerAttribute": "HAND_COUNT",
        "targetPlayer": "SELF"
      },
      "card": "token_spiderling"
    }
  },
  "collectible": true,
  "set": "CUSTOM",
  "fileFormatVersion": 1
}
```

**Hero card:**
```json
{
  "name": "Arido",
  "heroPower": "hero_power_arido",
  "baseManaCost": 10,
  "type": "HERO",
  "heroClass": "AMBER",
  "rarity": "LEGENDARY",
  "description": "Opener: Fill your board with Serv-Ants with Guard.",
  "battlecry": {
    "targetSelection": "NONE",
    "spell": {
      "class": "SummonSpell",
      "value": 7,
      "card": "token_serv-ant_guard"
    }
  },
  "attributes": {
    "ARMOR": 5,
    "BATTLECRY": true,
    "MAX_HP": 30
  },
  "collectible": false,
  "set": "CUSTOM",
  "fileFormatVersion": 1
}
```

**Hero power:**
```json
{
  "name": "Master of Combat",
  "baseManaCost": 2,
  "type": "HERO_POWER",
  "heroClass": "AMBER",
  "rarity": "FREE",
  "description": "Give an ally +1 Attack and Toxic.",
  "targetSelection": "FRIENDLY_CHARACTERS",
  "spell": {
    "class": "HeroPowerSpell",
    "spells": [
      {
        "class": "BuffSpell",
        "attackBonus": 1
      },
      {
        "class": "AddAttributeSpell",
        "attribute": "POISONOUS"
      }
    ]
  },
  "collectible": false,
  "set": "CUSTOM",
  "fileFormatVersion": 1
}
```
