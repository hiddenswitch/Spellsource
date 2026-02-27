# Card Audit Plan

Audit every collectible card added in the `deba1d5` ("internalcontent updates") commit relative to `faf5080`. For each card with non-trivial mechanics: write a test that validates its behavior, run it, and fix the card JSON if it fails.

## Repo location

`~/Documents/spellsource`
Submodule (cards + tests): `spellsource-cards-private/src/`
Card JSON: `spellsource-cards-private/src/main/resources/internalcontent/{set}/{heroClass}/{cardId}.json`
Tests: `spellsource-cards-private/src/test/java/com/hiddenswitch/spellsource/tests/hearthstone/`

## Build commands

```bash
# Run a single test
cd ~/Documents/spellsource
./gradlew :spellsource-cards-private:test --tests "*.FooTests.testBar"

# Run all tests in a class
./gradlew :spellsource-cards-private:test --tests "*.FooTests"

# Run everything
./gradlew :spellsource-cards-private:test
```

## What to test

**Test these**: any card with triggers, auras, deathrattles, battlecries with conditions, cost modifiers, transforms, "for each X" counting effects, discover, multi-part effects, or any text beyond a simple stat line.

**Skip these**: cards with no effect text (vanilla), simple unconditional stat buffs, cards that are pure damage/heal with no side effects. `CardValidationTests` already catches JSON schema errors.

**Quality bar**: the test must assert the *specific behavior described in the card text*, not just that the card can be played without crashing.

## Test class template

```java
package com.hiddenswitch.spellsource.tests.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Minion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FooTests extends TestBase {

    @Test
    public void testCardName() {
        // Card text: "..."
        runGym((context, player, opponent) -> {
            // setup
            // action
            // assert
        });
    }
}
```

## Key TestBase helpers

```java
playMinionCard(context, player, "card_id")             // play a minion, returns Minion
playCard(context, player, "card_id")                   // play any card
playCard(context, player, "card_id", target)           // play with target
receiveCard(context, player, "card_id")                // add to hand, returns Card
shuffleToDeck(context, player, "card_id")              // add to deck
costOf(context, player, card)                          // get mana cost of card in hand
destroy(context, target)                               // kill a minion
castDamageSpell(context, player, damage, target)       // deal exact damage
context.endTurn()                                      // end current player's turn
clearHand(context, player)                             // empty hand
attack(context, player, attacker, target)              // attack with a minion
useHeroPower(context, player)                          // use hero power
overrideDiscover(context, player, "card_id")           // fix discover choice
overrideBattlecry(context, player, fn)                 // fix battlecry target
player.getMinions()                                    // get friendly minions list
opponent.getMinions()                                  // get enemy minions list
player.getHand()                                       // hand
player.getDeck()                                       // deck
player.getHero().getHp()                               // hero HP
minion.getAttack() / minion.getHp()                    // stats
minion.hasAttribute(Attribute.TAUNT)                   // attribute check
minion.isDestroyed()                                   // destruction check
```

## Common card JSON patterns to watch for

- `BuffAura` only works on `Actor` entities (minions/heroes), NOT cards in hand — use `cardCostModifier` for hand cost effects
- `RESERVED_INTEGER_1` on the player is shared — counters should go on `SELF` (the entity)
- `AfterSpellCastedTrigger` not `CardPlayedTrigger` for spell counting
- `SetAttributeSpell` (not `RemoveAttributeSpell`) to reset a counter to 0
- Deathrattle must be a direct `SpellDesc`, NOT `{"spell": {...}}` wrapper
- Battlecry requires both `battlecry` field and `"BATTLECRY": true` in `attributes`
- `TurnStartTrigger` `sourcePlayer` vs `targetPlayer`: use `sourcePlayer: "SELF"` to fire on owner's turn start

---

## Priority order

Sets are ordered by: (1) zero existing tests first, (2) then lowest tests-per-card ratio.

---

### Phase 1 — Create new test files (sets with 0 tests)

| Set | Cards | Test class to create |
|-----|-------|----------------------|
| `core` | 201 | `CoreTests.java` |
| `time_travel` | 184 | `TimeTravelTests.java` |
| `murder_at_castle_nathria` | 161 | `MurderAtCastleNathriaTests.java` |
| `wonders` | 156 | `WondersTests.java` |
| `path_of_arthas` | 37 | `PathOfArthasTests.java` |
| `year_of_the_dragon` | 34 | `YearOfTheDragonTests.java` |
| `demon_hunter_initiate` | 20 | `DemonHunterInitiateTests.java` |

For each set in this phase:
1. List all card files: `ls spellsource-cards-private/src/main/resources/internalcontent/{set}/`
2. For each subdirectory (hero class), read each card JSON
3. Skip vanilla cards (no effect text)
4. For every non-trivial card: add a `@Test` method to the new test class
5. Run the full test class after every ~10 tests added: `./gradlew :spellsource-cards-private:test --tests "*.{TestClass}"`
6. Fix any failing cards before continuing

---

### Phase 2 — Expand sparse test files

| Set | Cards | Existing tests | Test class |
|-----|-------|----------------|------------|
| `ashes_of_outland` | 127 | 8 | `AshesOfOutlandTests.java` |
| `madness_at_the_darkmoon_faire` | 120 | 5 | `MadnessAtTheDarkmoonFaireTests.java` |
| `scholomance_academy` | 110 | 6 | `ScholomanceAcademyTests.java` |
| `saviors_of_uldum` | 135 | 9 | `SaviorsOfUldumTests.java` |
| `descent_of_dragons` | 121 | 8 | `DescentOfDragonsTests.java` |
| `space` | 192 | 30 | `SpaceTests.java` |
| `emerald_dream` | 170 | 30 | `EmeraldDreamTests.java` |
| `the_lost_city` | 183 | 30 | `TheLostCityTests.java` |

Same process: read each card, write a test for non-trivial ones, run incrementally, fix failures.

---

### Phase 3 — Audit sets with existing coverage

These sets already have tests but may have untested cards. Go through all collectible cards not yet covered by an existing test.

| Set | Cards | Existing tests | Test class |
|-----|-------|----------------|------------|
| `perils_in_paradise` | 174 | 116 | `PerilsInParadiseTests.java` |
| `voyage_to_the_sunken_city` | 170 | 102 | `VoyageToTheSunkenCityTests.java` |
| `march_of_the_lich_king` | 182 | 123 | `MarchOfTheLichKingTests.java` |
| `titans` | 176 | 118 | `TitansExpansionTests.java` |
| `showdown_in_the_badlands` | 179 | 136 | `ShowdownInTheBadlandsTests.java` |
| `festival_of_legends` | 174 | 139 | `FestivalOfLegendsTests.java` |
| `united_in_stormwind` | 164 | 138 | `UnitedInStormwindTests.java` |
| `forged_in_the_barrens` | 160 | 129 | `ForgedInTheBarrensTests.java` |
| `fractured_in_alterac_valley` | 168 | 148 | `FracturedInAlteracValleyTests.java` |
| `whizbangs_workshop` | 183 | 150 | `WhizbangsWorkshopTests.java` |

To find untested cards: cross-reference card file names against `grep "@Test\|cardId\|card_id\|\"" *Tests.java` to see which card IDs appear in tests.

---

### Phase 4 — Legacy and Core (large sets, lower risk)

| Set | Cards | Notes |
|-----|-------|-------|
| `legacy` | 240 | Classic-era cards, many already well-known |
| `core` | 201 | Basic set; many vanillas but some complex cards |

---

## Execution loop (per card)

```
1. cat spellsource-cards-private/src/main/resources/internalcontent/{set}/{class}/{card_id}.json
2. Read description field
3. If trivial → skip
4. Write @Test method asserting the described behavior
5. ./gradlew :spellsource-cards-private:test --tests "*.{TestClass}.test{CardName}"
6. If FAIL → read error, diagnose JSON, fix, re-run
7. If fix needs engine change → note it with TODO comment in the test, continue
8. Commit after each set: cd spellsource-cards-private/src && git add -A && git commit -m "tests and fixes for {set}"
```

## Committing

Work in the submodule:
```bash
cd ~/Documents/spellsource/spellsource-cards-private/src
git add main/resources/internalcontent/... test/java/...
git commit -m "{set} tests and fixes"
git push origin master
```

Then update the submodule pointer in the main repo:
```bash
cd ~/Documents/spellsource
git add spellsource-cards-private
git commit -m "update internalcontent for {set}"
git push origin develop
```
