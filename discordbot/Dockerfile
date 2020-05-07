FROM oracle/graalvm-ce:20.0.0-java11 as graalvm
RUN gu install native-image

# Context is the root project!
ADD ./ /app
WORKDIR /app
RUN ./gradlew --no-daemon discordbot:shadowJar
RUN cp /app/discordbot/build/libs/discordbot-*-all.jar /app/app.jar
RUN curl -L -o musl.tar.gz https://github.com/gradinac/musl-bundle-example/releases/download/v1.0/musl.tar.gz && \
    tar -xvzf musl.tar.gz
RUN javap -cp /app/app.jar com.hiddenswitch.spellsource.discordbot.applications.DiscordBot && \
    native-image --verbose \
    --no-server \
    --no-fallback \
    --enable-http \
    --enable-https \
    --allow-incomplete-classpath \
    --static \
    -H:+JNI \
    -Djava.net.preferIPv4Stack=true \
    -H:UseMuslC=bundle/ \
    -jar /app/app.jar discordbot_exec

FROM scratch
COPY --from=graalvm /app/discordbot_exec /app/discordbot
ENTRYPOINT ["/app/discordbot", "-Djava.library.path=/app"]