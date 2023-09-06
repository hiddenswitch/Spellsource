import { classpath, ensureJvm, importClass } from "java-bridge";
import path from "path";

export const java = {
  classpath: {
    push: classpath.append,
  },
  import: importClass,
};

// use real classpath path
java.classpath.push(
  path.join(
    process.cwd(),
    "../spellsource-web-cardeditor-support/build/libs/spellsource-web-cardeditor-support-all.jar"
  )
);

ensureJvm({
  opts: [
    "--enable-preview",
    "--add-opens",
    "java.base/java.lang=ALL-UNNAMED",
    "--add-modules",
    "jdk.incubator.concurrent",
  ],
});
