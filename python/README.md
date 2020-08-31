# Developing with the Python Module

On **macOS**, from the root `Spellsource-Server` directory:

```shell script
# install basic dependencies
brew install python@3
# the following lines change CONSTANTLY so always search for how to install java
brew cask install docker
brew install openjdk
```

Then, on all Unix platforms:

```shell script
# build the spellsource dependencies
./gradlew net:shadowJar
# If you have access to internalcontent sources
./gradlew internalcontent:jar
# create a virtual environment using the python 3 interpreter
virtualenv -m python3 .venv
source .venv/bin/activate
# now you're in the root directory of the module
cd python
# install an editable copy
pip install -e .
```