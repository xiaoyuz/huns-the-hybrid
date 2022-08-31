package com.huns.chain.p2p.packet.reader

import com.huns.chain.p2p.message.P2PMessage

abstract class PacketReader(
    private val successor: PacketReader? = null
) {
    abstract fun process(data: String): P2PMessage?

    fun successorProcess(data: String) = successor?.process(data)
}