# Spellsource-Server

[![Build Status](https://travis-ci.org/hiddenswitch/Spellsource-Server.svg?branch=master)](https://travis-ci.org/hiddenswitch/Spellsource-Server)

![In-Game Screenshot](https://thumbs.gfycat.com/EverlastingGiddyBluetickcoonhound-size_restricted.gif)

This is a simulator and game server for community and official Hearthstone cards.

**Play Now**: Download the Hidden Switch Launcher for [Mac OS X](http://go.hiddenswitch.com/mac) or [Windows](http://go.hiddenswitch.com/win) to get a copy of the game client. Play online against others! No other installation required.

**Developers**: See [this example notebook](docs/simulation_example.ipynb) for how to simulate games.

Please see the Issues tab to report bugs or request functionality.

### Contents

 1. [Description](#description)
 2. [AI Research FAQ](#ai-research-faq)
 3. [Quick Start Python](#quick-start-python)
 4. [Quick Start Multiplayer](#quick-start-multiplayer)
 5. [Quick Start Contributing Cards](#quick-start-contributing-cards)
 6. [Using the Command Line Simulator](#using-the-command-line-simulator)
 7. [Getting started with Development on Windows](#getting-started-with-development-on-windows)
 8. [Troubleshooting](#troubleshooting)
 9. [Contributing Cards](#contributing-cards)

### Description

The `Spellsource-Server` project adapts and updates `metastone`, an unmaintained Hearthstone simulator, to fully support hosted, networked gameplay. It features rudimentary matchmaking, collection management and support for game mechanics that persist between matches. It currently covers 100% of Hearthstone cards, with a handful of bugs, plus hundreds of community cards.

The project also contains adapters for Amazon Elastic MapReduce for processor-intensive AI training. Please reach out to the developers in an issue if you'd like to learn more or to use part of our AWS budget for AI experimentation.

### AI Research FAQ

Please visit [this FAQ](docs/faq.ipynb) for an example of interactively playing a match in Python using Spellsource. This example can help you get started poking around Spellsource.

### Quick Start Python

The `spellsource` package creates a bridge with the Java-based `Spellsource-Server` engine. It provides a direct 1-to-1 mapping with the Java API.

This requires **Python 3** and **Java 8 or higher**. To get started:

 1. Install the Java JDK from [Oracle's website](http://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html).
 2. `pip3 install spellsource` to install the latest version of the package.
 3. Start a game and play it with the specified bots:
 
    ```python
    from spellsource.context import Context
    from spellsource.playrandombehaviour import PlayRandomBehaviour

    with Context() as ctx:
        game_context = ctx.game.GameContext.fromTwoRandomDecks()
        behaviour1 = PlayRandomBehaviour()
        behaviour2 = PlayRandomBehaviour()
        game_context.setBehaviour(0, behaviour1.wrap(ctx))
        game_context.setBehaviour(1, behaviour2.wrap(ctx))
        game_context.play()
        assert game_context.updateAndGetGameOver()
    ```

Visit [`GameStateValueBehaviour`](spellsource/gamestatevaluebehaviour.py) to see an implementation of a complex AI bot in Python. This is a direct port of the Java code. Unfortunately, on the Python platform, remoting (accessing the Java engine) in the particular way this bot does is slow. To implement more sophisticated bots, consider adding a method to `GameContext` that will extract the exact data, in a binary format, that you need in your Python implementation, to reduce the communication overhead between Java and Python.

### Quick Start Multiplayer

 1. Download the Hidden Switch Launcher for [Mac OS X](http://go.hiddenswitch.com/mac) or [Windows](http://go.hiddenswitch.com/win).
 2. Download the Spellsource Client from within the launcher and start it.
 3. Enter Quick Play to play against a bot, or Matchmaking to play against a random opponent.

### Quick Start Contributing Cards

If you'd like to **contributed or edit cards**, **write new game mechanics** or **improve the server**, follow these instructions to install and run the server:

 1. Install the Java JDK from [Oracle's website](http://www.oracle.com/technetwork/java/javase/downloads/jdk10-downloads-4416644.html)
 2. Clone this repository.
 3. To run the server locally, execute the following on a command prompt:
    * Linux/Mac OS X: Run `./gradlew net:local`.
    * Windows: See the **Getting started with Development on Windows** guide below.
 4. Download the Hidden Switch Launcher for [Mac OS X](http://go.hiddenswitch.com/mac) or [Windows](http://go.hiddenswitch.com/win).
 5. Download the Spellsource Client from within the launcher and start it.
 6. Your game client will automatically detect your local server and connect to it, as long as the server  is running before you start the client.

### Using the Command Line Simulator

 1. Build the `cluster` shadow JAR: `./gradlew cluster:shadowJar`.
 2. Execute the simulator with `java -cp cluster/build/libs/cluster-1.3.0-all.jar com.hiddenswitch.cluster.applications.Simulate`. Read the help there.
 
### Getting started with Development on Windows

   1. Windows Defender significantly slows down or fails install processes. To temporarily turn off Windows Defender, hit the `Windows` key, type `Windows Defender` and open the `Windows Defender Security Center`.
     1. Then, visit the `Virus & threat protection` page, then `Virus & threat protection settings`, and turn off all protection modes.
     2. Go back to the home page by clicking the `Home` icon on the left. Visit `Firewall & network protection`, then turn off firewall for both `Private` and `Public` networks.
     3. You will be reminded to re-enable real-time protection at the end of this document.
   2. Hit `Start`, type `PowerShell`, right click on the `Windows PowerShell` result and choose `Run as Administrator`.
   3. From the `chocolatey` docs, we'll run the following commands:
      ```
      Set-ExecutionPolicy AllSigned;
      Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'));
      choco feature enable -n allowGlobalConfirmation;
      ```
      This installs `chocolatey`, the Windows development package manager.
   4. We'll now install basic development packages. This includes the MongoDB, Java 8 SDK, `git` and `ConEmu`, a great Windows terminal emulator.
      ```
      choco install chocolatey-core.extension git.install git-credential-manager-for-windows jdk8 conemu
      ```
      Then, install [IntelliJ Idea Community Edition](https://www.jetbrains.com/idea/download/#section=windows) to edit the `Spellsource-Server` Java project. Since sometimes `choco` packages fail to install, you might need to manually install [MongoDB](https://www.mongodb.com/download-center#community), [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), and [git](https://git-scm.com/download/win).  
   5. Exit `Windows PowerShell`
   6. Start `ConEmu`. If you're starting it for the first time, observe you can specify a startup task. Choose `{Shells::PowerShell (Admin)}`.
   7. Navigate to your preferred directory for cloning the GitHub repository using `cd path\to\directory`. In this example, we'll use your user's `Documents` folder. Then, clone the repository. It is strongly recommended to clone in order to get the latest updates, instead of using `Download as zip...` from the GitHub.com interface.
      ```
      cd Documents
      git clone https://github.com/hiddenswitch/Spellsource-Server.git
      ```
   8. Enter the directory with the code files in it with the following command. Whenever you want to execute commands on files located inside the code, you'll have to **cd** (change directory) into it.
      ```
      cd Spellsource-Server
      ```
   8. Start by running the tests that don't require networking behavior to verify your installation worked. To do this, execute the following command:
      ```
      ./gradlew game:test
      ```
      A lot of packages should install. You should observe no errors.
   9. If the tests pass, you're now ready to start the server.
      1. In one tab in `ConEmu`, `cd` into your `Spellsource-Server` directory. You'll see an example of this below. Then, start MongoDB with the commands:
         ```
         cd Spellsource-Server
         md -Force .\net\.mongo\db
         & "C:\Program Files\MongoDB\Server\3.6\bin\mongod" --dbpath .\net\.mongo\db
         ```
      2. Then, in another tab, start the server. `cd` into your `Spellsource-Server` directory and run the command:
         ```
         ./gradlew net:localWindows
         ```
   10. When making changes to the files in the `cards` directory, you will need to restart the server. To restart it, you need to send the correct command to shut down the server. Unfortunately, batch files do not generally support this command correctly. To shut down correctly, you must configure the `SIGINT` command in `ConEmu`. **Never end execution by closing the console tab or Window.** Instead, use ConEmu, and configure a hotkey to send the Break key. In ConEmu, you can do this by clicking the Hamburger menu in the upper right corner, choosing settings, and then configuring a break command as documented on [StackOverflow](https://stackoverflow.com/questions/41074403/conemusend-sigint-to-running-application).
   11. Install the `Hidden Switch Launcher`, start it, `Download` the latest client in it and start the client. It will automatically connect to the local server.

### Troubleshooting

**My download got interrupted in the launcher and it won't restart.**

On Windows, delete the `%APPDATA%\Electron` directory. *(Copy and paste this into your Explorer address bar or `Ctrl+R` and type, `explorer %APPDATA%\Electron`)*.

**I receive an error about Weaponized Piñata when I try to run tests while contributing cards on Windows.**

This message can be safely ignored.

### Contributing Cards

Contributing cards consists of writing specially-crafted JSON files into the `cards` directories and writing tests for them.

Additionally, to make it easier to contribute Hearthstone card functionality, the project has an automated test that accepts a URL to a community-standardized `cards.json` and checks which cards are missing. To use this feature, set the environment variable `SPELLSOURCE_CARDS_URL` to `https://api.hearthstonejson.com/v1/latest/enUS/cards.json`, or modify [CatalogueTests](game/src/test/java/com/blizzard/hearthstone/CatalogueTests.java) `getCurrentCards()` method to retrieve the latest URL.

Let's run through a complete example of implementing a card, "Exampler" that reads: `Neutral (1) 4/4. Battlecry: Summon a 5/5 Skeleton for your opponent.`

 1. In GitHub, **fork** the Spellsource-Server repository. Clone your fork. You'll save your new card to this fork.
 2. Create a file, [minion_exampler.json](cards/src/main/resources/cards/custom/minion_exampler.json), in the directory `cards/src/main/resources/cards/custom`. If the `custom` folder does not exist, create it; or, create a folder named after the game or mode for which you are creating cards.
 3. Find a similar card to start as a base. In this case, we'll search for cards that summon other cards. Let's use [Rattling Rascal](cards/src/main/resources/cards/hearthstone/knights_of_the_frozen_throne/neutral/minion_rattling_rascal.json). Copy the contents of that card into `minion_exampler.json`.
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

 5. Write a test that verifies that the card works. We'll create a new file, [ExampleCardTests](game/src/test/java/com/hiddenswitch/spellsource/ExampleCardTests.java), that uses a "gym" to test that the card does what it is supposed to do. Here's an example test for Exampler:

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

    Visit other tests to see how more complex cards are tested. An example of modifying random outcomes can be found in [`TheOldGodsTests#testYoggSaronHopesEnd`](/game/src/test/java/com/blizzard/hearthstone/TheOldGodsTests.java). For an example of overriding a discover action, see [`JourneyToUngoroTests#testFreeFromAmber()`](game/src/test/java/com/blizzard/hearthstone/JourneyToUngoroTests.java).

 6. Run your tests by executing `./gradlew game:test` on Mac or `gradlew.bat game:test` on Windows from a command line. If the engine has an issue parsing your card, you'll see an error in `CardValidationTests` with your card name specified. Other errors may occur due to differences in how projects run on Windows versus macOS; check the messages carefully for errors about your cards. If you don't see any about your cards, and you didn't change anything about other cards, you can safely proceed. For example, you can ignore issues related to "Weaponized Piñata" on Windows, because Windows does not read the "ñ" character correctly.

 7. To play with the card, start the server and client using the instructions in the Quick Start guide.

 8. Inside the client, choose Quick Play and create a new deck. The format for the deck list uses a standardized community pattern. Here's my example deck list:

     ```text
     ### Test Deck Name
     Hero Class: Warrior
     15x Exampler
     15x Innervate
     ```

    Select this deck when starting your game. Note the three hashes to indicate the start of a deck name; otherwise, the formatting given here is the minimal amount of content needed to make a valid deck.

    You can support more diverse scenarios/Tavern Brawls by specifying a Hero Card by name. For example, create a custom hero named `Enchantress` and add the line `Hero Card: Enchantress` to your decklist.
 9. You will now play against an AI using the card. To play against others on your local network, enter Matchmaking instead of Quick Play. As long as your opponent's client is running on the local network and the network supports UDP broadcasting (most local Wi-Fi networks), your opponent's client will discover your local server. In the Spellsource client, a toast will appear at the bottom of your login screen indicating that you have connected to a local server if it successfully found one.
 10. Once you are satisfied with your card, format it correctly using Python:
    
     ```bash
     # If you haven't already installed the Spellsource package, do so now.
     pip3 install spellsource
     python3 -m 'spellsource.ext.cardformatter'
     ```
     
 11. To contribute the card to the public networking servers, commit your changes to your fork with `git commit -am "A custom note about the card"`, `git push` and then pull-request to this repository.
