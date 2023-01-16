FROM ubuntu:latest AS builder

ADD . /JMusicBot/
WORKDIR /JMusicBot

# Install dependencies
RUN apt-get update
RUN apt-get install -y openjdk-17-jdk maven

# Build JMusicBot
RUN mvn clean
RUN mvn compile
RUN mvn test-compile
RUN mvn test
RUN mvn install

# Run JMusicBot
WORKDIR /JMusicBot/target
CMD [ "/usr/bin/java", "-jar", "./JMusicBot-Snapshot-All.jar" ]