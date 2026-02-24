# Testing Guide

Card tests live in `spellsource-cards-git/src/test/java/com/hiddenswitch/spellsource/tests/cards/`. They extend `TestBase` from `spellsource-testutils`.

## Writing a Test

Create a class that extends `TestBase` and annotate it with `@Execution(ExecutionMode.CONCURRENT)`:

```java
package com.hiddenswitch.spellsource.tests.cards;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class MyCardTests extends TestBase {
    @Test
    public void testBigBro() {
        runGym((context, player, opponent) -> {
            playCard(context, player, "minion_big_bro");
            // Big Bro's opener should add a card to hand
            assertEquals(1, player.getHand().size());
        });
    }
}
```

The `runGym` method sets up a game with two players using default heroes. `player` goes first and starts with 30 HP, full mana and an empty hand/board. `opponent` is the other player.

## TestBase Helper Methods

**Playing cards:**
- `playCard(context, player, "card_id")` — plays a card from nowhere (does not need to be in hand)
- `playCard(context, player, cardInstance)` — plays a Card object
- `playMinionCard(context, player, "card_id")` — plays a minion and returns the `Minion` entity
- `playChooseOneCard(context, player, "card_id", "choice_card_id")` — plays a choose-one card

**Hand and deck manipulation:**
- `receiveCard(context, player, "card_id")` — adds a card to the player's hand and returns it
- `shuffleToDeck(context, player, "card_id")` — shuffles a card into the deck
- `putOnTopOfDeck(context, player, "card_id")` — puts a card on top of the deck
- `clearHand(context, player)` — removes all cards from hand
- `clearZone(context, zone)` — clears any zone (e.g. `player.getDeck()`)

**Combat:**
- `attack(context, player, attacker, target)` — performs a physical attack
- `useHeroPower(context, player)` — uses the hero power with no target
- `useHeroPower(context, player, target)` — uses hero power on a target

**Finding entities:**
- `find(context, "card_id")` — finds a minion in play by its card ID (from either player). Returns null if not found.
- `findCard(context, "card_id")` — finds a card in any player's hand by ID

**Game state control:**
- `destroy(context, entity)` — destroys an entity
- `context.endTurn()` — ends the current player's turn
- `overrideBattlecry(context, player, action)` — forces a specific battlecry target
- `overrideDiscover(context, player, action)` — forces a specific discover choice
- `overrideRandomCard(context, "card_id")` — forces random card generation to return a specific card
- `receive(context, player, attack, hp, manaCost)` — creates an ad-hoc minion card with given stats

## Common Assertions

```java
// Board state
assertEquals(2, player.getMinions().size());
assertEquals(0, opponent.getMinions().size());

// Minion stats
Minion m = player.getMinions().get(0);
assertEquals(3, m.getAttack());
assertEquals(5, m.getHp());
assertEquals(5, m.getMaxHp());

// Attributes
assertTrue(m.hasAttribute(Attribute.TAUNT));
assertFalse(m.hasAttribute(Attribute.STEALTH));

// Hero HP and armor
assertEquals(25, opponent.getHero().getHp());
assertEquals(5, player.getHero().getArmor());

// Hand and deck
assertEquals(3, player.getHand().size());
assertEquals(10, player.getDeck().size());

// Card identity
assertEquals("token_lil_bro", player.getHand().get(0).getCardId());
```

## Running Tests

All card tests:
```
./gradlew game:test
```

A single test method:
```
./gradlew game:test --tests "*.MyCardTests.testBigBro"
```

A single test class:
```
./gradlew game:test --tests "*.MyCardTests"
```

## MassTest

`MassTest` plays 10,000 random games with random decks. Every collectible card gets played at least once statistically. This catches crashes, infinite loops and null pointer exceptions. Run it with:
```
./gradlew game:test --tests "*.MassTest"
```

## TraceTests

`TraceTests` replays recorded failure traces from `src/test/resources/traces/`. When MassTest or production games crash, a trace JSON file captures the exact game state and actions that led to the crash. TraceTests replays these to verify fixes.
