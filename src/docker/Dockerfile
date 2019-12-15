FROM debian:bullseye-slim

ENV LANG de_DE.UTF-8
RUN mkdir -p /usr/share/man/man1 && \
    apt-get update && \
    apt-get install -y --no-install-recommends procps curl openjdk-13-jdk-headless && \
    mkdir /usr/local/yogi

COPY maven/*.jar /usr/local/yogi/yogi.jar

CMD cd /usr/local/yogi/run && pwd && ls -la && java $OPTS -jar /usr/local/yogi/yogi.jar
