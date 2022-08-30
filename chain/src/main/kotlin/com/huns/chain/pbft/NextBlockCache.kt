package com.huns.chain.pbft

import com.huns.chain.common.manager.BlockManager
import com.huns.chain.pbft.model.HashData
import com.huns.chain.p2p.broadcastP2PMessage
import com.huns.chain.p2p.message.*
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory

class NextBlockCache(
    val vertx: Vertx
) {
    private val logger = LoggerFactory.getLogger(NextBlockCache::class.java)

    private val blockManager: BlockManager by lazy { BlockManager(vertx) }
    private val requestMap = mutableMapOf<String, MutableSet<HashData>>()
    private val wantHashs = mutableSetOf<String>()

    fun pop(hash: String) = if (wantHashs.remove(hash)) hash else null

    suspend fun push(hashData: HashData) {
        val hash = hashData.hash
        val prevHash = hashData.preHash
        blockManager.block(hash)?.let {
            requestMap.remove(prevHash)
            return
        }
        add(prevHash, hashData)
        val maxCount = findMaxHash(prevHash).size
        if (PbftConfig.pbftApproveCount - 1 <= maxCount) {
            if (!wantHashs.contains(hash)) {
                logger.info("There are $maxCount node response next block hash: $hash")
                wantHashs.add(hash)
                broadcastP2PMessage(vertx, BLOCK_REQ, HashMessage(hash = hash))
                requestMap.remove(prevHash)
            }
        }
    }

    private fun add(key: String, hashData: HashData) {
        val messages = requestMap[key] ?: mutableSetOf()
        if (messages.any { it.appId == hashData.appId }) return
        messages.add(hashData)
        requestMap[key] = messages
    }

    private fun findMaxHash(key: String): List<HashData> {
        val messages = requestMap[key] ?: return emptyList()
        val map = mutableMapOf<String, Int>()
        messages.forEach {
            map.merge(it.hash, 1) { t, u -> t + u }
        }
        var value = 0
        var hash: String? = null
        map.forEach { (k, v) ->
            if (value < v) {
                value = v
                hash = k
            }
        }
        return messages.filter { it.hash == hash }
    }
}