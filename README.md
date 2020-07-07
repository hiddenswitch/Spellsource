# Spellsource-Server

[![Build Status](https://travis-ci.org/hiddenswitch/Spellsource.svg?branch=master)](https://travis-ci.org/hiddenswitch/Spellsource)
[![Discord](https://img.shields.io/badge/chat-join%20us%20on%20discord-blue.svg?longCache=true&style=flat&icon=discord)](https://discord.gg/HmbESh2)
[![Documentation](https://img.shields.io/badge/docs-java-yellow.svg?longCache=true&style=flat)](https://www.playspellsource.com/javadoc)

This is a simulator and game server for Spellsource, a community-authored card game, licensed under the Affero GPLv3.

**[Play now in your browser, or download for your platform here.](www/src/pages/download.js)**

Please see the [Issues](https://github.com/hiddenswitch/Spellsource/issues) tab to report bugs or request functionality. 

### Changelist

Read [the latest changes here](www/src/pages-markdown/whatsnew.md) or the deployed changes on the [website](https://www.playspellsource.com/whats-new).

### Description

The `Spellsource-Server` project is a 2-player card battler that supports hosted, networked gameplay. It features rudimentary matchmaking, collection management and support for game mechanics that persist between matches.

See the complete code reference [here](https://www.playspellsource.com/javadoc).

### Tasks

Make sure to be running **Mongo** when you start the server.

```shell script
$ ./gradlew tasks --group contributors
------------------------------------------------------------
Tasks runnable from root project
------------------------------------------------------------

Contributors tasks
------------------
netRun - Starts the Spellsource server
netRunDebug - Starts the Spellsource server attachable as a Remote debug target from IntelliJ
testAll - Runs all tests. Make sure mongod is running. When testing custom cards, failed fuzzing results are put in cards/src/test/resources/traces by testRandomMassPlay.

To see all tasks and more detail, run gradle tasks --all
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

### Getting started with Development on macOS

Requirements: Java 11 or later, Mongo Community Edition 3.6, Python 3.7 or later, Node 10 or later.

 1. Install all dependencies except Java using `./deploy.sh -D`.
 2. See Spellsource-specific tasks using `./gradlew tasks --group contributors`.
 3. Run **Mongo** using `mkdir -pv .mongo/ && mongod --bind_ip_all --dbpath=.mongo/`, then run tests using `./gradlew testAll`
 4. Open the project with **IntelliJ Community Edition**.

### Getting started with Development on Windows

Visit the [Windows Development Guide](www/src/pages-markdown/windowsdevelopment.md) for more about Windows development.

Like with macOS, you can see Spellsource-specific gradle tasks using `./gradlew tasks --group contributors`.

### Contributing Cards

Visit the [Contribution Guide](www/src/pages-markdown/contribute.md) for more about contributions, including guidelines.

### Deployment

Use `./gradlew tasks --group spellsource` to see all deployment related tasks. You will need to be an Administrative user for these.

### Troubleshooting

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

> All the tests in `net:test` fail or take too long to complete.

Make sure you are running `mongo`.

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

You may have to regenerate reflection config using `sdk use java 20.0.0.r11-grl; ./gradlew --no-daemon clean; ./gradlew --no-daemon discordbot:genReflectionProps`.

### Special Thanks

![YourKit](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.

YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.
