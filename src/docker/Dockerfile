FROM debian:bullseye-slim

RUN mkdir -p /usr/share/man/man1 && \
    apt-get update && \
    apt-get install -y --no-install-recommends procps curl openjdk-13-jdk-headless && \
    mkdir /usr/local/springboot

COPY *.jar /usr/local/springboot

CMD java -jar /usr/local/springboot/*.jar
