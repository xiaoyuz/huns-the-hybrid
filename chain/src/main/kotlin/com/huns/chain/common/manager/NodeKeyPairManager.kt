package com.huns.chain.common.manager

import com.huns.chain.common.KEY_NODE_KEYPAIR
import com.huns.chain.common.genPairKey
import com.huns.chain.core.STORAGE_GET
import com.huns.chain.core.STORAGE_PUT
import com.huns.chain.storage.DBKV
import com.huns.common.KeyPair
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.kotlin.coroutines.await

class NodeKeyPairManager(private val vertx: Vertx) {

    suspend fun nodeKeyPair() = vertx.eventBus().request<String?>(STORAGE_GET, KEY_NODE_KEYPAIR).await().body()?.let {
        Json.decodeValue(it, KeyPair::class.java)
    } ?: genPairKey().let {
        vertx.eventBus().request<Unit>(STORAGE_PUT, DBKV(key = KEY_NODE_KEYPAIR, value = Json.encode(it))).await()
        it
    }
}