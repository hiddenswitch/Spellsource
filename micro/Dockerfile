#FROM oracle/graalvm-ce:19.3.0-java8 as graalvm
FROM oracle/graalvm-ce:19.3.0-java11 as graalvm
RUN gu install native-image
COPY . /home/app/micro
WORKDIR /home/app/micro
RUN native-image --no-server --no-fallback --initialize-at-build-time --static -cp build/libs/micro-*-all.jar

FROM scratch
EXPOSE 8080
COPY --from=graalvm /home/app/micro/micro /app/micro
ENTRYPOINT ["/app/micro", "-Djava.library.path=/app"]
