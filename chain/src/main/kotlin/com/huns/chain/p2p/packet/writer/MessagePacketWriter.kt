package com.huns.chain.p2p.packet.writer

import org.apache.commons.codec.binary.Base64

class MessagePacketWriter(
    successor: PacketWriter? = null
) : PacketWriter(successor) {

    override fun process(data: String): String? {
        return successorProcess(Base64.encodeBase64String(data.toByteArray()))
    }
}