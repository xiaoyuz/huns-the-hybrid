package com.huns.chain.p2p.server.handler

import com.huns.chain.EnvConfig
import com.huns.chain.common.manager.BlockManager
import com.huns.chain.core.PBFT_PUSH_VOTE
import com.huns.chain.p2p.message.BlockMessage
import com.huns.chain.pbft.model.VOTE_PREPREPARE
import com.huns.chain.pbft.model.VoteData
import io.vertx.core.Vertx
import io.vertx.core.net.NetSocket

class GenBlockReqHandler(vertx: Vertx) : BaseHandler<BlockMessage>(vertx) {

    private val blockManager: BlockManager by lazy { BlockManager(vertx) }

    override fun dataClass() = BlockMessage::class.java

    override suspend fun handle(data: BlockMessage, type: Byte, socket: NetSocket) {
        val block = data.block
        logger.info("Receive generate block request from ${data.common.nodeData}, block: $block")

        blockManager.check(block)
        val voteData = VoteData(
            block = block,
            voteType = VOTE_PREPREPARE,
            number = block.blockHeader.number,
            appId = EnvConfig.nodeAppId,
            hash = block.hash,
            agree = true
        )
        vertx.eventBus().publish(PBFT_PUSH_VOTE, voteData)
    }
}