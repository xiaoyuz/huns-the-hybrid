FROM openjdk:11
MAINTAINER xiaoyuz

ENV WORKDIR "/app"
WORKDIR $WORKDIR

ENV NODE_NAME "node0"
ENV NODE_APP_ID "node0app"
ENV MANAGER_HOST "127.0.0.1"
ENV NODE_PUBLIC_KEY "BLg2VxyZJj2tlfqSadTC1X1R2P31wc750WsWrTBDyuEjH1Tofm/PNruxdLBIG80SJOeUMM3rqRn5IN0g3MwTDYw="
ENV NODE_PRIVATE_KEY "mssvlEy8zey1FDnWicIacrZ/gdjGWU/1a5Vd2saKjI4="

EXPOSE 38080
EXPOSE 5000

ADD ./chain/target/chain-1.0.0-SNAPSHOT-fat.jar $WORKDIR
ADD ./build/chain-docker-config.json $WORKDIR/config.json

ENTRYPOINT ["/bin/bash", "-c", "java -jar $WORKDIR/chain-1.0.0-SNAPSHOT-fat.jar -conf $WORKDIR/config.json"]