package com.huns.chain.common.manager

import com.huns.chain.block.model.Block
import com.huns.chain.block.model.BlockBody
import com.huns.chain.common.*
import com.huns.common.exception.Errors
import com.huns.common.exception.KeyException
import com.huns.chain.common.merkle.MerkleTree
import com.huns.chain.storage.DBKV
import com.huns.chain.core.FETCHER_CHECK_BLOCK_PERMISSION
import com.huns.chain.core.FETCHER_CHECK_TRANSACTIONS_PERMISSION
import com.huns.chain.core.STORAGE_GET
import com.huns.chain.core.STORAGE_PUT
import com.huns.common.TransactionHelper
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.kotlin.coroutines.await

class BlockManager(private val vertx: Vertx) {

    suspend fun transactionBlock(transactionHash: String) =
        vertx.eventBus().request<String?>(STORAGE_GET, "$KEY_TRANSACTION_BLOCK_PREFIX$transactionHash")
            .await().body()?.let { blockHash ->
                block(blockHash)
            }

    suspend fun firstBlock() =
        vertx.eventBus().request<String?>(STORAGE_GET, KEY_FIRST_BLOCK).await().body()?.let { firstBlockHash ->
            block(firstBlockHash)
        }

    suspend fun lastBlock() =
        vertx.eventBus().request<String?>(STORAGE_GET, KEY_LAST_BLOCK).await().body()?.let { firstBlockHash ->
            block(firstBlockHash)
        }

    suspend fun nextBlock(hash: String) =
        vertx.eventBus().request<String?>(STORAGE_GET, "$KEY_BLOCK_NEXT_PREFIX${hash}")
            .await().body()?.let { nextBlockHash ->
                block(nextBlockHash)
            }

    suspend fun block(hash: String) =
        vertx.eventBus().request<String?>(STORAGE_GET, "$KEY_BLOCK_PREFIX$hash")
            .await().body()?.let { Json.decodeValue(it, Block::class.java) }

    suspend fun putTransactionBlock(transactionHash: String, blockHash: String) =
        vertx.eventBus().request<Unit>(STORAGE_PUT, DBKV(key = "$KEY_TRANSACTION_BLOCK_PREFIX$transactionHash", blockHash)).await()

    suspend fun putBlock(hash: String, block: Block) =
        vertx.eventBus().request<Unit>(STORAGE_PUT, DBKV(key = "$KEY_BLOCK_PREFIX$hash", value = Json.encode(block))).await()

    suspend fun putFirstBlock(hash: String) =
        vertx.eventBus().request<Unit>(STORAGE_PUT, DBKV(key = KEY_FIRST_BLOCK, value = hash)).await()

    suspend fun putLastBlock(hash: String) =
        vertx.eventBus().request<Unit>(STORAGE_PUT, DBKV(key = KEY_LAST_BLOCK, value = hash)).await()

    suspend fun putNextBlock(previousHash: String, hash: String) =
        vertx.eventBus().request<Unit>(STORAGE_PUT, DBKV(key = "$KEY_BLOCK_NEXT_PREFIX$previousHash", value = hash)).await()

    suspend fun checkBlockBody(blockBody: BlockBody) {
        val transactions = blockBody.transactions
        if (transactions.isEmpty()) throw KeyException(Errors.EMPTY_TRANSACTIONS_ERROR)
        transactions.forEach {
            if (!TransactionHelper.checkSign(it)) throw KeyException(Errors.VERIFY_SIGN_ERROR)
            if (!TransactionHelper.checkHash(it)) throw KeyException(Errors.VERIFY_HASH_ERROR)
        }
        val check = vertx.eventBus().request<Boolean>(FETCHER_CHECK_TRANSACTIONS_PERMISSION, transactions).await().body()
        if (!check) throw KeyException(Errors.PERMISSION_CHECK_ERROR)
    }

    suspend fun check(block: Block) {
        checkDuplicateTransaction(block)
        checkMerkle(block)
        checkSign(block)
        checkNumber(block)
        checkTime(block)
        checkHash(block)
        checkPermission(block)
    }

    private suspend fun checkNumber(block: Block) {
        val localNum = lastBlock()?.blockHeader?.number ?: 0
        if (localNum + 1 != block.blockHeader.number) throw KeyException(Errors.BLOCK_NUM_MISMATCH_ERROR)
    }

    private suspend fun checkPermission(block: Block) {
        val check = vertx.eventBus().request<Boolean>(FETCHER_CHECK_BLOCK_PERMISSION, block).await().body()
        if (!check) throw KeyException(Errors.PERMISSION_CHECK_ERROR)
    }

    private suspend fun checkHash(block: Block) {
        val lastBlock = lastBlock()
        if (lastBlock == null && block.blockHeader.previousBlockHash.isEmpty()) return

        lastBlock?.let {
            if (it.hash != block.blockHeader.previousBlockHash) throw KeyException(Errors.VERIFY_HASH_ERROR)
        } ?: throw KeyException(Errors.VERIFY_HASH_ERROR)
    }

    private suspend fun checkTime(block: Block) {
        lastBlock()?.let { lastBlock ->
            if (block.blockHeader.timeStamp <= lastBlock.blockHeader.timeStamp)
                throw KeyException(Errors.BLOCK_TIME_MISMATCH_ERROR)
        }
    }

    private suspend fun checkSign(block: Block) {
        val blockBody = block.blockBody
        checkBlockBody(blockBody)
        val hash = block.calculateHash()
        if (hash != block.hash) throw KeyException(Errors.VERIFY_HASH_ERROR)
    }

    private fun checkMerkle(block: Block) {
        val hashes = block.blockBody.transactions.map { it.hash }
        val merkleRoot = block.blockHeader.merkleRootHash
        val merkleTree = MerkleTree(hashes)
        merkleTree.build()
        if (merkleRoot != merkleTree.root()) throw KeyException(Errors.VERIFY_MERKLE_ROOT_ERROR)
    }

    private suspend fun checkDuplicateTransaction(block: Block) {
        val transactions = block.blockBody.transactions
        val transactionHashes = transactions.map { it.hash }.toSet()
        if (transactions.size != transactionHashes.size) throw KeyException(Errors.DUPLICATE_TRANSACTION_ERROR)
        transactions.forEach {
            transactionBlock(it.hash)?.let { throw KeyException(Errors.DUPLICATE_TRANSACTION_ERROR) }
        }
    }

    private suspend fun checkAnyBlock(block: Block) {
        checkSign(block)
        val preHash = block.blockHeader.previousBlockHash
        val preBlock = block(preHash) ?: return
        val localNum = preBlock.blockHeader.number
        if (localNum + 1 != block.blockHeader.number) throw KeyException(Errors.BLOCK_NUM_MISMATCH_ERROR)
        if (block.blockHeader.timeStamp <= preBlock.blockHeader.timeStamp)
            throw KeyException(Errors.BLOCK_TIME_MISMATCH_ERROR)
    }
}