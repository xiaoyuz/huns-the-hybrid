package com.huns.chain.p2p.packet

data class P2PPacket(
    var content: String = "",
    var withHead: Boolean = false,
    var withTail: Boolean = false
) {

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
