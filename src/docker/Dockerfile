FROM debian:bullseye-20220912-slim
ENV OPTS ""
RUN apt-get update && \
    apt-get install -y --no-install-recommends procps curl openjdk-17-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir /usr/local/yogi && \
    mkdir /usr/local/yogi/run
WORKDIR /usr/local/yogi/run
COPY maven/*.jar /usr/local/yogi/app.jar
ENV LANG=C.UTF-8
ENTRYPOINT ["java", "-jar", "/usr/local/yogi/app.jar" ]
