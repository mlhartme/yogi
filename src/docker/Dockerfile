FROM debian:sid-slim
ENV LANG de_DE.UTF-8
ENV OPTS ""
RUN mkdir -p /usr/share/man/man1 && \
    apt-get update && \
    apt-get install -y --no-install-recommends procps curl locales locales-all openjdk-16-jdk-headless && \
    mkdir /usr/local/yogi && \
    mkdir /usr/local/yogi/run

COPY maven/*-springboot.jar /usr/local/yogi/yogi.jar

RUN export RUN=/usr/local/yogi/yogi.sh && \
    echo "#!/bin/sh" >$RUN && \
    echo "exec java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dyogi.etc=/usr/local/yogi/etc -Dyogi.lib=/usr/local/yogi/lib $OPTS -jar /usr/local/yogi/yogi.jar" >>$RUN && \
    chmod a+x $RUN
ENTRYPOINT ["/usr/local/yogi/yogi.sh"]
