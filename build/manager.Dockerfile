FROM openjdk:11
MAINTAINER xiaoyuz

ENV WORKDIR "/app"
ENV MYSQL_HOST "127.0.0.1"
WORKDIR $WORKDIR

EXPOSE 8085

ADD ./blockmanager/target/blockmanager-1.0.0-SNAPSHOT-fat.jar $WORKDIR
ADD ./build/manager-docker-config.json $WORKDIR/config.json

ENTRYPOINT ["/bin/bash", "-c", "java -jar $WORKDIR/blockmanager-1.0.0-SNAPSHOT-fat.jar -conf $WORKDIR/config.json"]