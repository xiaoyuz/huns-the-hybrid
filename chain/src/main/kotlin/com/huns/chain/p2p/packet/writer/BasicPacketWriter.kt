package com.huns.chain.p2p.packet.writer

class BasicPacketWriter(
    successor: PacketWriter? = null
) : PacketWriter(successor) {

    override fun process(data: String): String? {
        return "<$data>"
    }
}