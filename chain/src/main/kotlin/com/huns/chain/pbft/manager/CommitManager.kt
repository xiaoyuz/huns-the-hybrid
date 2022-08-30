package com.huns.chain.pbft.manager

import com.huns.chain.block.model.Block
import com.huns.chain.core.BLOCK_GENERATE
import com.huns.chain.pbft.PbftConfig
import com.huns.chain.pbft.model.VoteData
import io.vertx.core.Vertx

class CommitManager(
    var vertx: Vertx,
    var managerHub: ManagerHub? = null
) : BaseVoteManager(vertx, managerHub) {

    private val voteDataMap = mutableMapOf<String, MutableList<VoteData>>()
    private val voteStateMap = mutableMapOf<String, Boolean>()

    override suspend fun push(voteData: VoteData) {
        val hash = voteData.hash
        if (!voteDataMap.containsKey(hash)) {
            voteDataMap[hash] = mutableListOf()
        }
        val voteMsgs = voteDataMap[hash]!!
        if (voteMsgs.any { it.appId == voteData.appId }) return
        voteMsgs.add(voteData)
        if (voteStateMap.containsKey(hash)) return

        val count = voteMsgs.count { it.agree }
        logger.info("Commit true count: $count")
        if (count >= PbftConfig.pbftApproveCount) {
            voteData.block?.let {
                voteStateMap[hash] = true
                vertx.eventBus().publish(BLOCK_GENERATE, it)
            }
        }
    }

    override fun hasConfirmed(hash: String, number: Int) = run loop@ {
        voteDataMap.forEach { (k, v) ->
            if (k != hash && v.first().number >= number && voteStateMap[k] == true) return@loop true
        }
        return false
    }

    override fun afterAllDone(block: Block) {
        val number = block.blockHeader.number
        vertx.setTimer(SCHEDULE_CLEAR_DELAY) {
            logger.info("Cleaning commit map")
            val keysNeedRemove = voteDataMap.filter { (it.value.firstOrNull()?.number ?: 0) <= number }.map { it.key }
            keysNeedRemove.forEach {
                voteDataMap.remove(it)
                voteStateMap.remove(it)
            }
        }
    }
}