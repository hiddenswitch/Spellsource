FROM oracle/graalvm-ce:20.0.0-java11 as graalvm
RUN gu install native-image

# Context is the root project!
ADD ./ /app
WORKDIR /app
RUN ./gradlew --no-daemon discordbot:shadowJar
RUN cp /app/discordbot/build/libs/discordbot-*-all.jar /app/app.jar
RUN javap -cp /app/app.jar com.hiddenswitch.spellsource.discordbot.applications.DiscordBot && \
    native-image --verbose \
    --no-server \
    --no-fallback \
    --enable-http \
    --enable-https \
    --allow-incomplete-classpath \
    -Djava.net.preferIPv4Stack=true \
    -jar /app/app.jar discordbot_exec

# use phusion baseimage because we need glibc
FROM phusion/baseimage:0.11
# Put in the compiled binary
COPY --from=graalvm /app/discordbot_exec /app/discordbot
# Remember the root project is the context
ADD discordbot/bin/run /etc/service/discordbot/run
ENV DISCORD_BOT_API_KEY=needs_token

CMD ["/sbin/my_init"]