package com.huns.chain.p2p.server.handler

import com.huns.chain.core.PBFT_PUSH_VOTE
import com.huns.chain.p2p.message.VoteMessage
import io.vertx.core.Vertx
import io.vertx.core.net.NetSocket

class PbftVoteReqHandler(vertx: Vertx) : BaseHandler<VoteMessage>(vertx) {

    override fun dataClass() = VoteMessage::class.java

    override suspend fun handle(data: VoteMessage, type: Byte, socket: NetSocket) {
        val voteMsg = data.voteData
        logger.info("Receive voteMsg request from ${data.common.nodeData}, voteMsg: $voteMsg")
        vertx.eventBus().publish(PBFT_PUSH_VOTE, voteMsg)
    }
}