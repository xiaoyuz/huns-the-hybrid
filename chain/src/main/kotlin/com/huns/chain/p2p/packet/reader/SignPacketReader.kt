package com.huns.chain.p2p.packet.reader

import com.huns.chain.p2p.message.P2PMessage
import com.huns.chain.p2p.packet.PacketContent
import com.huns.common.crypto.ECDSA
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import org.apache.commons.codec.binary.Base64

class SignPacketReader(
    successor: PacketReader? = null
) : PacketReader(successor) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun process(data: String): P2PMessage? {
        val decodedData = String(Base64.decodeBase64(data))
        try {
            val packetContent = Json.decodeValue(decodedData, PacketContent::class.java)
            val publicKey = packetContent.publicKey
            val sign = packetContent.sign
            return if (ECDSA.verify(packetContent.data, sign, publicKey)) {
                successorProcess(packetContent.data)
            } else null
        } catch (e: Exception) {
            logger.error(e)
            logger.info("AAAAAAAAAAAAAAAAAAAAAAAAAAA $decodedData")
            return null
        }
    }
}