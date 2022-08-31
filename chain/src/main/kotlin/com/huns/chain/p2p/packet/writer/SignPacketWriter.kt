package com.huns.chain.p2p.packet.writer

import com.huns.chain.EnvConfig
import com.huns.chain.p2p.packet.PacketContent
import com.huns.common.crypto.ECDSA
import io.vertx.core.json.Json
import org.apache.commons.codec.binary.Base64

class SignPacketWriter(
    successor: PacketWriter? = null
) : PacketWriter(successor) {

    override fun process(data: String): String? {
        val privateKey = EnvConfig.nodePrivateKey
        if (privateKey.isEmpty()) return null
        val sign = ECDSA.sign(privateKey, data)
        val packetContent = PacketContent(
            publicKey = EnvConfig.nodePublicKey,
            sign = sign,
            data = data
        )
        val json = Json.encode(packetContent)
        return successorProcess(Base64.encodeBase64String(json.toByteArray()))
    }
}