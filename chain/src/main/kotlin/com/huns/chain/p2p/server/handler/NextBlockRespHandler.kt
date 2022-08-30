package com.huns.chain.p2p.server.handler

import com.huns.chain.core.PBFT_PUSH_CACHE
import com.huns.chain.pbft.model.HashData
import com.huns.chain.p2p.message.NextBlockMessage
import io.vertx.core.Vertx
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.coroutines.await

class NextBlockRespHandler(vertx: Vertx) : BaseHandler<NextBlockMessage>(vertx) {

    override fun dataClass() = NextBlockMessage::class.java

    override suspend fun handle(data: NextBlockMessage, type: Byte, socket: NetSocket) {
        val hash = data.hash
        logger.info("Revieved the reply from ${data}, next block hash: $hash")
        if (hash.isEmpty()) {
            logger.info("We have the newest latest block")
            return
        }
        val hashData = HashData(
            hash = hash,
            preHash = data.prevHash,
            appId = data.common.nodeData.appId
        )
        vertx.eventBus().request<String>(PBFT_PUSH_CACHE, hashData).await()
    }
}