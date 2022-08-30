package com.huns.chain.pbft.manager

import com.huns.chain.block.model.Block
import com.huns.chain.pbft.model.VOTE_PREFARE
import com.huns.chain.pbft.model.VoteData
import io.vertx.core.Vertx

class PrePrepareManager(
    var vertx: Vertx,
    var managerHub: ManagerHub? = null
) : BaseVoteManager(vertx, managerHub) {

    private val msgMap = mutableMapOf<String, VoteData>()

    override suspend fun push(voteData: VoteData) {
        logger.info("Into preprepare, voteMsg: $voteData")
        val hash = voteData.hash
        if (msgMap.containsKey(hash)) return
        if (managerHub!!.prepareConfirmed(hash, voteData.number)) {
            logger.info("Reject the block to prepare status, hash: $hash")
            return
        }
        // TODO: Check the script in sqlite

        msgMap[hash] = voteData
        voteData.voteType = VOTE_PREFARE
        broadcastVote(voteData)
    }

    override fun hasConfirmed(hash: String, number: Int) = true

    override fun afterAllDone(block: Block) {
        val number = block.blockHeader.number
        vertx.setTimer(SCHEDULE_CLEAR_DELAY) {
            logger.info("Cleaning prePrepare map")
            val keysNeedRemove = msgMap.filter { it.value.number <= number }.map { it.key }
            keysNeedRemove.forEach { msgMap.remove(it) }
        }
    }

    fun getBlockByHash(hash: String) = msgMap[hash]?.block
}