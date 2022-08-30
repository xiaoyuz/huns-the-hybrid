package com.huns.chain.p2p.server.handler

import com.huns.chain.core.P2P_PING
import com.huns.chain.p2p.message.*
import io.vertx.core.Vertx
import io.vertx.core.net.NetSocket

class PingRequestHandler(vertx: Vertx) : BaseHandler<PingMessage>(vertx) {

    override fun dataClass() = PingMessage::class.java

    override suspend fun handle(data: PingMessage, type: Byte, socket: NetSocket) {
        vertx.eventBus().request<String>(P2P_PING, data.nodeData)
    }
}