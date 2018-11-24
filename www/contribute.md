---
layout: page
title: Contribute
permalink: /contribute/
---
### Guidelines

Visit our [Discord](https://discord.gg/HmbESh2) to chat about these guidelines and what cards you'd like to add.

Spellsource welcomes custom cards from the community. Almost all contributions are accepted, but they tend to be accepted at different speeds depending on a few characteristics of the contribution. The better the **engineering** and the better the **gameplay quality**, the more likely your cards will be available in the public servers sooner rather than later.

With regards to **engineering**, maintainers prioritize content based on its engineering quality in this order:

 1. Fully coded in JSON with tests and placed in a Pull Request.
 2. Fully coded in JSON and placed in a Pull Request.
 3. Mostly coded in JSON and placed in a Pull Request.
 4. Code of any kind that's attached as a zip file in an Issue.
 5. Code of any kind sent to the maintainers in the Discord.
 6. Hearthcards gallery of content linked in an Issue.
 7. Spreadsheet of content linked in an Issue.
 8. Card text in an ordinary text format in the Discord `#suggestions` channel.
 9. Direct messaging the maintainers in Discord non-coded (i.e. image or text) content of any kind.
 10. Card images in the Discord `#suggestions` channel.
 
Observe that the way you may be used to sharing a card--as an image with carefully selected art--is actually the least helpful way for the Spellsource maintainers to integrate.

With regards to **gameplay quality**, maintainers prioritize content based on its gameplay quality in this order:

 1. Original hero classes (60+ cards) with some evidence of being judged positively by the community (e.g. Hearthpwn competitions, weekly /r/customhearthstone competitions).
 2. Original cards with some evidence of being judged positively by the community.
 3. Original cards using the Legacy mechanic: cards that remember things that have happened to them in prior games, like how much damage they dealt, what minions they destroyed, etc.
 4. Original cards with great new mechanics.
 5. Original cards for custom classes.
 6. Original cards for the Neutral class.
 7. Original cards for Hearthstone classes.
 8. Cards that adapt obscure copyrighted content. Use your best judgement for obscurity. For example, any English language television or movie content with a sequel is not considered obscure, becuase it was worth making a sequel.
 9. Hero classes that comprehensively adapt mainstream copyrighted content. For example, a complete House of Stark hero class adaptation for Game of Thrones.
 10. Cards that are adaptations of cards from all card games except Hearthstone (e.g., Magic: The Gathering).
 11. Cards that are adaptations of existing Hearthstone cards.
 12. Hearthstone obsolete content, like cards prior to being nerfed, beta cards, etc.
 13. Hearthstone uncollectible content, like adventure content.
 14. All other Hearthstone content.
 15. Individual cards that use copyrighted mainstream content, like mainstream Marvel and DC superheroes, big HBO TV shows, etc. Consider making a whole Hero Class instead.
 16. Joke and parody cards.
 
The way to read this chart is to think about how your content can meet the qualifications to be as close to #1 for both measures. Fully coded original hero classes with tests will get the most QA attention and will be integrated the fastest. However, since some people are great designers and not so great programmers and vice versa, the maintainers are committed to code your content or improve your designs on the Discord.

As a general policy, except in very rare instances, we say a hard no to joke cards.

### Programming Cards

Contributing cards consists of writing specially-crafted JSON files into the `cards` directories and writing tests for them.

Use the complete reference [here](https://hiddenswitch.github.io/Spellsource-Server/). In particular, the [spells](https://hiddenswitch.github.io/Spellsource-Server/net/demilich/metastone/game/spells/package-summary.html) reference is handy for learning exactly how spells (effects) work. 

Additionally, to make it easier to contribute Hearthstone card functionality, the project has an automated test that accepts a URL to a community-standardized `cards.json` and checks which cards are missing. To use this feature, set the environment variable `SPELLSOURCE_CARDS_URL` to `https://api.hearthstonejson.com/v1/latest/enUS/cards.json`, or modify [CatalogueTests](game/src/test/java/com/blizzard/hearthstone/CatalogueTests.java) `getCurrentCards()` method to retrieve the latest URL.

Let's run through a complete example of implementing a card, "Exampler" that reads: `Neutral (1) 4/4. Battlecry: Summon a 5/5 Skeleton for your opponent.`

 1. In GitHub, **fork** the Spellsource-Server repository. Clone your fork. You'll save your new card to this fork.
 2. Create a file, [minion_exampler.json](https://hiddenswitch.github.io/Spellsource-Server/blob/master/cards/src/main/resources/cards/custom/minion_exampler.json), in the directory `cards/src/main/resources/cards/custom`. If the `custom` folder does not exist, create it; or, create a folder named after the game or mode for which you are creating cards.
 3. Find a similar card to start as a base. In this case, we'll search for cards that summon other cards. Let's use [Rattling Rascal](https://hiddenswitch.github.io/Spellsource-Server/blob/master/cards/src/main/resources/cards/hearthstone/knights_of_the_frozen_throne/neutral/minion_rattling_rascal.json). Copy the contents of that card into `minion_exampler.json`.
 4. Edit the appropriate fields to create this card. My version is below:

     ```json
     {
       "name": "Exampler",
       "baseManaCost": 1,
       "type": "MINION",
       "heroClass": "ANY",
       "baseAttack": 4,
       "baseHp": 4,
       "rarity": "EPIC",
       "description": "Battlecry: Summon a 5/5 Skeleton for your opponent",
       "battlecry": {
         "targetSelection": "NONE",
         "spell": {
           "class": "SummonSpell",
           "card": "token_skeletal_enforcer",
           "targetPlayer": "OPPONENT"
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

     A more detailed documentation of what all these fields mean is forthcoming. You're strongly encouraged to look at existing cards to see how various fields, like `battlecry`, `trigger`, and `attributes` work. The various enumerations can be found in the code, but most surprisingly hero classes have been renamed to colors.

 5. Write a test that verifies that the card works. We'll create a new file, [ExampleCardTests](https://hiddenswitch.github.io/Spellsource-Server/blob/master/game/src/test/java/com/hiddenswitch/spellsource/ExampleCardTests.java), that uses a "gym" to test that the card does what it is supposed to do. Here's an example test for Exampler:

    ```java
    package com.hiddenswitch.spellsource;

    import net.demilich.metastone.tests.util.TestBase;
    import org.testng.Assert;
    import org.testng.annotations.Test;

    public class ExampleCardTests extends TestBase {
      @Test public void testExampler() {
        runGym((context, player, opponent) -> {
          playCard(context, player, "minion_exampler");
          Assert.assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(),
          "token_skeletal_enforcer",
          "The opponent should have a Skeletal Enforcer after Exampler is summoned");
        });
      }
    }
    ```

    These tests can be as involved as you'd like, and should explore corner cases or interactions whenever possible. Many simple cards do not require tests. But when you start writing your own code to implement cards, tests are especially important to verify functionality. **All** community-contributed cards that get distributed to the production Spellsource server must have tests.

 6. Run your tests by executing `./gradlew game:test` on Mac or `gradlew.bat game:test` on Windows from a command line. If the engine has an issue parsing your card, you'll see an error in `CardValidationTests` with your card name specified. Other errors may occur due to differences in how projects run on Windows versus macOS; check the messages carefully for errors about your cards. If you don't see any about your cards, and you didn't change anything about other cards, you can safely proceed. For example, you can ignore issues related to "Weaponized Piñata" on Windows, because Windows does not read the "ñ" character correctly.

 7. To play with the card, start the server and client using the instructions in the Quick Start guide.

 8. Inside the client, choose Quick Play and create a new deck. The format for the deck list uses a standardized community pattern. Here's my example deck list:

     ```text
     Name: Test Deck Name
     Class: Warrior
     29x Exampler
     1x Cheat Cost Zero
     ```

    Select this deck when starting your game. Note the three hashes to indicate the start of a deck name; otherwise, the formatting given here is the minimal amount of content needed to make a valid deck.

    You can support more diverse scenarios/Tavern Brawls by specifying a Hero Card by name. For example, create a custom hero named `Enchantress` and add the line `Hero Card: Enchantress` to your decklist.
 9. You will now play against an AI using the card. To play against others on your local network, enter Matchmaking instead of Quick Play. As long as your opponent's client is running on the local network and the network supports UDP broadcasting (most local Wi-Fi networks), your opponent's client will discover your local server. In the Spellsource client, a toast will appear at the bottom of your login screen indicating that you have connected to a local server if it successfully found one.
 10. Once you are satisfied with your card, format it correctly using Python:
    
     ```bash
     # If you haven't already installed the Spellsource package, do so now.
     pip3 install spellsource
     spellsource format-cards
     ```
     
 11. To contribute the card to the public networking servers, commit your changes to your fork with `git commit -am "A custom note about the card"`, `git push` and then pull-request to this repository.