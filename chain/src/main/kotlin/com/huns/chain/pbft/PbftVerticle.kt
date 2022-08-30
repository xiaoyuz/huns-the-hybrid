package com.huns.chain.pbft

import com.huns.chain.block.model.Block
import com.huns.common.handleMessage
import com.huns.chain.core.*
import com.huns.chain.pbft.model.HashData
import com.huns.chain.pbft.manager.*
import com.huns.chain.pbft.model.VOTE_COMMIT
import com.huns.chain.pbft.model.VOTE_PREFARE
import com.huns.chain.pbft.model.VOTE_PREPREPARE
import com.huns.chain.pbft.model.VoteData
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle

class PbftVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(PbftVerticle::class.java)

    private val managerMap = mutableMapOf<Byte, BaseVoteManager>()
    private val nextBlockCache: NextBlockCache by lazy { NextBlockCache(vertx) }

    override suspend fun start() {
        val prePrepareManager = PrePrepareManager(vertx)
        val prepareManager = PrepareManager(vertx)
        val commitManager = CommitManager(vertx)
        val hub = ManagerHub(prePrepareManager, prepareManager, commitManager)
        prePrepareManager.managerHub = hub
        prepareManager.managerHub = hub
        commitManager.managerHub = hub

        managerMap[VOTE_PREPREPARE] = prePrepareManager
        managerMap[VOTE_PREFARE] = prepareManager
        managerMap[VOTE_COMMIT] = commitManager

        val bus = vertx.eventBus()
        bus.consumer(PBFT_PUSH_VOTE, this::pushVote)
        bus.consumer(PBFT_POP_CACHE, this::popCache)
        bus.consumer(PBFT_PUSH_CACHE, this::pushCache)
        bus.consumer(BLOCK_GENERATE, this::blockGenerated)
    }

    private fun pushVote(message: Message<VoteData>) {
        handleMessage(message, PBFT_CODE_ERROR) { mes ->
            val voteMsg = mes.body()
            val manager = managerMap[voteMsg.voteType]
            manager?.push(voteMsg)
            mes.reply("")
        }
    }

    private fun popCache(message: Message<String>) {
        message.reply(nextBlockCache.pop(message.body()) ?: "")
    }

    private fun pushCache(message: Message<HashData>) {
        handleMessage(message, PBFT_CODE_ERROR) { mes ->
            nextBlockCache.push(message.body())
            mes.reply("")
        }
    }

    private fun blockGenerated(message: Message<Block>) {
        handleMessage(message, PBFT_CODE_ERROR) { mes ->
            managerMap.values.forEach {
                it.afterAllDone(mes.body())
            }
        }
    }
}