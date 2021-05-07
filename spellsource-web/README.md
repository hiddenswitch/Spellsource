# The Spellsource Website

This website is authored using Gatsby.js. You can add new pages by duplicating an existing one [here](src/pages-markdown/).

## Installing

**On macOS:**

```shell script
npm install -g gatsby-cli
# Ensure test dependencies get built correctly
npm install
```

After gatsby-cli is installed, run the website with `gatsby develop`.

```shell script
gatsby clean
gatsby develop
```


## Testing

Running the tests requires the polyglot environment.

#### Enable polyglot Java development

```bash
# command line utilities for compilation
xcode-select --install
# prevents brew from complaining in 2021, this will constantly change
brew install python@3.9
brew unlink python@3.9
# dependencies for node canvas package
brew install pkg-config cairo pango libpng jpeg giflib librsvg
# sdkman for graal
curl -s "https://get.sdkman.io" | bash
sdk install java 21.0.0.2.r11-grl
sdk use java 21.0.0.2.r11-grl
gu install nodejs
npm install --legacy-peer-deps --save --nodedir="${JAVA_HOME}/languages/js" --build-from-source
```

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