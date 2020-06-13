FROM oracle/graalvm-ce:20.0.0-java11 as graalvm
RUN gu install native-image
COPY ./micro /home/app/micro
COPY ./cards /home/app/cards
COPY ./game /home/app/game
WORKDIR /home/app/micro
RUN native-image \
    --no-server \
    --no-fallback \
    --initialize-at-build-time=com.hiddenswitch.spellsource.micro.BotsController,com.hiddenswitch.spellsource.core.JsonConfiguration,io.vertx.core.json,net.demilich,com.google.common.base.Preconditions,com.google.common.base.Ascii,com.google.common.base.CharMatcher,com.google.common.base.CharMatcher\$InRange,com.google.common.base.CharMatcher\$Is,com.google.common.base.CharMatcher\$FastMatcher,com.google.common.base.CaseFormat,com.google.common.base.CaseFormat\$3,com.google.common.base.CaseFormat\$1,com.google.common.base.CaseFormat\$4,com.google.common.base.CaseFormat\$2,com.google.common.base.CaseFormat\$5 \
    --static \
    -H:+TraceClassInitialization \
    -H:ReflectionConfigurationFiles=src/main/resources/META-INF/native-image/com.hiddenswitch.spellsource/micro/reflect-config.json \
    -H:DynamicProxyConfigurationFiles=src/main/resources/META-INF/native-image/com.hiddenswitch.spellsource/micro/proxy-config.json \
    -H:ResourceConfigurationFiles=src/main/resources/META-INF/native-image/com.hiddenswitch.spellsource/micro/resource-config.json \
    -H:IncludeResources=src/main/resources/logback.xml,src/main/resources/application.yml \
    -H:Name=micro \
    -H:Class=com.hiddenswitch.spellsource.micro.Application \
    -cp build/libs/micro-*-all.jar

FROM scratch
EXPOSE 8080
COPY --from=graalvm /home/app/micro/micro /app/micro
ENTRYPOINT ["/app/micro", "-Djava.library.path=/app"]
