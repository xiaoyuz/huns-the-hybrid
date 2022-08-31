package com.huns.chain.p2p.packet.writer

abstract class PacketWriter(
    private val successor: PacketWriter? = null
) {
    abstract fun process(data: String): String?

    fun successorProcess(data: String) = successor?.process(data)
}