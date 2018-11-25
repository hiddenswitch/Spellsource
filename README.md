# Spellsource-Server

[![Build Status](https://travis-ci.org/hiddenswitch/Spellsource-Server.svg?branch=master)](https://travis-ci.org/hiddenswitch/Spellsource-Server)
[![Discord](https://img.shields.io/badge/chat-join%20us%20on%20discord-blue.svg?longCache=true&style=flat&icon=discord)](https://discord.gg/HmbESh2)
[![Documentation](https://img.shields.io/badge/docs-java-yellow.svg?longCache=true&style=flat)](https://hiddenswitch.github.io/Spellsource-Server/overview-summary.html)

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
 7. [Automated Deckbuilding FAQ](#automated-deckbuilding-faq)
 8. [Getting started with Development on Windows](#getting-started-with-development-on-windows)
 9. [Troubleshooting](#troubleshooting)
 10. [Contributing Cards](#contributing-cards)

### Description

The `Spellsource-Server` project adapts and updates `metastone`, an unmaintained Hearthstone simulator, to fully support hosted, networked gameplay. It features rudimentary matchmaking, collection management and support for game mechanics that persist between matches. It currently covers 100% of Hearthstone cards, with a handful of bugs, plus hundreds of community cards.

The project also contains adapters for Amazon Elastic MapReduce for processor-intensive AI training. Please reach out to the developers in an issue if you'd like to learn more or to use part of our AWS budget for AI experimentation.

See the complete reference [here](https://hiddenswitch.github.io/Spellsource-Server/overview-summary.html).

### AI Research FAQ

Please visit [this FAQ](docs/faq.ipynb) for an example of interactively playing a match in Python using Spellsource. This example can help you get started poking around Spellsource.

### Quick Start Python

The `spellsource` package creates a bridge with the Java-based `Spellsource-Server` engine. It provides a direct 1-to-1 mapping with the Java API.

You can explore commands available in the package using this command:

```
$ spellsource --help

Usage: spellsource [OPTIONS] COMMAND [ARGS]...

Options:
  --help  Show this message and exit.

Commands:
  create-user         Creates an AWS user named USERNAME.
  format-cards        Formats JSON card files.
  hearthcards-stubs   Creates stubs from the Hearthcards.
  hs-replay-matchups  Prints a table of HSReplay matchups in TSV format.
  image-stubs         Converts images to card stubs.
  simulate            Run a simulation using AIs of a given deck matchup.
  update-dbf          Updates Hearthstone IDs.
  update-decklists    Updates the deck lists from Tempostorm.
```

You can also use the `spellsource` package programmatically. This requires **Python 3** and **Java 8 or higher**. To get started:

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
 
### Automated Deckbuilding FAQ
 
Visit the [Cluster README](cluster/README.md) for some theory on automated deckbuilding and useful scripts and extensions for performing it with Spellsource.
 
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

Visit our [website](http://playspellsource.com/contribute) for more about contributions, including guidelines.
 
##### Programming New Cards

Visit our [website](http://playspellsource.com/contribute) for more about programming cards, including an example.