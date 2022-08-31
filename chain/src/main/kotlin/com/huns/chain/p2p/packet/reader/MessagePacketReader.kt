package com.huns.chain.p2p.packet.reader

import com.huns.chain.p2p.message.P2PMessage
import io.vertx.core.json.Json
import org.apache.commons.codec.binary.Base64

class MessagePacketReader(
    successor: PacketReader? = null
) : PacketReader(successor) {

    override fun process(data: String): P2PMessage? {
        val messageStr = String(Base64.decodeBase64(data))
        return Json.decodeValue(messageStr, P2PMessage::class.java)
    }
}