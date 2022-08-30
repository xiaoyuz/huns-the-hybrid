package com.huns.chain.pbft.manager

import com.huns.chain.block.model.Block
import com.huns.chain.pbft.PbftConfig
import com.huns.chain.pbft.model.VOTE_COMMIT
import com.huns.chain.pbft.model.VoteData
import io.vertx.core.Vertx

class PrepareManager(
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

        if (managerHub!!.commitConfirmed(hash, voteData.number)) {
            sendApprove(voteData, false)
        } else {
            val approveCount = voteMsgs.count { it.agree }
            val notApproveCount = voteMsgs.size - approveCount
            if (approveCount >= PbftConfig.pbftSize) {
                sendApprove(voteData, true)
            } else if (notApproveCount >= PbftConfig.pbftSize + 1) {
                sendApprove(voteData, false)
            }
        }
    }

    override fun hasConfirmed(hash: String, number: Int): Boolean {
        if (managerHub!!.commitConfirmed(hash, number)) return true
        return run loop@ {
            voteDataMap.forEach { (k, v) ->
                if (k != hash && v.first().number >= number && voteStateMap[k] == true) return@loop true
            }
            return false
        }
    }

    override fun afterAllDone(block: Block) {
        val number = block.blockHeader.number
        vertx.setTimer(SCHEDULE_CLEAR_DELAY) {
            logger.info("Cleaning prepare map")
            val keysNeedRemove = voteDataMap.filter { (it.value.firstOrNull()?.number ?: 0) <= number }.map { it.key }
            keysNeedRemove.forEach {
                voteDataMap.remove(it)
                voteStateMap.remove(it)
            }
        }
    }

    private suspend fun sendApprove(voteData: VoteData, agree: Boolean) {
        logger.info("Prepare complete, approve: $agree")
        voteData.agree = agree
        voteStateMap[voteData.hash] = agree
        voteData.voteType = VOTE_COMMIT
        broadcastVote(voteData)
    }
}