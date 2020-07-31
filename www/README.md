# The Spellsource Website

This website is authored using Gatsby.js. You can add new pages by duplicating an existing one [here](src/pages-markdown/).

```shell script
npm install -g gatsby-cli
CXX="clang++ -I${JAVA_HOME}/include/darwin/" npm install --save
gatsby develop
```

After gatsby-cli is installed, 

```shell script
gatsby clean
```

On modern JDKs, `node` should be invoked with the following environment variable:

```
DYLD_INSERT_LIBRARIES=${JAVA_HOME}/lib/server/libjvm.dylib
```