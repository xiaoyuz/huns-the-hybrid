package com.huns.chain.p2p.packet

import io.vertx.core.buffer.Buffer

class PacketHelper {

    private var tempPacket: P2PPacket? = null

    fun fromString(str: String): List<P2PPacket> {
        val packets = mutableListOf<P2PPacket>()
        var sb: StringBuilder? = null
        var packet: P2PPacket? = null
        str.forEach {
            if (packet == null) packet = P2PPacket()
            if (sb == null) sb = StringBuilder()
            if (it == '<') {
                sb = StringBuilder()
                packet!!.withHead = true
            } else if (it == '>') {
                packet!!.withTail = true
                packet!!.content = sb.toString()
                packets.add(packet!!)
                packet = null
                sb = null
            } else {
                sb!!.append(it)
            }
        }
        if (packet != null) {
            packet!!.content = sb.toString()
            packets.add(packet!!)
        }
        return packets
    }

    fun fromBuffer(buffer: Buffer) = fromString(buffer.toString())

    fun handleBuffer(buffer: Buffer, handler: (P2PPacket) -> Unit) {
        val packets = fromBuffer(buffer)
        packets.forEach { packet ->
            if (packet.withHead && packet.withTail) {
                handler(packet)
            } else {
                tempPacket = tempPacket?.concat(packet) ?: packet
                if (tempPacket!!.withHead && tempPacket!!.withTail) {
                    handler(tempPacket!!)
                    tempPacket = null
                }
            }
        }
    }
}

fun main() {
    val helper = PacketHelper()
    val test1 = helper.fromString("<fdfdfdfd><erwerwergg><ewrsfd>")
    val test2 = helper.fromString("<fdfdfdfd><erwerwergg><ewrs")
    val test3 = helper.fromString("fdfd><erwerwergg><ewrsfd>")
    val test4 = helper.fromString("fdfd><erwerwergg><ewrs")
    val test5 = helper.fromString("")
    val test6 = helper.fromString("<>")
    val test7 = helper.fromString("fdfd><erwerwergg<ewrs")

    val a = 0
}