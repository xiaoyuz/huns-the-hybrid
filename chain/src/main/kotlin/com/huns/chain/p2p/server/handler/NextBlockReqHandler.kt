package com.huns.chain.p2p.server.handler

import com.huns.chain.block.model.Block
import com.huns.chain.common.manager.BlockManager
import com.huns.chain.core.P2P_SEND
import com.huns.chain.p2p.message.*
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.coroutines.await

class NextBlockReqHandler(vertx: Vertx) : BaseHandler<HashMessage>(vertx) {

    private val blockManager: BlockManager by lazy { BlockManager(vertx) }

    override fun dataClass() = HashMessage::class.java

    override suspend fun handle(data: HashMessage, type: Byte, socket: NetSocket) {
        logger.info("Revieved the request from ${data.common.nodeData}, Block hash: ${data.hash}")
        val hash = data.hash

        val nextBlock = if (hash.isEmpty()) {
            blockManager.firstBlock()
        } else {
            blockManager.nextBlock(hash)
        } ?: Block()
        val nextHash = nextBlock.hash
        val nextBlockMessage = NextBlockMessage(
            common = CommonInfo(
                responseId = data.common.requestId
            ),
            hash = nextHash,
            prevHash = hash
        )
        val p2pMessage = P2PMessage(
            type = NEXT_BLOCK_RESP,
            data = Json.encode(nextBlockMessage)
        )
        vertx.eventBus().request<String>(P2P_SEND, data.common.nodeData to p2pMessage).await()
        logger.info("Sent the response to ${data.common.nodeData}, p2pMessage: $p2pMessage")
    }
}