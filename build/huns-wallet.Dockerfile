FROM openjdk:11
MAINTAINER xiaoyuz

ENV WORKDIR "/app"
WORKDIR $WORKDIR

EXPOSE 8086

ADD ./wallet/target/wallet-1.0.0-SNAPSHOT-fat.jar $WORKDIR
ADD ./build/wallet-docker-config.json $WORKDIR/config.json

ENTRYPOINT ["/bin/bash", "-c", "java -jar $WORKDIR/wallet-1.0.0-SNAPSHOT-fat.jar -conf $WORKDIR/config.json"]