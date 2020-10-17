FROM debian:buster-slim

LABEL net.oneandone.stool-port.http=8080

ENV LANG de_DE.UTF-8
RUN mkdir -p /usr/share/man/man1 && \
    apt-get update && \
    apt-get install -y --no-install-recommends procps curl openjdk-11-jdk-headless && \
    mkdir /usr/local/yogi && \
    mkdir /usr/local/yogi/run

COPY maven/*.jar /usr/local/yogi/yogi.jar

CMD cd /usr/local/yogi/run && java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 $OPTS -jar /usr/local/yogi/yogi.jar
