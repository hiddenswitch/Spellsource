# Spellsource-Server

[![Build Status](https://travis-ci.org/hiddenswitch/Spellsource-Server.svg?branch=master)](https://travis-ci.org/hiddenswitch/Spellsource-Server)
[![Discord](https://img.shields.io/badge/chat-join%20us%20on%20discord-blue.svg?longCache=true&style=flat&icon=discord)](https://discord.gg/HmbESh2)
[![Documentation](https://img.shields.io/badge/docs-java-yellow.svg?longCache=true&style=flat)](https://hiddenswitch.github.io/Spellsource-Server/overview-summary.html)

This is a simulator and game server for Spellsource, a community-authored card game, licensed under the Affero GPLv3.

**Play Now**: Download the Hidden Switch Launcher for [Mac OS X](http://go.hiddenswitch.com/mac) or [Windows](http://go.hiddenswitch.com/win) to get a copy of the game client. Play online against others! No other installation required.

**Developers**: See [this example notebook](docs/simulation_example.ipynb) for how to simulate games.

Please see the Issues tab to report bugs or request functionality.

### Contents

 1. [Changelist](www/whatsnew.md)
 2. [Description](#description)
 3. [AI Research FAQ](#ai-research-faq)
 4. [Quick Start Python](#quick-start-python)
 5. [Quick Start Multiplayer](#quick-start-multiplayer)
 6. [Quick Start Contributing Cards](#quick-start-contributing-cards)
 7. [Getting started with Development on Windows](#getting-started-with-development-on-windows)
 8. [Contributing Cards](#contributing-cards)

### Changelist

Read [the latest changes here](www/whatsnew.md) or the deployed changes on the [website](https://playspellsource.com/whats-new).

### Description

The `Spellsource-Server` project is a 2-player card battler that supports hosted, networked gameplay. It features rudimentary matchmaking, collection management and support for game mechanics that persist between matches.

See the complete code reference [here](https://hiddenswitch.github.io/Spellsource-Server/overview-summary.html).

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
  change-password       Changes a Spellsource user's password.
  create-user           Creates an AWS user named USERNAME.
  format-cards          Formats JSON card files.
  hearthcards-stubs     Creates stubs from the Hearthcards.
  hs-replay-matchups    Prints a table of HSReplay matchups in TSV format.
  image-stubs           Converts images to card stubs.
  markdown-to-textmesh  Renders a Markdown file to TextMesh markup.
  replicate-database    Replicates mongo databases.
  simulate              Run a simulation using AIs of a given deck matchup.
  update-dbf            Updates Hearthstone IDs.
  update-decklists      Updates the deck lists from Tempostorm.
```

You can also use the `spellsource` package programmatically. This requires **Python 3** and **Java 12 or higher** (only **Java 12** tested). To get started:

 1. Install a Java JDK.
    - On Windows: Visit [this link](https://github.com/ojdkbuild/ojdkbuild) for the latest OpenJDK builds.
    - On macOS: Install `brew`, then `brew cask install java`.
 2. `pip3 install spellsource` to install the latest version of the package. To build from Git, use `pip3 install -e .` to install the package from the root of this repository, and run `./gradlew net:shadowJar` to build the engine.
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

If you'd like to **contribute or edit cards**, **write new game mechanics** or **improve the server**, visit the [Windows Development Guide](http://playspellsource.com/windows-development).
 
### Getting started with Development on Windows

Visit the [Windows Development Guide](http://playspellsource.com/windows-development) for more about Windows development.


### Contributing Cards

Visit our [website](http://playspellsource.com/contribute) for more about contributions, including guidelines.


### Special Thanks

YourKit supports open source projects with its full-featured Java Profiler.

YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.
