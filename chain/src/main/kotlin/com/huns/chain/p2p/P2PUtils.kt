package com.huns.chain.p2p

import com.huns.chain.core.P2P_BROADCAST
import com.huns.chain.p2p.message.P2PMessage
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.kotlin.coroutines.await

suspend fun broadcastP2PMessage(vertx: Vertx, type: Byte, body: Any) = P2PMessage(
    type = type,
    data = Json.encode(body)
).let { vertx.eventBus().request<String>(P2P_BROADCAST, it).await() }