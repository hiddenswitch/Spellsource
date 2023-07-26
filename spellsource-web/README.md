# Spellsource Web

This is the package for the HTML and related backend server content for Spellsource. This includes the online collections UI.

## Generating Images from PSD/PSBs

```shell script
# get python 3.7+
brew install python
# create a python environment if it doesn't already exist
if [[ ! -f .venv/bin/activate ]]; then
  pip3 install -U virtualenv >/dev/null
  virtualenv -p python3 .venv
fi

source .venv/bin/activate
# install the spellsource python package
pip install -e python
spellsource psb-2-png-layers --help
```

For example, to generate a single image of the map and save it, 3x scale, to Map.png:
```shell script
spellsource psb-2-png-layers --merge=TRUE --scale=3 ~/Dropbox/Spellsource\ Art/Spellsource\ World\ Map.psd Map.png
```