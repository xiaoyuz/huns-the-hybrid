package com.huns.chain.pbft.manager

import com.huns.chain.block.model.Block
import com.huns.chain.p2p.broadcastP2PMessage
import com.huns.chain.p2p.message.PBFT_VOTE
import com.huns.chain.p2p.message.VoteMessage
import com.huns.chain.pbft.model.VoteData
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory

const val SCHEDULE_CLEAR_DELAY = 2 * 1000L // 2s

abstract class BaseVoteManager(
    private val vertx: Vertx,
    private val managerHub: ManagerHub?
) {

    protected val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun broadcastVote(voteData: VoteData) {
        val newVoteMsg = voteData.copy(
            appId = com.huns.chain.EnvConfig.nodeAppId
        )
        broadcastP2PMessage(vertx, PBFT_VOTE, VoteMessage(voteData = newVoteMsg))
    }

    suspend fun pushVote(voteData: VoteData) {
        managerHub ?: return
        push(voteData)
    }

    abstract suspend fun push(voteData: VoteData)

    abstract fun hasConfirmed(hash: String, number: Int): Boolean

    abstract fun afterAllDone(block: Block)
}