# JMusicBot Dockerfile
FROM maven:3.8.5-openjdk-17 AS builder

ADD . /JMusicBot/
WORKDIR /JMusicBot

# Build JMusicBot
RUN mvn clean
RUN mvn compile
RUN mvn test-compile
RUN mvn test
RUN mvn install

# Build final image using alpine (Distroless) for smaller image size
FROM alpine:3.18.2
COPY --from=builder /JMusicBot/target/JMusicBot-Snapshot-All.jar /JMusicBot/JMusicBot.jar

# Install useful packages
RUN apk update
RUN apk add --no-cache bash
RUN apk add --no-cache nano
RUN apk add --no-cache openjdk17
RUN apk add --no-cache vim

# https://github.com/AdoptOpenJDK/openjdk-docker/issues/75#issuecomment-469899609
RUN apk add --no-cache fontconfig
RUN apk add --no-cache ttf-dejavu
RUN ln -s /usr/lib/libfontconfig.so.1 /usr/lib/libfontconfig.so
RUN ln -s /lib/libuuid.so.1 /usr/lib/libuuid.so.1
RUN ln -s /lib/libc.musl-x86_64.so.1 /usr/lib/libc.musl-x86_64.so.1
ENV LD_LIBRARY_PATH /usr/lib

# Entrypoint of JMusicBot
WORKDIR /JMusicBot
CMD [ "/usr/bin/java", "-Dnogui=true", "-Dconfig.override_with_env_vars=true", "-jar", "/JMusicBot/JMusicBot.jar" ]
