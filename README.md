# Spellsource

<img width="664" alt="Screenshot 2023-01-10 at 9 49 52 PM" src="https://user-images.githubusercontent.com/2229300/211727827-c259ed44-ae92-404b-9366-9648e307af9c.png">

[![Discord](https://img.shields.io/badge/chat-join%20us%20on%20discord-blue.svg?longCache=true&style=flat&icon=discord)](https://discord.gg/HmbESh2)

This is a simulator and game server for Spellsource, a community-authored card game.

Please see the [Issues](https://github.com/hiddenswitch/Spellsource/issues) tab to report bugs or request functionality.

### Description

The `Spellsource-Server` project is a 2-player card battler that supports hosted, networked gameplay. It features matchmaking, collection management and support for game mechanics that persist between matches.

### Getting Around

Cards are located at [spellsource-cards-git/src/main/resources/cards/custom](https://github.com/hiddenswitch/Spellsource/tree/master/spellsource-cards-git).

To implement new effects (called **Spells** inside Spellsource) add a new Spell subclass to [spellsource-game/src/main/java/net/demilich/metastone/game/spells](spellsource-game/src/main/java/net/demilich/metastone/game/spells).

You can learn more about the Spellsource AI as implemented in the [GameStateValueBehaviour](spellsource-game/src/main/java/net/demilich/metastone/game/behaviour/GameStateValueBehaviour.java) class.

The server application starts in [EntryPoint](spellsource-server/src/main/java/com/hiddenswitch/framework/EntryPoint.java). `./gradlew spellsource:run` uses the test [EntryPoint](spellsource-server/src/test/java/com/hiddenswitch/framework/tests/applications/EntryPoint.java).

The client is private, please contact for access on the Discord.

### Tasks

```shell script
$ ./gradlew tasks --group spellsource
> Task :tasks

------------------------------------------------------------
Tasks runnable from root project 'spellsource'
------------------------------------------------------------

Spellsource tasks
-----------------
buildAndroid - Builds the Android version of the game using deploy.sh file and your local Unity version.
buildIOS - Builds the Windows version of the game using deploy.sh file and your local Unity version.
buildMacOS - Builds the macOS versions of the game using deploy.sh file and your local Unity version.
buildWindows - Builds the Windows version of the game using deploy.sh file and your local Unity version.
deploySpellsourceProd - Deploys Spellsource to production. Requires access to Spellsource secrets.
distAndroid - Builds and uploads the Android project to Google Play
distIOS - Builds and uploads to Testflight the iOS project
distSteam - Uploads the macOS and Windows builds to Steam
distWWW - Builds and deploys the website
migrationsOrSqlChanged - Run whenever the schema as encoded in spellsource-server/src/main/resources/db/migration has changed - recreates the JOOQ DSL and dumps a ddl.sql file for autocompletion
protosChanged - Run whenever the files in src/main/proto have been changed. Recreates the models and updates the client.
run - Starts the Spellsource server locally. Starts all dependencies using Docker (must be installed).
test - Runs the test suite.
testLegacyMigration - Tests the migration from the legacy server to this current server 
```

### Cloning this repository

You do not have access to the private submodules. Use the following commands to clone the repository:

```shell
git clone https://github.com/hiddenswitch/Spellsource.git
cd Spellsource
```

This repository uses **Git Submodules**. This means, if you have the proper authorization, you'll be able to access all the source code using:

##### Prerequisites

Make sure `longpaths` is enabled on Windows. Run a `bash` or other command prompt **as administrator**, and then execute `git config --system core.longpaths true`.

##### All platforms

```shell
git lfs install
git clone --recurse-submodules git@github.com:hiddenswitch/Spellsource.git
cd Spellsource
git submodule update --init --recursive
```

Failures are normal if you do not have permissions to the repositories.

If you have the permissions, you will need to add your SSH key to the private repositories, for both BitBucket and GitHub, to access all of them. Contact us on the Discord at the start of this document if you'd like to contribute to private work like the game client.

You cannot use the GitHub Desktop app to download this repository.

### Getting started with Development on macOS

Requirements: **Java 19 or later** and **Docker**. Check your current version of Java using `java --version`.

 1. Install dependencies:
    ```shell script
    # XCode binaries
    xcode-select --install
    # Brew
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
    # Docker. Look carefully at any messages brew tells you and do them
    brew cask install docker
    # Java (if required)
    # Install openjdk 19 or higher
    brew install openjdk dotnet-sdk
    sudo ln -sfn /usr/local/opt/openjdk/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
    brew link --force openjdk
    ```
 2. Clone the repository:
    ```shell script
    git clone https://github.com/hiddenswitch/Spellsource.git
    cd Spellsource
    # If you have access to the client, this will succeed.
    git submodule update --init --recursive unityclient/src/unity
    ```
 3. See Spellsource-specific tasks using `./gradlew tasks --group spellsource`.
 4. Run tests using `./gradlew test`
 5. Start a local server using `./gradlew run`

### Contributing Cards

Visit the [Contribution Guide](CONTRIBUTE.md) for more about contributions, including guidelines.

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

> I uploaded to Steam, but I do not see the build.

Visit the [Steam partner's page](https://partner.steamgames.com/apps/builds/987160) and promote the build.

> I uploaded to TestFlight but the build isn't public yet.

Make sure the Public group is [added here](https://appstoreconnect.apple.com/WebObjects/iTunesConnect.woa/ra/ng/app/1257566265/testflight?section=iosbuilds).

> `./gradlew distSwarm` fails with the message of the form:

```shell script
#13 71.30 FAILURE: Build failed with an exception.
#13 71.30 
#13 71.30 * What went wrong:
#13 71.30 Could not determine the dependencies of task ':net:shadowJar'.
#13 71.30 > Could not resolve all dependencies for configuration ':net:runtimeClasspath'.
#13 71.30    > Could not resolve project :vertx-redis-cluster.
#13 71.30      Required by:
#13 71.30          project :net
#13 71.30       > Unable to find a matching configuration of project :subproject:
#13 71.30           - None of the consumable configurations have attributes.
```

Make sure to add the sub project directory and any others that need to be visible to Docker to [.dockerignore](.dockerignore) in the form of `!directory/*`.

> `./gradlew spellsource-server:run` hangs with error `Caused by: org.testcontainers.containers.ContainerLaunchException: Timed out waiting for log output matching '.*waiting for connections on port.*'`

Make sure to use your local `docker` context using `docker context use default`.

> I receive an error starting the server with `gradle spellsource-server:run` of the form:

```shell script
main ERROR o.t.d.DockerClientProviderStrategy Could not find a valid Docker environment. Please check configuration. Attempted configurations were:
Exception in thread "main" 20200811T112136 main ERROR o.t.d.DockerClientProviderStrategy     UnixSocketClientProviderStrategy: failed with exception InvalidConfigurationException (ping failed). Root cause NoSuchFileException (/var/run/docker.sock)
org.testcontainers.containers.ContainerLaunchException: Container startup failed
main ERROR o.t.d.DockerClientProviderStrategy As no valid configuration was found, execution cannot continue
```

Restart Docker. Make sure Docker is running.

> I cannot connect to the Hidden Switch cluster to deploy the servers.

You need special authorization for this. It is accessed via an audited API key.

> I receive an error related to `(sharp:42678): GLib-CRITICAL **: 17:17:14.186: g_hash_table_lookup: assertion 'hash_table != NULL' failed`

Delete the NPM modules folder: `rm -rf www/npm_modules`, then rerun `./gradlew distWWW`.

> Autocomplete, code insight, intellisense or other code completion features are missing when I am trying to write code accessing the generating protobufs definitions, like `Spellsource.java` or `Hiddenswitch.java`

In IntelliJ, visit the Help > Edit Custom Properties... menu, then add the following lines:

```
# custom IntelliJ IDEA properties
idea.max.intellisense.filesize=99999
```

### Special Thanks

![YourKit](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.

YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.
