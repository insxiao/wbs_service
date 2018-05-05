ARG VERSION=1.0-SNAPSHOT
ARG NAME=services

FROM ccr.ccs.tencentyun.com/scala/sbt_build:1.0.4 AS builder
ARG VERSION
ARG NAME

ADD . src

RUN cd src && \
    /sbt/bin/sbt universal:packageZipTarball && \
    mv target/universal/$NAME-$VERSION.tgz /root/$NAME-$VERSION.tgz


FROM openjdk:8-jre-alpine
ARG VERSION
ARG NAME

WORKDIR /root
EXPOSE 9000

RUN apk update && apk add bash

COPY --from=builder /root/services-$VERSION.tgz /root/

RUN tar zxf /root/$NAME-$VERSION.tgz -C /root && \
    mv $NAME-$VERSION app

WORKDIR /root

CMD app/bin/services -Dplay.http.secret.key=qaz