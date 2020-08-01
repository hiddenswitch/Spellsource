# The Spellsource Website

This website is authored using Gatsby.js. You can add new pages by duplicating an existing one [here](src/pages-markdown/).


## Installing

**On macOS:**

```shell script
npm install -g gatsby-cli
# Ensure test dependencies get built correctly
CXX="clang++ -I${JAVA_HOME:-`/usr/libexec/java_home`}/include/darwin/" npm install
```

After gatsby-cli is installed, run the website with `gatsby develop`.

```shell script
gatsby clean
```

## Running Tests


First, execute the `shadowJar` task to build the JAR from the root project directory.

```
gradle www:shadowJar
```

On modern JDKs to run the test, `node` should be invoked with the following environment variable:

```
DYLD_INSERT_LIBRARIES=${JAVA_HOME:-`/usr/libexec/java_home`}/lib/server/libjvm.dylib
```

This means `jest` should be executed with the environment variable first. You can modify your run configuration in IntelliJ to add the environment varialbe. Note IntelliJ does not do environment variable expansion.

Command line:

```
DYLD_INSERT_LIBRARIES="${JAVA_HOME:-`/usr/libexec/java_home`}/lib/server/libjvm.dylib" ./node_modules/.bin/jest
```