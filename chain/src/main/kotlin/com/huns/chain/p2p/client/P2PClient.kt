package com.huns.chain.p2p.client

import com.huns.chain.EnvConfig
import com.huns.common.getAddress
import com.huns.common.getIp
import com.huns.common.bean.MemberData
import com.huns.chain.common.bean.NodeData
import com.huns.chain.p2p.message.*
import com.huns.chain.p2p.packet.P2PPacket
import com.huns.chain.p2p.packet.writer.BasicPacketWriter
import com.huns.chain.p2p.packet.writer.MessagePacketWriter
import com.huns.chain.p2p.packet.writer.PacketWriter
import com.huns.chain.p2p.packet.writer.SignPacketWriter
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.coroutines.await

private const val SOCKET_SIZE = 262144 // 256k

var connectedSockets = mutableMapOf<NodeData, NetSocket>()

class P2PClient(
    private val vertx: Vertx
) {
    private val logger = LoggerFactory.getLogger(P2PClient::class.java)

    private val netClient: NetClient
    private val packetWriter: PacketWriter by lazy { MessagePacketWriter(SignPacketWriter(BasicPacketWriter())) }

    init {
        val netClientOptions = NetClientOptions().apply {
            sendBufferSize = SOCKET_SIZE
            receiveBufferSize = SOCKET_SIZE
        }
        netClient = vertx.createNetClient(netClientOptions)
    }

    fun socketExisted(nodeData: NodeData) = connectedSockets.containsKey(nodeData)

    suspend fun addClient(nodeData: NodeData) {
        try {
            val socket = netClient.connect(nodeData.port, nodeData.ip).await()
            socket
                .exceptionHandler {
                    connectedSockets.remove(nodeData)
                }
                .closeHandler {
                    connectedSockets.remove(nodeData)
                    logger.info("Connection closed to ${socket.remoteAddress()}, list: $connectedSockets")
                }
            connectedSockets[nodeData] = socket
            logger.info("Connection to ${socket.remoteAddress()}, list: $connectedSockets")
        } catch (e: Exception) {
            logger.error("Connection failed: ${nodeData.pingAddress()}")
        }
    }

    suspend fun refreshClients(nodeDatas: Set<NodeData>) {
        val currentNodeDatas = connectedSockets.keys
        val needRemoved = currentNodeDatas.minus(nodeDatas)
        val needAdded = nodeDatas.minus(currentNodeDatas)
        needAdded.forEach {
            addClient(it)
        }
        needRemoved.forEach { nodeData ->
            connectedSockets[nodeData]?.close()
            connectedSockets.remove(nodeData)
        }
    }

    fun broadcast(p2PMessage: P2PMessage, includeSelf: Boolean = false) {
        val localAddress = getAddress(getIp(), EnvConfig.tcpPort)
        connectedSockets.values.forEach { netSocket ->
            if (includeSelf || netSocket.remoteAddress().toString() != localAddress) {
                val json = Json.encode(p2PMessage)
                packetWriter.process(json)?.let {
                    netSocket.write(it)
                }
            }
        }
    }

    fun send(nodeData: NodeData, p2PMessage: P2PMessage) {
        val json = Json.encode(p2PMessage)
        packetWriter.process(json)?.let {
            connectedSockets[nodeData]?.write(it)
        }
    }
}