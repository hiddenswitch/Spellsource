#!/bin/bash

./gradlew net:shadowJar
screen -L java -javaagent:net/lib/quasar-core-0.7.5-jdk8.jar -cp net/build/libs/net-1.3.0-all.jar com.hiddenswitch.proto3.net.applications.Remote
