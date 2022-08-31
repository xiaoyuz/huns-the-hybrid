package com.huns.chain.p2p.server.handler

import com.huns.chain.block.model.Block
import com.huns.chain.common.manager.BlockManager
import com.huns.chain.core.P2P_SEND
import com.huns.chain.p2p.message.*
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.coroutines.await

class BlockReqHandler(vertx: Vertx) : BaseHandler<HashMessage>(vertx) {

    private val blockManager: BlockManager by lazy { BlockManager(vertx) }

    override fun dataClass() = HashMessage::class.java

    override suspend fun handle(data: HashMessage, type: Byte, socket: NetSocket) {
        logger.info("Receive the request from ${data.common.nodeData}, Block hash: ${data.hash}")
        val block = blockManager.block(data.hash) ?: Block()
        val blockMessage = BlockMessage(
            common = CommonInfo(
                responseId = data.common.requestId
            ),
            block = block
        )
        val p2pMessage = P2PMessage(
            type = BLOCK_RESP,
            data = Json.encode(blockMessage)
        )
        vertx.eventBus().request<String>(P2P_SEND, data.common.nodeData to p2pMessage).await()
        logger.info("Sent the response to ${data.common.nodeData}, Block: $block")
    }
}