# Spellsource-Server

[![Build Status](https://travis-ci.org/hiddenswitch/Spellsource.svg?branch=master)](https://travis-ci.org/hiddenswitch/Spellsource)
[![Discord](https://img.shields.io/badge/chat-join%20us%20on%20discord-blue.svg?longCache=true&style=flat&icon=discord)](https://discord.gg/HmbESh2)
[![Documentation](https://img.shields.io/badge/docs-java-yellow.svg?longCache=true&style=flat)](https://www.playspellsource.com/javadoc)

This is a simulator and game server for Spellsource, a community-authored card game, licensed under the Affero GPLv3.

**[Play now in your browser, or download for your platform here.](www/src/pages-markdown/download.md)**

Please see the [Issues](https://github.com/hiddenswitch/Spellsource/issues) tab to report bugs or request functionality.

### Changelist

Read [the latest changes here](www/src/pages-markdown/whatsnew.md) or the deployed changes on the [website](https://www.playspellsource.com/whats-new).

### Description

The `Spellsource-Server` project is a 2-player card battler that supports hosted, networked gameplay. It features rudimentary matchmaking, collection management and support for game mechanics that persist between matches.

See the complete code reference [here](https://www.playspellsource.com/javadoc).
 
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

### Special Thanks

![YourKit](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.

YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.
