FROM debian:buster-slim
ENV LANG de_DE.UTF-8
ENV OPTS ""
RUN mkdir -p /usr/share/man/man1 && \
    apt-get update && \
    apt-get install -y --no-install-recommends procps curl locales locales-all openjdk-11-jdk-headless && \
    mkdir /usr/local/yogi && \
    mkdir /usr/local/yogi/run

COPY maven/*-springboot.jar /usr/local/yogi/yogi.jar

CMD cd /usr/local/yogi/run && java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 $OPTS -jar /usr/local/yogi/yogi.jar
