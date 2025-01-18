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

The server application starts in [EntryPoint](spellsource-server/src/main/java/com/hiddenswitch/framework/EntryPoint.java). `./gradlew spellsource:runServer` uses the test [EntryPoint](spellsource-server/src/test/java/com/hiddenswitch/framework/tests/applications/EntryPoint.java).

The client is private, please contact for access on the Discord.

### Getting Started on Windows

Enable Developer Mode in Windows 10 and greater. Or, follow the [instructions here](#symlinks) to enable symlinking on Windows headlessly.

Hit `Win + X` and click Windows PowerShell (Admin). Then run the following:

```pwsh
# installs chocolatey
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
# install dependencies
wsl --install
choco install -y gsudo git.portable 7zip openjdk vcredist140 docker-desktop dotnet-sdk nvm python gradle
# separately add vs2022 compilation tools with clang
# must be run separately
choco install -y visualstudio2022buildtools
# allows building with il2cpp for Windows
choco install -y visualstudio2022-workload-nativedesktop --package-parameters "--add Microsoft.VisualStudio.Component.VC.Llvm.Clang --add Microsoft.VisualStudio.Component.VC.Llvm.ClangToolset"
# disable realtime monitoring from windows defender, since it interferes with developer workloads
Set-MpPreference -DisableRealtimeMonitoring $true
```

Then, close the window and restart Windows for Docker Desktop to be enabled.

Once you've rebooted, start Docker Desktop at least once. Observe you may be prompted by "Ubuntu" to set a username and password. This is for your Linux-on-Windows install. There is no right answer, but it is safe to use the username and password of your Windows account.

Hit `Win + X` and click Windows Powershell. Do not start a prompt as an `admin`. Then run the following:

```sh
# install node 22
nvm install 22
nvm use 22

# enable git symlinks in git
git config --global core.symlinks=true
git lfs install

# now clone the repository
cd ~/Documents/
git clone https://github.com/hiddenswitch/Spellsource.git
cd Spellsource
# you are now in the spellsource directory, get the submodules
git submodule update --init --recursive
```

You should now be able to run the tests.

```shell
gradle test
```

Start a local server and website using:

```shell
gradle runServer
```

### Unity Requirements

Install Unity 6 with the iOS, macOS, Windows, and Android modules for your platform. You should not install Visual Studio.

##### Getting Around Unity

In order to visualize the battlefield from the correct camera angle, navigate in the menu **Tools | UIT | Projection | Dimetric1x2**. Then, click the **Eyeball** button in the **Hierarchy** pane on **Canvas (UI)** to hide it from the **Scene** view. Select **World** in the **Hierarchy** pane, then hit F to focus, then zoom.

To work on a screen, unhide the **Canvas (UI)**, click **2D** in the **Scene** pane, select **Root**, then change the **Screen View | Current screen** property in the Inspector to the screen you want to work on. Screen Views will automatically update the current page to whatever you are clicked on, but it is clunky when you click away.

When getting ready to commit, ensure your resolution is `Full HD (1920x1080)`, and in the **Game** pane, uncheck **Low Resolution Aspect Ratios**, which may only be clickable on Hi-DPI screens. Then, set the **Screen View | Current screen** to `Loading` on the **Root** game object.

##### Starting the Game

There are four main components:

 - The Java server backend:
   **Windows**: `./gradlew.bat spellsource-server:run`
   **macOS and Linux**: `./gradlew spellsource-server:run`
 - The website: `yarn install --immutable; cd spellsource-web; yarn run dev`
 - The image generator:
   **Windows**: `./gradlew.bat venv; & .venv/scripts/activate.ps1; comfyui -w ./spellsource-python/workdir`
   **Linux and macOS:** `./gradlew venv; source .venv/bin/activate; comfyui -w ./spellsource-python/workdir`
 - The Unity client: Open `spellsource-client/src/unity` in Unity 6, open the **Assets/Scenes/Game.unity** scene, and hit Play.

Start the Java server backend first, since it also starts Postgres and Redis.

The server is ready to accept connections from clients when you see this message:

```text
11:22:30.497 [vert.x-eventloop-thread-0] INFO  c.hiddenswitch.framework.Application - Started application, now broadcasting
```

##### Using `toxiproxy`

Testing connectivity is easy using `toxiproxy`. This will allow you to reproduce issues with disconnecting clients.

```shell
brew install toxiproxy
toxiproxy-server -port 8474 -host localhost
# in another tab
toxiproxy-cli create --listen localhost:8082 --upstream localhost:8081 myserver
```

Now, toggle connectivity:

```shell
toxiproxy-cli toggle myserver
```


##### Configure your IDE for Python development:

1. Run `gradle venv`.
2. Install the **Python** plugin inside the [**Plugin Marketplace**](jetbrains://idea/settings?name=Plugins). By default, this pane is navigated to **Installed**, make sure to click **Marketplace** to find it.
3. Check **Generate *.iml files for modules imported from Gradle** in the [**Gradle**](jetbrains://idea/settings?name=Build%2C+Execution%2C+Deployment--Build+Tools--Gradle) settings.
4. In **Project Structure**:
    1. Visit **SDKs** and add the `python` executable for your platform at `.venv/bin/python` or `.venv/scripts/python.exe`.
    2. Visit **Facets** and add the Python facet to your root project. Then select the Python interpreter corresponding to the SDK you added (typically named Spellsource).

Sometimes autocomplete / modules cannot be detected in IntelliJ even after this configuration. Open **Project Structure**, delete your Python Facet on the root module, add it again, then **File > Invalidate Caches**.


#### Symlinks

To enable symlink creation without enabling developer mode, you can use the following script. This requires
administrative privileges:

```pwsh
$exportPath = "$env:TEMP\secpol.cfg"
$importPath = "$env:TEMP\secpol_modified.cfg"
$dbPath = "$env:TEMP\secpol.sdb"
secedit /export /cfg "$exportPath"
(Get-Content $exportPath) -replace '^SeCreateSymbolicLinkPrivilege.*$', ("SeCreateSymbolicLinkPrivilege = *S-1-5-32-544,*" + [System.Security.Principal.WindowsIdentity]::GetCurrent().User.Value) | Set-Content $importPath
secedit /import /db "$dbPath" /cfg "$importPath" /overwrite
Remove-Item $exportPath
Remove-Item $importPath
Remove-Item $dbPath
```

### Getting started with Development on macOS

Requirements: **Java 21 or later** and **Docker**. Check your current version of Java using `java --version`.

1. Install dependencies:
   ```shell script
   # XCode binaries
   xcode-select --install
   # Brew
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
   # Docker. Look carefully at any messages brew tells you and do them
   brew cask install docker
   # Java (if required)
   # Install openjdk 21 or later, dotnet 6.0 & gradle 8.3 or higher
   brew install openjdk dotnet-sdk gradle
   sudo ln -sfn /usr/local/opt/openjdk/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
   brew link --force openjdk
   ```
2. Clone the repository:
   ```shell script
   git clone https://github.com/hiddenswitch/Spellsource.git
   cd Spellsource
   ```
3. See Spellsource-specific tasks using `./gradlew tasks --group spellsource`.
4. Run tests using `./gradlew test`
5. Start a local server using `./gradlew run`. This will download about 9GB of content.

macOS requires larger receive buffer limits:

```shell
sudo sysctl -w kern.ipc.maxsockbuf=33554432
sudo sysctl -w net.inet.udp.recvspace=29554432
```

### Contributing Cards

Visit the [Contribution Guide](CONTRIBUTE.md) for more about contributions, including guidelines.

### Deployment

Use `./gradlew tasks --group spellsource` to see all deployment related tasks. You will need to be an Administrative
user for these.

### Troubleshooting

> I see errors about the Gradle daemon already running.

This is an issue if you are accidentally already running the server in another terminal tab or your IDE. Carefully check that you are not already running the server using a Gradle command.

This is normal when using the IntelliJ IDEs, or when killing Gradle with Ctrl+C on Windows. You can run `gradle --stop` to clean these "daemons" (background processes), or restart your computer.

> I cannot clone the repository.

The GitHub Desktop app is not supported. Please use the `git` command line commands to clone the repository:

```shell script
git clone https://github.com/hiddenswitch/Spellsource.git
```

> I am having issues with Git Submodules, like failures to download

Public users do not have access to the private repositories that fail to download. You can safely ignore those errors.
If you'd like to contribute to the private repositories, like the game client, please use the Discord invite link above
and discuss with the team there.

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

Make sure the Public group
is [added here](https://appstoreconnect.apple.com/WebObjects/iTunesConnect.woa/ra/ng/app/1257566265/testflight?section=iosbuilds).

> `./gradlew spellsource-server:run` hangs with
> error `Caused by: org.testcontainers.containers.ContainerLaunchException: Timed out waiting for log output matching '.*waiting for connections on port.*'`

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

> Autocomplete, code insight, intellisense or other code completion features are missing when I am trying to write code
> accessing the generating protobufs definitions, like `Spellsource.java` or `Hiddenswitch.java`

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
