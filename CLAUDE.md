# Spellsource

Open-source card game engine. Cards are JSON files processed by a Java game engine.

## Key Paths

- Collectible cards: `spellsource-cards-git/src/main/resources/cards/collectible/{heroClass}/custom/`
- Tokens: `spellsource-cards-git/src/main/resources/cards/uncollectible/{heroClass}/custom/`
- Spell classes: `spellsource-game/src/main/java/net/demilich/metastone/game/spells/`
- Card model: `spellsource-game/src/main/java/net/demilich/metastone/game/cards/desc/CardDesc.java`
- Tests: `spellsource-cards-git/src/test/java/com/hiddenswitch/spellsource/tests/cards/`

## Engineering Rules

- Never rename or delete card files. To remove a card set `"collectible": false`.
- Always include `"set": "CUSTOM"` and `"fileFormatVersion": 1`.
- Card ID = filename without `.json`. Do not change after release.
- Deathrattle is a direct SpellDesc. Wrong: `"deathrattle": {"spell": {...}}`. Right: `"deathrattle": {"class": "...", ...}`.
- Battlecry requires both the `battlecry` field and `"BATTLECRY": true` in `attributes`.
- File names: `{type}_{name}.json`, all lowercase with underscores.

## Hero Classes

Each class is a directory under `cards/collectible/`. Discover them with:
```
ls spellsource-cards-git/src/main/resources/cards/collectible/
```
Each directory has a `class_{color}.json` file defining the class. Use `"heroClass": "ANY"` for neutral cards.

## Keyword Mapping

| Description | JSON |
|------------|------|
| Opener | `battlecry` + `"BATTLECRY": true` |
| Aftermath | `deathrattle` + `"DEATHRATTLES": true` |
| Guard | `"TAUNT": true` |
| Blitz | `"CHARGE": true` |
| Dash | `"RUSH": true` |
| Dodge/Deflect | `"DIVINE_SHIELD": true` / `"DEFLECT": true` |
| Hidden | `"STEALTH": true` |
| Toxic | `"POISONOUS": true` |
| Drain | `"LIFESTEAL": true` |
| Extra Strike | `"WINDFURY": true` |
| Elusive | `"UNTARGETABLE_BY_SPELLS": true` |
| Decay | `"DECAY": true` |
| Surge | `"COMBO": true` |

## Commit Messages

No colons. No commas. Lowercase. Succinct.

## Commands

Run all card tests:
```
./gradlew game:test
```

Run a single test:
```
./gradlew game:test --tests "*.ExampleCardTests.testExampler"
```

## Documentation

- [docs/architecture.md](docs/architecture.md) — Project structure and keyword terminology
- [docs/card-authoring.md](docs/card-authoring.md) — Card JSON schema and examples
- [docs/spell-reference.md](docs/spell-reference.md) — Spell classes, triggers, value providers and filters
- [docs/testing-guide.md](docs/testing-guide.md) — Writing and running card tests
- [CONTRIBUTE.md](CONTRIBUTE.md) — Contribution guidelines and merging process
- [spellsource-web/src/pages-markdown/keywords.md](spellsource-web/src/pages-markdown/keywords.md) — Full keyword definitions
- [spellsource-cards-git/README.md](spellsource-cards-git/README.md) — Clean room and licensing rules
