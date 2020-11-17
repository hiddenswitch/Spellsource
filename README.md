# Spellsource-Server

[![Build Status](https://travis-ci.org/hiddenswitch/Spellsource.svg?branch=master)](https://travis-ci.org/hiddenswitch/Spellsource)
[![Discord](https://img.shields.io/badge/chat-join%20us%20on%20discord-blue.svg?longCache=true&style=flat&icon=discord)](https://discord.gg/HmbESh2)
[![Documentation](https://img.shields.io/badge/docs-java-yellow.svg?longCache=true&style=flat)](https://www.playspellsource.com/javadoc)

This is a simulator and game server for Spellsource, a community-authored card game, licensed under the Affero GPLv3.

**[Download for your platform here.](https://www.playspellsource.com/download)**

Steam keys for the Alpha are available for bonafide testers. Contact us in the Discord.

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
    brew install openjdk@11
    # Not sure why brew doesn't just do this for you
    sudo ln -sfn /usr/local/opt/openjdk/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
    brew link --force openjdk@11
    ```
 2. Clone the repository:
    ```shell script
    git clone https://github.com/hiddenswitch/Spellsource.git
    cd Spellsource
    ```
 3. See Spellsource-specific tasks using `./gradlew tasks --group spellsource`.
 4. Run tests using `./gradlew --no-parallel test`
 5. Start a local server using `./gradlew netRun`
 6. Generate project files using `./gradlew idea`, then open the project with **IntelliJ Community Edition**. You can install this with `brew cask install intellij-idea-ce`.

### Getting started with Development on Windows

Requirements: **Java 11 or later**, **Docker**, **bash** and **GNU binutils** like MinGW that comes with **git**.

 1. Install dependencies:
    1. Java AdoptOpenJDK [11.0.9](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.9%2B11.1/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.9_11.msi) or later. During installation, choose to set `JAVA_HOME` and add `java` to your `PATH`.
    2. [Docker for Windows Desktop](https://desktop.docker.com/win/stable/Docker%20Desktop%20Installer.exe). You will be prompted to enable and install Windows Subsystem for Linux 2 (WSL2) features, carefully follow those instructions including the new MSI it downloads into your downloads folder.
    3. Git [2.29.2](https://github.com/git-for-windows/git/releases/download/v2.29.2.windows.2/Git-2.29.2.2-64-bit.exe) or later. During installation, choose **Use Git and optional Unix tools from the Windows Command Prompt**.
    4. PuTTY [0.74](https://the.earth.li/~sgtatham/putty/latest/w64/putty-64bit-0.74-installer.msi) or later. Then, follow the instructions from [here](https://vladmihalcea.com/tutorials/git/windows-git-ssh-authentication-to-github/) starting with "Installing SSH Tools" to get authorization configured for GitHub.
    5. `dotnet` [3.1](https://download.visualstudio.microsoft.com/download/pr/3366b2e6-ed46-48ae-bf7b-f5804f6ee4c9/186f681ff967b509c6c9ad31d3d343da/dotnet-sdk-3.1.404-win-x64.exe
) or later.
 2. Right click in the folder where you'd like to store your Spellsource files, then choose **Git Bash Here**. Then, clone the repository:
    ```shell script
    git clone https://github.com/hiddenswitch/Spellsource.git
    cd Spellsource
    ``` 
 3. See Spellsource-specific tasks using `TERM=mintty ./gradlew tasks --group spellsource`.
 4. Run tests using `TERM=mintty ./gradlew --no-parallel test`
 5. Start a local server using `TERM=mintty ./gradlew netRun`
 6. Generate project files using `TERM=mintty ./gradlew idea`, then open the project with **IntelliJ Community Edition**. You can install this from [here](https://www.jetbrains.com/idea/download/download-thanks.html?platform=windows&code=IIC).

Unfortunately, it's true, you have to prefix every `gradlew` command with `TERM=mintty` until a [bug in Gradle](https://github.com/gradle/gradle/issues/8204) is fixed.

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

> I uploaded to Steam, but I do not see the build.

Visit the [Steam partner's page](https://partner.steamgames.com/apps/builds/987160) and promote the build.

> I uploaded to TestFlight but the build isn't public yet.

Make sure the Public group is [added here](https://appstoreconnect.apple.com/WebObjects/iTunesConnect.woa/ra/ng/app/1257566265/testflight?section=iosbuilds).

> The Discord bot will not compile due to an error that reads, in part, "Classes that should be initialized at run time got initialized during image building"

Add the class or the package containing it to end of the list of classes in the `Args = --initialize-at-build-time=...` line to [native-image.properties](discordbot/src/main/resources/META-INF/native-image/com.hiddenswitch/discordbot/native-image.properties).

You may have to regenerate reflection config using **sdkman**'s Graal distributable. The following commands will fix this issue:

```
sdk use java 20.1.0.r11-grl
./gradlew --no-daemon clean
./gradlew --no-daemon discordbot:genReflectionProps
```

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

> `discordbot` Swarm build (`./gradlew distSwarm`) fails with `com.oracle.svm.driver.NativeImage$NativeImageError: Image build request failed with exit status 137`

On **macOS** and **Windows**, allocate more memory to your Docker host.

> `./gradlew net:run` hangs with error `Caused by: org.testcontainers.containers.ContainerLaunchException: Timed out waiting for log output matching '.*waiting for connections on port.*'`

Make sure to use your local `docker` context using `docker context use default`.

> I receive an error starting the server with `gradle net:run` of the form:

```shell script
main ERROR o.t.d.DockerClientProviderStrategy Could not find a valid Docker environment. Please check configuration. Attempted configurations were:
Exception in thread "main" 20200811T112136 main ERROR o.t.d.DockerClientProviderStrategy     UnixSocketClientProviderStrategy: failed with exception InvalidConfigurationException (ping failed). Root cause NoSuchFileException (/var/run/docker.sock)
org.testcontainers.containers.ContainerLaunchException: Container startup failed
main ERROR o.t.d.DockerClientProviderStrategy As no valid configuration was found, execution cannot continue
```

Restart Docker. Make sure Docker is running.

> The MongoDB container doesn't start with `gradle net:run` with the following error: `Timed out waiting for log output matching '.*waiting for connections on port.*'`

Try deleting your local database which is automatically bind-mounted to the container at `.mongo`:

```shell script
rm -rf .mongo/
```

> I cannot connect to the Hidden Switch cluster to deploy the servers.

You need special authorization for this. It is accessed via an audited API key.

> I receive an error related to `(sharp:42678): GLib-CRITICAL **: 17:17:14.186: g_hash_table_lookup: assertion 'hash_table != NULL' failed`

Delete the NPM modules folder: `rm -rf www/npm_modules`, then rerun `./gradlew distWWW`.

### Special Thanks

![YourKit](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.

YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.
