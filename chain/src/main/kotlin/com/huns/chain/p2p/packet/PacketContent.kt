package com.huns.chain.p2p.packet

data class PacketContent(
    var publicKey: String = "",
    var sign: String = "",
    var data: String = ""
)