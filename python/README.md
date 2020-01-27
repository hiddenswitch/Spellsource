# Developing with the Python Module

On **macOS**, from the root `Spellsource-Server` directory:

```shell script
# install basic dependencies
brew install python@3
# the following lines change CONSTANTLY so always search for how to install java
brew tap mongodb/brew
brew install mongodb-community
brew link --force mongodb-community
brew tap adoptopenjdk/openjdk # assuming this tap was not already added
brew cask install adoptopenjdk11 # this command *may* need a password to succeed
```

On all Unix platforms:

```shell script
# build the spellsource dependencies
./gradlew net:shadowJar
./gradlew hearthstone:jar
# create a virtual environment using the python 3 interpreter
virtualenv -m python3 .venv
source .venv/bin/activate
# now you're in the root directory of the module
cd python
# install an editable copy
pip install -e .
```