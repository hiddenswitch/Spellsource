#!/bin/bash
if [ -z "$STY" ]; then
  exec screen -dm -S serverscreen /bin/bash "$0";
fi
./gradlew clean
./gradlew net:shadowJar
java -javaagent:net/lib/quasar-core-0.7.5-jdk8.jar -cp net/build/libs/net-1.3.0-all.jar com.hiddenswitch.spellsource.applications.Remote