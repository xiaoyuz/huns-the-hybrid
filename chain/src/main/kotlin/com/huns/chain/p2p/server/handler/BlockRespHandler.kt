package com.huns.chain.p2p.server.handler

import com.huns.chain.core.BLOCK_GENERATE
import com.huns.chain.core.PBFT_POP_CACHE
import com.huns.chain.common.manager.BlockManager
import com.huns.chain.p2p.broadcastP2PMessage
import com.huns.chain.p2p.message.*
import io.vertx.core.Vertx
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.coroutines.await

class BlockRespHandler(vertx: Vertx) : BaseHandler<BlockMessage>(vertx) {

    private val blockManager: BlockManager by lazy { BlockManager(vertx) }

    override fun dataClass() = BlockMessage::class.java

    override suspend fun handle(data: BlockMessage, type: Byte, socket: NetSocket) {
        logger.info("Revieved the reply from ${data.common.nodeData}, Block: ${data.block}")
        val block = data.block
        if (block.hash.isEmpty()) {
            logger.info("No block")
            return
        }
        vertx.eventBus().request<String>(PBFT_POP_CACHE, block.hash).await().body().let { if (it.isEmpty()) return }
        try {
            blockManager.check(block)
        } catch (e: Exception) {
            logger.info("Block ${block} is in processing")
            return
        }
        vertx.eventBus().publish(BLOCK_GENERATE, block)

        // Request for next block
        val lastBlock = blockManager.lastBlock() ?: return
        broadcastP2PMessage(vertx, NEXT_BLOCK_REQ, HashMessage(hash = lastBlock.hash))
    }
}