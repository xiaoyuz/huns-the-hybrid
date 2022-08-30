package com.huns.chain.p2p.packet

import com.huns.chain.p2p.message.P2PMessage
import io.vertx.core.json.Json
import org.apache.commons.codec.binary.Base64

data class P2PPacket(
    var content: String = "",
    var withHead: Boolean = false,
    var withTail: Boolean = false
) {

    constructor(message: P2PMessage) : this(
        withHead = true,
        withTail = true,
    ) {
        val json = Json.encode(message)
        val base64Str = Base64.encodeBase64String(json.toByteArray())
        content = "<$base64Str>"
    }

    fun genP2PMessage(): P2PMessage {
        val messageStr = String(Base64.decodeBase64(content))
        return Json.decodeValue(messageStr, P2PMessage::class.java)
    }

    fun concat(other: P2PPacket): P2PPacket {
        if (withTail) return this
        if (other.withHead) return other
        val result = P2PPacket()
        result.withHead = withHead
        result.withTail = other.withTail
        result.content = content + other.content
        return result
    }
}
