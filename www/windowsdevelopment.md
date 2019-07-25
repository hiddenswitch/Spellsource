---
layout: page
title: Windows Development
permalink: /windows-development/
---

Developing for Spellsource on Windows starts with installing dependencies, a good code editor, and familiarizing yourself with some common Java code practices.

Follow this guide to be able to test your cards and make changes to the game code on Windows 10 and later.

### Table of Contents

 1. [Prerequisites](#1-prerequisites)
 2. [Download Spellsource](#2-download-spellsource)
 3. [Open the Project](#3-open-the-project)
 4. [Common Coding Tasks](#4-common-coding-tasks)
    1. [Editing an Existing Card](#41-editing-an-existing-card)
    2. [Creating a New Card](#42-creating-a-new-card)
    3. [Creating a Custom Spell](#43-creating-a-custom-spell)
    4. [Writing a New Bot](#44-writing-a-new-bot)
 5. [Testing](#5-testing)
    1. [Running Test Code](#51-running-test-code)
    2. [Understanding Traces](#52-understanding-traces)
    3. [Connecting to a Local Server](#53-connecting-to-a-local-server)
 6. [Contributing Your Work](#6-contributing-your-work)

### 1. Prerequisites

 1. Install some helpful Windows development utilities.
    1. [7-Zip](https://www.7-zip.org/a/7z1900-x64.exe) for a friendlier way to open zip files.
    2. [ConEmu](https://www.fosshub.com/ConEmu.html?dwl=ConEmuSetup.190526.exe) (download the installer) for a better console.
    3. [Git for Windows](https://github.com/git-for-windows/git/releases/download/v2.22.0.windows.1/Git-2.22.0-64-bit.exe) which also installs some helpful console programs. Hit next on all the prompts, since it's a little confusing.
 2. Install Java 12.
    1. Start by [downloading the zip file for Windows (OpenJDK 12.0.1)](https://download.java.net/java/GA/jdk12.0.1/69cfe15208a647278a19ef0990eea691/12/GPL/openjdk-12.0.1_windows-x64_bin.zip) from the [OpenJDK website](https://jdk.java.net/12/).
    2. Extract the zip file to your `C:\Program Files` directory. If you did this correctly, you should be able to find `java.exe` at  `C:\Program Files\jdk-12.0.1\bin\java.exe`.
 3. Install [MongoDB 4](https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-4.0.10-signed.msi).
 4. Add `java`, `git` and `mongod` to your PATH:
    1. Hit the Windows key to bring open the Start menu, and type "This PC".
    2. Right click on the This PC result and choose Properties. You should now see the `Control Panel\System and Security\System` control panel pane.
    3. Click Advanced System Settings in the left sidebar.
    4. Click the Advanced Tab.
    5. Click the Environment Variables button.
    6. In the System variables pane, double click Path to edit it.
    7. For each of the following paths, click the New button and set the text to the specified values below. You will make the below 3 entries, then click "OK."
        1. `C:\Program Files\jdk-12.0.1\bin`
        2. `C:\Program Files\MongoDB\Server\4.0\bin`
        3. `C:\Program Files\Git\bin`
    8. Back to the System variables pane, click "New..."
        1. In the "New System Variable" tab, enter `JAVA_HOME` as variable name, and `C:\Program Files\jdk-12.0.1` as variable value.
 5. Install [IntelliJ IDEA Community Edition](https://download.jetbrains.com/idea/ideaIC-2019.1.3-jbr11.exe) to use as a code editor.
 
### 2. Download Spellsource

 1. Fork the code on GitHub.
    1. [Create a GitHub account](https://github.com/join) or login with your existing one.
    2. Visit [Spellsource-Server](https://github.com/hiddenswitch/Spellsource-Server).
    3. Click Fork in the upper right corner to fork it into your account. This creates a copy of the game you can edit freely.
    4. In your fork's page, click clone or download, and copy the URL shown there. For example, if your username is `bdg`, you will see the URL `https://github.com/bdg/Spellsource-Server`
 1. Open ConEmu.
    1. The first time you run it, you will be prompted to configure it. Under "Choose your startup task or even a shell with arguments:", choose `{Shells::PowerShell (Admin)}`.
    2. Hit OK.
    3. You will now be in a console window that resembles `PS C:\Users\YourUsername> ` with a blinking cursor.
 2. Enter commands to download the Spellsource code.
    1. First, "change directory" into your Documents folder with the command `cd .\Documents\` and hit enter.
    2. Then, download the code by writing `git clone ` (notice the space at the end), then pasting in the URL you copied from GitHub. If your username is `bdg` on GitHub, the command will look like: `git clone https://github.com/bdg/Spellsource-Server`.
    3. Change directory into this code folder with the command `cd .\Spellsource-Server`.
    4. Create a project file for IntelliJ with the command `./gradlew.bat idea`. This may take a while!
    
### 3. Open the Project

 1. Start IntelliJ and hit next on all the prompts.
 2. Click Open, and navigate to your Spellsource-Server directory.
 3. Be very patient while it loads, which may take a while. IntelliJ's progress appears in the lower right corner.
 4. Set your code style:
    1. Go to File > Settings.
    2. Navigate to Editor > Font.
    3. Change your font to Fira Code Retina. This will make text more legible.
    4. Again inside settings, navigate to Editor > Code Style.
    5. Click the gear icon to the right of Scheme, then choose Import Scheme > IntelliJ IDEA code style XML.
    6. Click the 3rd icon from the left above the file path, which looks like a folder with a mini IntelliJ IDEA logo in the lower right corner. This navigates you to the project folder.
    7. Choose idea-codestyle-scheme.xml in your project directory.
 5. Configure IntelliJ to run the project correctly.
    1. Go to File > Settings.
    2. Navigate to Build, Execution, Deployment > Build Tools > Gradle.
    3. Under Delegate settings, both combo boxes should be set to Gradle.
    4. Navigate to Build, Execution, Deployment > Build Tools > Gradle > Runner.
    5. Check Delegate IDE build/run actions to Gradle.
    6. Set Run tests using: to Gradle Test Runner.

You have now configured a working Spellsource-Server editing environment.

### 4. Common Coding Tasks

Learn more about how the Spellsource engine works by exploring the documentation in the code or [located here](https://hiddenswitch.github.io/Spellsource-Server/index.html).

Any changes you make should be documented in the `www/whatsnew.md` file. Open this file and edit the latest version with the appropriate fix or content addition notes.

Let's go over some common coding tasks to get you started with contributions.

#### 4.1 Editing an Existing Card

 1. Make sure IntelliJ IDEA is open.
 2. Hit Shift twice to bring up the universal search, and enter the name of your card. In this example, I'll write Abholos.
 3. Observe there may be multiple results. Choose the file that appears to be a `.json` file located in the `cards/` directory. In this case, we'd choose the `minion_abholos.json` file.
 4. Once you've made any changes, you may need to edit tests. Typically, the card's name is located in its test. Try hitting Shift twice and searching for Abholos. In this case, there is a `testAbholos` method. Written in Java, you will need to update this test. Run the test by clicking the green play button icon to the left of the test method declaration in the gutter of the editor.

Visit the documentation about [CardDesc](https://hiddenswitch.github.io/Spellsource-Server/net/demilich/metastone/game/cards/desc/CardDesc.html) to learn how this card format works. You can browse the documentation to learn more about any specific effect. Search the word you see in the `.json` file inside the search in the documentation.

#### 4.2 Creating a New Card

 1. In the Project tool window showing all the files in the project in the left hand side of the editor, navigate to `cards/src/main/resources/cards/custom/group10`. If you don't see this pane, navigate to View > Tool Windows > Project.
 2. Create a new `.json` file in this directory.
 3. Copy and paste the contents of a card similar to yours to get started.
    1. To find these cards, you can search the cards.
    2. Hit Ctrl+Shift+F to bring up the Find in Path window.
    4. Check the File Mask box, and write `*.json` to match only card code files.
    4. Check the Regex box. This lets you make sophisticated text searches.
    5. Searches will take the form of `"description": ".*keyword1.*keyword2.*morekeywords`.
        1. For example, to find cards that give taunt, search `"description": ".*give.*taunt`. Observe before each keyword you write `.*`, which signals to the regex search to allow any number of words in between your keywords.
        2. To find cards that are an opener or a battlecry, search `"description": ".*(opener)|(battlecry)`. Observe the keywords are wrapped in parentheses and separated by a pipe character.
 4. Make sure the card's set line looks like `"set": "CUSTOM"`.
 
Use the complete reference [here](https://hiddenswitch.github.io/Spellsource-Server/). In particular, the [spells](https://hiddenswitch.github.io/Spellsource-Server/net/demilich/metastone/game/spells/package-summary.html) reference is handy for learning exactly how spells (effects) work. 

Let's run through a complete example of implementing a card, "Exampler" that reads: `Neutral (1) 4/4. Opener: Summon a 5/5 Skeleton for your opponent.`

 1. In IntelliJ, create a file, [minion_exampler.json](https://hiddenswitch.github.io/Spellsource-Server/blob/master/cards/src/main/resources/cards/custom/minion_exampler.json), in the directory `cards/src/main/resources/cards/custom/group10`.
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
       "description": "Opener: Summon a 5/5 Skeleton for your opponent",
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

#### 4.3 Creating a Custom Spell

Sometimes effects are too difficult to implement in the JSON scripting format and Java is better suited.

This example will implement the spell, "Summon the minion with the most copies in your deck."

 1. Create a spell whose `"spell": {"class"...` is `custom.SummonMinionWithMostCopiesInDeckSpell`:

     ```json
     {
       "name": "A Common Summoner",
       "baseManaCost": 6,
       "type": "SPELL",
       "heroClass": "JADE",
       "rarity": "EPIC",
       "description": "Summon the minion with the most copies in your deck.",
       "targetSelection": "NONE",
       "spell": {
         "class": "custom.SummonMinionWithMostCopiesInDeckSpell"
       },
       "collectible": true,
       "set": "CUSTOM",
       "fileFormatVersion": 1
     }
     ```
 2. Create a new Java file corresponding to this spell.
    1. In the Project panel, navigate to the `game/src/main/java/net/demilich/metastone/game/spells/custom/` directory by expanding the little triangles.
    2. Right click on the directory icon with the white dot in it corresponding to `custom`.
    3. Choose New > Java Class.
    4. Enter `SummonMinionWithMostCopiesInDeckSpell`
    5. Write `extends Spell` after the class name.
    6. The class will now appear to have a red underline underneath it. Hit Alt-Enter and choose Implement methods... Alt-Enter is a general hotkey for "Help me."
    7. Add the annotation `@Suspendable` to `onCast`.
    8. Write `/**` above the `public class Summon...` line, and hit enter. You will now have autocompleted a comment block where you should document what this spell does.
    9. Hit Ctrl-Alt-L to autoformat the file.
    10. Your code will now look like this:
      
         ```java
         package net.demilich.metastone.game.spells.custom;
         
         import co.paralleluniverse.fibers.Suspendable;
         import net.demilich.metastone.game.GameContext;
         import net.demilich.metastone.game.Player;
         import net.demilich.metastone.game.entities.Entity;
         import net.demilich.metastone.game.spells.Spell;
         import net.demilich.metastone.game.spells.desc.SpellDesc;
         
         /**
          * Summons a minion from the player's deck with the most copies in the deck. If there are multiple minions with the most
          * copies, summon one at random.
          */
         public class SummonMinionWithMostCopiesInDeckSpell extends Spell {
         	@Override
         	@Suspendable
         	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
         
         	}
         }

        
         ```
 3. Author the spell.
    1. The `player` variable corresponds to the player who's currently invoking the spell. The `source` is, in this case, the card being played, but is generally the origin of the effect. The `target` is `null` in this case, because the player did not choose a target, but it is typically the player's chosen target.
    2. To reuse an existing spell effect, like a summon, create a `new SpellDesc(SummonSpell.class)`, then case it using `SpellUtils.castChildSpell`. The name of the argument to `new SpellDesc` will correspond to the `"class":` items you find in the existing cards.
    3. We want something of the form:
        
        ```json
        {
          "class": "SummonSpell",
          "card": /*the most common card*/
        }
        ``` 
        
       To do this, you will start with a `SpellDesc` and `put` arguments into it:
       
        ```java
        SpellDesc summonSpell = new SpellDesc(SummonSpell.class);
        summonSpell.put(SpellArg.CARD, /* the most common card */);
        ```
       
       Observe that the key `"target"` that normally appears in the JSON corresponds to an enum value `SpellArg.TARGET` in the `SpellDesc`. You can find corresponding `SpellArgs` by looking for the `UPPER_CASE` formatted version of JSON keys. Check your work using double Shift to find the `SpellArg`.
    4. Iterate through the player's deck to find the minion card with the most copies, then summon it:
         
         ```java
         Map<String, Integer> countOfCard = new HashMap<>();
         for (int i = 0; i < player.getDeck().size(); i++) {
         	Card card = player.getDeck().get(i);
         	if (card.getCardType() != CardType.MINION) {
         		continue;
         	}
         	int newCount = countOfCard.getOrDefault(card.getCardId(), 1);
         	countOfCard.put(card.getCardId(), newCount);
         }
         // Find the highest count card
         int maxCount = Integer.MIN_VALUE;
         List<String> maxCardIds = new ArrayList<>();
         for (String cardId : countOfCard.keySet()) {
         	int count = countOfCard.get(cardId);
         	if (count > maxCount) {
         		maxCount = count;
         		maxCardIds.clear();
         		maxCardIds.add(cardId);
         	} else if (count == maxCount) {
         		maxCardIds.add(cardId);
         	}
         }
         SpellDesc summonSpell = new SpellDesc(SummonSpell.class);
         String randomCardId = context.getLogic().removeRandom(maxCardIds);
         summonSpell.put(SpellArg.CARD, randomCardId);
         SpellUtils.castChildSpell(context, player, summonSpell, source, target);
         ```
    5. There are several alternative ways to author this spell and make it better. An important revision is to set the spell to extend a `SummonSpell` instead of a `Spell`, so that this effect interacts with other effects that specifically deal with summoning. Then, we'll use `super.onCast` instead of `SpellUtils.castChildSpell` to call the original effect. We also need to deal with the fact that the spell might not find any minions:
    
        ```java
        public class SummonMinionWithMostCopiesInDeckSpell extends SummonSpell {
        	@Override
        	@Suspendable
        	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        		Map<String, Integer> countOfCard = new HashMap<>();
        		for (int i = 0; i < player.getDeck().size(); i++) {
        			Card card = player.getDeck().get(i);
        			if (card.getCardType() != CardType.MINION) {
        				continue;
        			}
        
        			int newCount = countOfCard.getOrDefault(card.getCardId(), 1);
        			countOfCard.put(card.getCardId(), newCount);
        		}
        		// Find the highest count card
        		int maxCount = Integer.MIN_VALUE;
        		List<String> maxCardIds = new ArrayList<>();
        		for (String cardId : countOfCard.keySet()) {
        			int count = countOfCard.get(cardId);
        			if (count > maxCount) {
        				maxCount = count;
        				maxCardIds.clear();
        				maxCardIds.add(cardId);
        			} else if (count == maxCount) {
        				maxCardIds.add(cardId);
        			}
        		}
        		
        		if (maxCardIds.isEmpty()) {
        			return;
        		}
        
        		SpellDesc summonSpell = new SpellDesc(SummonSpell.class);
        		String randomCardId = context.getLogic().removeRandom(maxCardIds);
        		summonSpell.put(SpellArg.CARD, randomCardId);
        		super.onCast(context, player, summonSpell, source, target);
        	}
        }
        ```
        
 4. Write a test for your card using the examples in this document. 
 
#### 4.4 Writing a New Bot

 1. For an example of an existing bot, navigate to `GameStateValueBehaviour` by searching for it using the double Shift search.
 2. Create a new intelligent bot by navigating to `IntelligentBehaviour`.
 3. Place your cursor on the class name in the editor, hit Alt+Enter, and choose Implement abstract class.
 4. Name your bot along the pattern of "TechnologyBehaviour". For example, if you use neural networks as the underlying technology, call it `NeuralNetworkBehaviour`.
 5. Implement the methods.
 6. Set the bot used by the server to your new bot:
    1. Navigate to the `Bots` class in the `net` package.
    2. Observe there is a static field, `BEHAVIOUR`. Observe it is a supplier, a zero-arg function that returns a new instance of a `Behaviour`.
    3. Change the supplier to provide an instance of your behaviour. For example, if your behaviour's class is `NeuralNetworkBehaviour`, change it to `AtomicReference<Supplier<? extends Behaviour>> BEHAVIOUR = new AtomicReference<>(NeuralNetworkBehaviour::new)`.
 
### 5. Testing

Use these procedures to test your code either with coded tests or by interacting directly with the server.

#### 5.1 Running Test Code

 1. To test a card, navigate to `CustomTests.java` and observe the pattern for testing cards. This involves learning a lot of Java. You can search for a card's test by hitting Shift twice and writing `test` followed by the card name. For example, to find Abholos's test, search `testAbholos`.
 2. Click the play button in the editor's left gutter to run the test.
 3. Commonly, you will have syntax errors in your JSON files. These errors are printed in the test results in the window at the bottom of IntelliJ. They are difficult to interpret.
 
You can run all game tests by executing `./gradlew.bat game:test` inside ConEmu on Windows. If the engine has an issue parsing your card, you'll see an error in `CardValidationTests` with your card name specified. Other errors may occur due to differences in how projects run on Windows versus macOS; check the messages carefully for errors about your cards.

#### 5.2 Understanding Traces

When you run `game:test`, changes that cause exceptions in `testRandomMassPlay` (a [fuzzer](https://en.wikipedia.org/wiki/Fuzzing)) will create files in the `game` directory, like `game/masstest-trace-2019-06-14T20_21_02_86166.json`.

Use these to help you debug rare interactions or errors you didn't test in your cards.

 1. Configure IntelliJ to break on useful exceptions.
    1. Navigate to Run > View Breakpoints.
    2. Click the plus icon in the left list and choose Java Exception Breakpoints.
    3. Write `java.lang.RuntimeException` and hit OK.
    4. In the right pane:
        1. Check Suspend, and choose **All**.
        2. Check Condition, and set it to `!(this instanceof CancellationException)`.
        3. Check Class filters, and set it to `com.hiddenswitch.* net.demilich.*`.
        4. Under Notifications, check Caught exception and Uncaught Exception.
    5. Click Done.
 2. Drag and drop the `.json` trace files into `game/src/test/resources/traces`.
 3. Navigate to `testTraces` by hitting shift twice and searching for it.
 4. Click the play button in the left gutter of the editor, and then choose Debug (the bug icon).
 5. Observe you will "break" on the exception that caused your test to fail. Look carefully for a `source` variable in the callstack of the Debug pane at the bottom, which you can navigate by clicking further down in the Stack panel. Examine the `source`, which is typically an in-game reference to the card whose effect is causing the issue.
 6. Fix the issue.
 7. Run the `testTraces` method again, which will exactly reproduce the issue. If the test now passes, you have fixed the issue successfully.
 8. Try running `testRandomMassPlay` by navigating to it with double Shift or by using `./gradlew.bat game:test`, and see if it passes now.

#### 5.3 Connecting to a Local Server

 1. Disable your firewalls.
 2. Open ConEmu or create a new tab using the green plus icon button.
 3. Start the MongoDB database.
    1. Create a directory to store the data in using the following command: `New-Item -ItemType Directory -Force -Path .mongo`. Observe it is a little verbose, but this ensures you create a directory only if it doesn't already exist. It is strongly recommended to use `.mongo` as the directory name, because if you do this command inside your `Spellsource-Server` directory, that `.mongo` directory will be specially ignored by git when you save your work.
    2. Run the database using the command `mongod --dbpath .mongo --bind_ip_all`.
 4. Start the server inside the IntelliJ editor.
    1. Navigate to the `LocalClustered.java` file.
    2. Click the play button in the editor's left gutter to execute it. Be patient.
    3. Once you observe `***** SERVER IS READY. START THE CLIENT. *****`, navigate to the next step.
 5. Start a client to connect to the local server.
    1. Download and install the [Hidden Switch Launcher](http://go.hiddenswitch.com/win) if you haven't already.
    2. Launch the game.
    3. Observe a popup that says, "Connected to local server ..."
 6. Create an account. Remember, this belongs to your local server instance only.
 7. Observe you are now playing on your local server. You can visit the Collection screen to navigate to any new cards.
 8. When you are done testing, close the applications.
    1. Close the Spellsource client application.
    2. Shut down the server by hitting the red Stop button in the IntelliJ interface.
    3. Shut down the database by closing the tab in ConEmu.

You can improve the performance of starting the server by disabling Windows Defender. You can permanently disable Windows Defender using [Defender Control](https://www.majorgeeks.com/files/details/defender_control.html).

### 6. Contributing Your Work 

 1. In IntelliJ, go to VCS > Commit...
 2. Check the boxes next to the files that you have added or modified.
 3. Author a clear commit message.
 4. Click Commit.
 5. Go to VCS > Git > Push.
 6. Leave the defaults and click Push.
 7. Finally, go to VCS > Git > Create Pull Request, and follow the on screen instructions.