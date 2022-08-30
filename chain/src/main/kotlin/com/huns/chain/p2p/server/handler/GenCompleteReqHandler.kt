package com.huns.chain.p2p.server.handler

import com.huns.chain.common.manager.BlockManager
import com.huns.chain.p2p.broadcastP2PMessage
import com.huns.chain.p2p.message.*
import io.vertx.core.Vertx
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val SCHEDULE_CHECK_DELAY = 2 * 1000L // 2s

class GenCompleteReqHandler(vertx: Vertx) : BaseHandler<HashMessage>(vertx) {

    private val blockManager: BlockManager by lazy { BlockManager(vertx) }

    override fun dataClass() = HashMessage::class.java

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun handle(data: HashMessage, type: Byte, socket: NetSocket) {
        // Receive generated block complete from other nodes,
        // current node need to check if the block has been generated locally
        logger.info("Receive generate block complete request from ${data.common.nodeData}, data: $data")

        vertx.setTimer(SCHEDULE_CHECK_DELAY) {
            GlobalScope.launch(vertx.dispatcher()) {
                blockManager.block(data.hash)?.let { return@launch }
                logger.info("Try to get the new generated block from other nodes")
                // Get next block from other nodes
                val lastBlock = blockManager.lastBlock() ?: return@launch
                HashMessage(
                    hash = lastBlock.hash,
                    common = CommonInfo(
                        responseId = data.common.requestId
                    )
                ).let { broadcastP2PMessage(vertx, NEXT_BLOCK_REQ, it) }
            }
        }
    }
}