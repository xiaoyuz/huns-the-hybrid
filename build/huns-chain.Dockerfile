FROM openjdk:11
MAINTAINER xiaoyuz

ENV WORKDIR "/app"
WORKDIR $WORKDIR

ENV NODE_NAME "node0"
ENV NODE_APP_ID "node0app"
ENV MANAGER_HOST "127.0.0.1"

EXPOSE 38080
EXPOSE 5000

ADD ./chain/target/chain-1.0.0-SNAPSHOT-fat.jar $WORKDIR
ADD ./build/chain-docker-config.json $WORKDIR/config.json

ENTRYPOINT ["/bin/bash", "-c", "java -jar $WORKDIR/chain-1.0.0-SNAPSHOT-fat.jar -conf $WORKDIR/config.json"]