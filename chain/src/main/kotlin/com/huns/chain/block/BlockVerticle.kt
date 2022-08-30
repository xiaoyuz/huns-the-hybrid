package com.huns.chain.block

import com.huns.chain.block.model.Block
import com.huns.chain.block.model.BlockBody
import com.huns.chain.block.model.BlockHeader
import com.huns.common.crypto.SHA256
import com.huns.common.crypto.toHex
import com.huns.common.exception.Errors
import com.huns.common.exception.KeyException
import com.huns.common.handleMessage
import com.huns.chain.common.manager.BlockManager
import com.huns.chain.common.merkle.MerkleTree
import com.huns.chain.core.*
import com.huns.chain.p2p.broadcastP2PMessage
import com.huns.chain.p2p.message.*
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

private const val VERSION = 1
private const val SCHEDULE_FETCH_BLOCKS_START = 10 * 1000L // 10s
private const val SCHEDULE_FETCH_BLOCKS = 1 * 60 * 1000L // 1 min

class BlockVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(BlockVerticle::class.java)

    private val blockManager: BlockManager by lazy { BlockManager(vertx) }

    override suspend fun start() {
        val bus = vertx.eventBus()
        bus.consumer(BLOCK_INSERT, this::insertBlock)
        bus.consumer(BLOCK_GENERATE, this::generateBlock)
        bus.consumer(BLOCK_LAST_BLOCK, this::lastBlock)
        bus.consumer(BLOCK_QUERY_BLOCK, this::queryBlock)
        bus.consumer(BLOCK_QUERY_NEXT_BLOCK, this::queryNextBlock)

        vertx.setTimer(SCHEDULE_FETCH_BLOCKS_START, this::fetchBlocks)
    }

    private fun fetchBlocks(timerId: Long) {
        launch {
            val lastBlockHash = blockManager.lastBlock()?.hash ?: ""
            logger.info("Try to get the newest block from other nodes, local last hash $lastBlockHash")
            // Get next block from other nodes
            broadcastP2PMessage(vertx, NEXT_BLOCK_REQ, HashMessage(hash = lastBlockHash))
        }
        vertx.setTimer(SCHEDULE_FETCH_BLOCKS, this::fetchBlocks)
    }

    private fun lastBlock(message: Message<String>) {
        handleMessage(message, BLOCK_CODE_ERROR) { mes ->
            blockManager.lastBlock()?.let {
                mes.reply(it)
            } ?: throw KeyException(Errors.INVALID_PARAM_ERROR)
        }
    }

    private fun queryBlock(message: Message<String>) {
        handleMessage(message, BLOCK_CODE_ERROR) { mes ->
            blockManager.block(mes.body())?.let {
                mes.reply(it)
            } ?: throw KeyException(Errors.INVALID_PARAM_ERROR)
        }
    }

    private fun queryNextBlock(message: Message<String>) {
        handleMessage(message, BLOCK_CODE_ERROR) { mes ->
            blockManager.nextBlock(mes.body())?.let {
                mes.reply(it)
            } ?: throw KeyException(Errors.INVALID_PARAM_ERROR)
        }
    }

    private fun insertBlock(message: Message<BlockBody>) {
        handleMessage(message, BLOCK_CODE_ERROR) { mes ->
            val blockBody = mes.body()
            check(blockBody)
            mes.reply(addBlock(blockBody))
        }
    }

    private suspend fun check(blockBody: BlockBody) {
        blockManager.checkBlockBody(blockBody)
    }

    private suspend fun addBlock(blockBody: BlockBody): Block {
        val transactions = blockBody.transactions
        val hashList = transactions.map { it.hash }

        val lastBlockNumber = blockManager.lastBlock()?.blockHeader?.number ?: 0

        val merkelTree = MerkleTree(hashList)
        merkelTree.build()

        val blockHeader = BlockHeader(
            merkleRootHash = merkelTree.root(),
            timeStamp = System.currentTimeMillis(),
            version = VERSION,
            number = lastBlockNumber + 1,
            previousBlockHash = blockManager.lastBlock()?.hash ?: "",
            nonce = lastBlockNumber.toLong()
        )

        val block = Block(
            blockHeader = blockHeader,
            blockBody = blockBody,
            hash = SHA256.sha256(blockHeader.toString() + blockBody.toString()).toHex()
        )

        // Block broadcast to send via P2P
        val body =  BlockMessage(block = block)
        val p2pMessage = P2PMessage(
            type = GEN_BLOCK_REQ,
            data = Json.encode(body)
        )
        vertx.eventBus().request<String>(P2P_BROADCAST_INCLUDE_SELF, p2pMessage).await()

        return block
    }

    private fun generateBlock(message: Message<Block>) {
        handleMessage(message, BLOCK_CODE_ERROR) { mes ->
            logger.info("Start generate block")
            val block = mes.body()
            val hash = block.hash
            blockManager.block(hash)?.let { return@handleMessage }
            blockManager.check(block)
            if (block.blockHeader.previousBlockHash.isEmpty()) {
                blockManager.putFirstBlock(hash)
            } else {
                blockManager.putNextBlock(block.blockHeader.previousBlockHash, hash)
            }
            blockManager.putBlock(hash, block)
            blockManager.putLastBlock(hash)
            logger.info("New block generated locally")

            block.blockBody.transactions.forEach {
                blockManager.putTransactionBlock(it.hash, block.hash)
            }
            logger.info("Transactions mapped locally")
            // TODO: Sync to sqlite
            sqliteSync()

            broadcastP2PMessage(vertx, GEN_BLOCK_COMPLETE_REQ, HashMessage(hash = hash))
            mes.reply("")
        }
    }

    fun sqliteSync() {

    }
}