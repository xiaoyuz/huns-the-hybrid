package com.huns.chain.transaction

import com.huns.chain.block.model.Block
import com.huns.chain.block.model.BlockBody
import com.huns.common.model.Transaction
import com.huns.common.TransactionHelper
import com.huns.common.exception.Errors
import com.huns.common.exception.KeyException
import com.huns.common.handleMessage
import com.huns.chain.core.*
import com.huns.chain.common.manager.BlockManager
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

private const val SCHEDULE_PACK = 30 * 1000L // 30s

class TransactionVerticle : CoroutineVerticle() {

    private val transactionPool = mutableListOf<Transaction>()
    private val transactionPacking = mutableListOf<Transaction>()

    private val logger = LoggerFactory.getLogger(TransactionVerticle::class.java)

    private val blockManager: BlockManager by lazy { BlockManager(vertx) }

    override suspend fun start() {
        val bus = vertx.eventBus()
        bus.consumer(TRANSACTION_QUERY_BLOCK, this::queryTransactionBlock)
        bus.consumer(TRANSACTION_SUBMIT_POOL, this::submitPool)

        vertx.setTimer(randomPackScheduleTime(), this::startPack)
    }

    private fun startPack(timerId: Long) {
        if (transactionPool.isNotEmpty()) {
            launch {
                logger.info("Transaction pack start")
                transactionPacking.clear()
                transactionPool.forEach {
                    if (TransactionHelper.checkSign(it) && blockManager.transactionBlock(it.hash) == null) {
                        transactionPacking.add(it)
                    }
                }
                transactionPool.clear()
                if (transactionPacking.isNotEmpty()) {
                    val blockBody = BlockBody(transactionPacking)
                    vertx.eventBus().request<Block>(BLOCK_INSERT, blockBody).await()
                }
            }
        }
        vertx.setTimer(randomPackScheduleTime(), this::startPack)
    }

    private fun queryTransactionBlock(message: Message<String>) {
        handleMessage(message, TRANSACTION_CODE_ERROR) { mes ->
            val transactionHash = mes.body()
            blockManager.transactionBlock(transactionHash)?.let {
                mes.reply(it)
            } ?: throw KeyException(Errors.INVALID_PARAM_ERROR)
        }
    }

    private fun submitPool(message: Message<Transaction>) {
        handleMessage(message, TRANSACTION_CODE_ERROR) { mes ->
            logger.info("Transaction into pool: ${mes.body()}")
            if (transactionPool.any { it.hash == mes.body().hash }) throw KeyException(Errors.DUPLICATE_TRANSACTION_ERROR)
            blockManager.transactionBlock(mes.body().hash)?.let {
                throw KeyException(Errors.DUPLICATE_TRANSACTION_ERROR)
            }
            transactionPool.add(mes.body())
            mes.reply("")
        }
    }

    private fun randomPackScheduleTime() = (SCHEDULE_PACK..SCHEDULE_PACK * 3).random()
}