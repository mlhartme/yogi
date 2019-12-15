FROM debian:bullseye-slim

RUN mkdir -p /usr/share/man/man1 && \
    apt-get update && \
    apt-get install -y --no-install-recommends procps curl openjdk-13-jdk-headless && \
    mkdir /usr/local/springboot && \
    mkdir /var/log/springboot

COPY maven/*.jar /usr/local/springboot

CMD java $OPTS -jar /usr/local/springboot/*.jar
