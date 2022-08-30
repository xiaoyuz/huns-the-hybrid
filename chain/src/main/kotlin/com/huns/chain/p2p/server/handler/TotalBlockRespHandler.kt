package com.huns.chain.p2p.server.handler

import com.huns.chain.p2p.message.BlockMessage
import io.vertx.core.Vertx
import io.vertx.core.net.NetSocket

class TotalBlockRespHandler(vertx: Vertx) : BaseHandler<BlockMessage>(vertx) {

    override fun dataClass() = BlockMessage::class.java

    override suspend fun handle(data: BlockMessage, type: Byte, socket: NetSocket) {

    }
}