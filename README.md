# Spellsource-Server

[![Build Status](https://travis-ci.org/hiddenswitch/Spellsource.svg?branch=master)](https://travis-ci.org/hiddenswitch/Spellsource)
[![Discord](https://img.shields.io/badge/chat-join%20us%20on%20discord-blue.svg?longCache=true&style=flat&icon=discord)](https://discord.gg/HmbESh2)
[![Documentation](https://img.shields.io/badge/docs-java-yellow.svg?longCache=true&style=flat)](https://www.playspellsource.com/javadoc)

This is a simulator and game server for Spellsource, a community-authored card game, licensed under the Affero GPLv3.

**[Play now in your browser, or download for your platform here.](https://www.playspellsource.com/download)**

Please see the [Issues](https://github.com/hiddenswitch/Spellsource/issues) tab to report bugs or request functionality. 

### Changelist

Read [the latest changes here](www/src/pages-markdown/whatsnew.md) or the deployed changes on the [website](https://www.playspellsource.com/whats-new).

### Description

The `Spellsource-Server` project is a 2-player card battler that supports hosted, networked gameplay. It features rudimentary matchmaking, collection management and support for game mechanics that persist between matches.

See the complete code reference [here](https://www.playspellsource.com/javadoc).

### Getting Around

Cards are located at [cards/src/main/resources/cards/custom](cards/src/main/resources/cards/custom).

To implement new effects (called **Spells** inside Spellsource) add a new Spell subclass to [game/src/main/java/net/demilich/metastone/game/spells](game/src/main/java/net/demilich/metastone/game/spells).

You can learn more about the Spellsource AI as implemented in the [GameStateValueBehaviour](game/src/main/java/net/demilich/metastone/game/behaviour/GameStateValueBehaviour.java) class.

The server application starts in [Clustered](net/src/main/java/com/hiddenswitch/spellsource/net/applications/Clustered.java). `./gradlew netRun` uses [LocalClustered](net/src/test/java/com/hiddenswitch/spellsource/net/tests/LocalClustered.java).

The client is private, please contact for access on the Discord.

### Tasks

```shell script
$ ./gradlew tasks --group spellsource

> Task :tasks

------------------------------------------------------------
Tasks runnable from root project
------------------------------------------------------------

Spellsource tasks
-----------------
bumpVersion - Bumps the server version
cloneMongo - Connects to the production database, dumps its database file, then restores the database to your local mongo.
deployAll - Deploys the server, the client and the website
distAndroid - Builds and uploads the Android project to Google Play
distIOS - Builds and uploads to Testflight the iOS project
distSteam - Uploads the macOS and Windows builds to Steam
distSwarm - Deploys to a Docker Swarm
distWebGL - Uploads the WebGL build to playspellsource.com
distWWW - Builds and deploys the website (requires npm, python, the .venv virtualenv installed)
netRun - Starts the Spellsource server
netRunDebug - Starts the Spellsource server attachable as a Remote debug target from IntelliJ
swagger - Generates sources from the client/swagger-api.yaml. Run this whenever you change that file.
testAll - Runs all tests. When testing custom cards, failed fuzzing results are put in cards/src/test/resources/traces by testRandomMassPlay.

To see all tasks and more detail, run gradle tasks --all

To see more detail about a task, run gradle help --task <task>

```

### Cloning this repository

This repository uses **Git Submodules**. This means, if you have the proper authorization, you'll be able to access all the source code using:

```shell script
git clone https://github.com/hiddenswitch/Spellsource.git
cd Spellsource
git submodule update --init --recursive
```

Failures are normal if you do not have permissions to the repositories.

If you have the permissions, you will need to add your SSH key to the private repositories, for both BitBucket and GitHub, to access all of them. Contact us on the Discord at the start of this document if you'd like to contribute to private work like the game client.

You cannot use the GitHub Desktop app to download this repository.

### Getting started with Development on macOS

Requirements: **Java 11 or later** and **Docker**. Check your current version of Java using `java --version`.

 1. Install dependencies:
    ```shell script
    # XCode binaries
    xcode-select --install
    # Brew
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
    # Docker. Look carefully at any messages brew tells you and do them
    brew cask install docker
    # Java (if required)
    brew install java
    # Not sure why brew doesn't just do this for you
    sudo ln -sfn /usr/local/opt/openjdk/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
    brew link --force java
    ```
 2. Clone the repository:
    ```shell script
    git clone https://github.com/hiddenswitch/Spellsource.git
    cd Spellsource
    ```
 3. See Spellsource-specific tasks using `./gradlew tasks --group spellsource`.
 4. Run tests using `./gradlew testAll`
 5. Start a local server using `./gradlew netRun`
 6. Generate project files using `./gradlew idea`, then open the project with **IntelliJ Community Edition**. You can install this with `brew cask install intellij-idea-ce`.

### Getting started with Development on Windows

Development on Windows is currently not supported.

### Contributing Cards

Visit the [Contribution Guide](www/src/pages-markdown/contribute.md) for more about contributions, including guidelines.

### Deployment

Use `./gradlew tasks --group spellsource` to see all deployment related tasks. You will need to be an Administrative user for these.

### Troubleshooting

> I cannot clone the repository.

The GitHub Desktop app is not supported. Please use the `git` command line commands to clone the repository:

```shell script
git clone https://github.com/hiddenswitch/Spellsource.git
```

> I am having issues with Git Submodules, like failures to download

Public users do not have access to the private repositories that fail to download. You can safely ignore those errors. If you'd like to contribute to the private repositories, like the game client, please use the Discord invite link above and discuss with the team there.

> I am seeing issues with too many files open.

On macOS, issue the following commands to increase your per-process limits:

```shell script
sudo sysctl -w kern.maxfiles=5242880
sudo sysctl -w kern.maxfilesperproc=524288
ulimit -n 200000
sudo launchctl limit maxfiles 524288 5242880
```

> `testTraces` is failing.

You had failures in `testRandomMassPlay`, the fuzzer for Spellsource. These are real issues.

> I receive an error from `fastlane` for the `gradle distIOS` command: `(<unknown>): found unexpected end of stream while scanning a quoted scalar at line 1 column 1`

Log into Apple using `fastlane spaceauth` and paste the new session into [unityclient-build.env](secrets/spellsource/unityclient-build.env).

> I uploaded to Steam, but I do not see the build.

Visit the [Steam partner's page](https://partner.steamgames.com/apps/builds/987160) and promote the build.

> I uploaded to TestFlight but the build isn't public yet.

Make sure the Public group is [added here](https://appstoreconnect.apple.com/WebObjects/iTunesConnect.woa/ra/ng/app/1257566265/testflight?section=iosbuilds).

> The Discord bot will not compile due to an error that reads, in part, "Classes that should be initialized at run time got initialized during image building"

Add the class or the package containing it to end of the list of classes in the `Args = --initialize-at-build-time=...` line to [native-image.properties](discordbot/src/main/resources/META-INF/native-image/com.hiddenswitch/discordbot/native-image.properties).

You may have to regenerate reflection config using **sdkman**'s Graal distributable. Then, `sdk use java 20.0.0.r11-grl; ./gradlew --no-daemon clean; ./gradlew --no-daemon discordbot:genReflectionProps`.

### Special Thanks

![YourKit](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.

YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.
