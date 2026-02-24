# Architecture

Spellsource is an open-source card game engine similar to Hearthstone. Cards are defined as JSON files, processed by a Java game engine and tested with JUnit.

## Modules

- **spellsource-game** — The game engine. Contains all spell classes, triggers, card parsing and game logic. The entry point for a game is `GameContext`; AI lives in `GameStateValueBehaviour`.
- **spellsource-cards-git** — Public card JSON files and card tests. Cards live under `src/main/resources/cards/`. Tests live under `src/test/java/`.
- **spellsource-cards-private** (submodule at `spellsource-cards-private/src`) — Internal/private card content.
- **spellsource-testutils** — `TestBase` class with helper methods for writing card tests.
- **spellsource-server** — Multiplayer server, matchmaking, collections.
- **spellsource-web** — Website and keyword definitions.

## Card Loading Pipeline

JSON files → `CardParser` → `CardDesc` (Java object) → `Card` entity → `ClasspathCardCatalogue`

The `CardDesc` class at `spellsource-game/src/main/java/net/demilich/metastone/game/cards/desc/CardDesc.java` defines every field a card JSON can have. Each JSON key maps directly to a field in `CardDesc`.

## Key Paths

| What | Path |
|------|------|
| Collectible cards | `spellsource-cards-git/src/main/resources/cards/collectible/{heroClass}/custom/` |
| Uncollectible/tokens | `spellsource-cards-git/src/main/resources/cards/uncollectible/{heroClass}/custom/` |
| Internal cards | `spellsource-cards-private/src/main/resources/cards/` |
| Card model | `spellsource-game/src/main/java/net/demilich/metastone/game/cards/desc/CardDesc.java` |
| Spell classes | `spellsource-game/src/main/java/net/demilich/metastone/game/spells/` |
| Custom spell classes | `spellsource-game/src/main/java/net/demilich/metastone/game/spells/custom/` |
| Triggers | `spellsource-game/src/main/java/net/demilich/metastone/game/spells/trigger/` |
| Test base | `spellsource-testutils/src/main/java/net/demilich/metastone/tests/util/TestBase.java` |
| Card tests | `spellsource-cards-git/src/test/java/com/hiddenswitch/spellsource/tests/cards/` |

## Hero Classes

Hero classes are user-authored. Each class is a directory under `cards/collectible/` with a `class_{color}.json` file of `"type": "CLASS"`. The `heroClass` field is a free-form uppercase string like `"AMBER"` or `"NAVY"`. Use `"ANY"` for neutral cards.

To discover existing classes:
```
ls spellsource-cards-git/src/main/resources/cards/collectible/
```

Current directories: amber, azure, bluegrey, camo, candy, copper, coral, crimson, darkblue, darkgreen, darkmagenta, lightbrown, magenta, navy, neongreen, neutral, offwhite, olive, peach, rust, toast, twilight.

A CLASS card looks like this:
```json
{
  "name": "Hive Queen",
  "type": "CLASS",
  "heroClass": "AMBER",
  "rarity": "FREE",
  "collectible": true,
  "set": "CUSTOM",
  "fileFormatVersion": 1,
  "hero": "hero_zara",
  "art": {
    "primary": { "a": 1.0, "b": 0.0, "g": 0.8, "r": 1.0 },
    "secondary": { "a": 1.0, "b": 0.0, "g": 0.694, "r": 0.867 },
    "highlight": { "a": 1.0, "b": 0.333, "g": 0.867, "r": 1.0 },
    "shadow": { "a": 1.0, "b": 0.0, "g": 0.482, "r": 0.604 }
  },
  "tooltips": [
    { "keywords": ["larva"], "text": "<b>Larva</b>: A 1/1 unit." }
  ]
}
```

The `hero` field references the default hero card ID. The `art` colors control the class UI in the client.

## Keyword Terminology

Spellsource uses its own keywords in card `description` text. The JSON fields and attribute names use different (often Hearthstone-derived) identifiers.

| Description text | JSON field or attribute |
|-----------------|----------------------|
| Opener | `battlecry` field + `"BATTLECRY": true` attribute |
| Aftermath | `deathrattle` field + `"DEATHRATTLES": true` attribute |
| Guard | `"TAUNT": true` attribute |
| Blitz | `"CHARGE": true` attribute |
| Dash | `"RUSH": true` attribute |
| Deflect | `"DEFLECT": true` attribute |
| Dodge | `"DIVINE_SHIELD": true` attribute |
| Hidden | `"STEALTH": true` attribute |
| Toxic | `"POISONOUS": true` attribute |
| Drain | `"LIFESTEAL": true` attribute |
| Extra Strike | `"WINDFURY": true` attribute |
| Elusive | `"UNTARGETABLE_BY_SPELLS": true` attribute |
| Decay | `"DECAY": true` attribute |
| Surge | `"COMBO": true` attribute |
| Skill | `HERO_POWER` card type |
| Champion | `HERO` card type |
| Unit | `MINION` card type |
| Supremacy | Implement with a trigger that checks for kills |
| Quick Draw | Implement with `CardDrawnTrigger` |
| Sacrifice | Implement with `MinionDeathTrigger` targeting friendly minions |

Source: `spellsource-web/src/pages-markdown/keywords.md`
