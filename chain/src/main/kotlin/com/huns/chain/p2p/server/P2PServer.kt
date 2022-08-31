package com.huns.chain.p2p.server

import com.huns.chain.EnvConfig
import com.huns.chain.p2p.server.handler.BaseHandler
import com.huns.chain.p2p.message.*
import com.huns.chain.p2p.packet.reader.BasicPacketReader
import com.huns.chain.p2p.packet.reader.MessagePacketReader
import com.huns.chain.p2p.packet.reader.PacketReader
import com.huns.chain.p2p.packet.reader.SignPacketReader
import com.huns.chain.p2p.server.handler.*
import com.huns.chain.permission.PermissionHelper
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.net.NetServerOptions
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val SOCKET_SIZE = 262144 // 256k

class P2PServer(
    private val vertx: Vertx
) {
    private val logger = LoggerFactory.getLogger(P2PServer::class.java)

    private val packetReader: PacketReader by lazy { BasicPacketReader(SignPacketReader(MessagePacketReader())) }
    private val handlerMap: Map<Byte, BaseHandler<*>> = mapOf(
        PING to PingRequestHandler(vertx),
        GEN_BLOCK_COMPLETE_REQ to GenCompleteReqHandler(vertx),
        GEN_BLOCK_REQ to GenBlockReqHandler(vertx),
        TOTAL_BLOCK_REQ to TotalBlockReqHandler(vertx),
        BLOCK_REQ to BlockReqHandler(vertx),
        NEXT_BLOCK_REQ to NextBlockReqHandler(vertx),
        PBFT_VOTE to PbftVoteReqHandler(vertx),

        TOTAL_BLOCK_RESP to TotalBlockRespHandler(vertx),
        NEXT_BLOCK_RESP to NextBlockRespHandler(vertx),
        BLOCK_RESP to BlockRespHandler(vertx),
    )

    init {
        val netServerOptions = NetServerOptions().apply {
            sendBufferSize = SOCKET_SIZE
            receiveBufferSize = SOCKET_SIZE
        }
        vertx
            .createNetServer(netServerOptions)
            .connectHandler(this::handleServerConnect)
            .listen(EnvConfig.tcpPort)
    }

    private fun handleServerConnect(socket: NetSocket) {
        logger.info("Connection from ${socket.remoteAddress()}")
        socket
            .handler { handleBuffer(socket, it) }
            .exceptionHandler { handleException(socket, it) }
            .closeHandler {
                logger.info("Connection from ${socket.remoteAddress()} closed")
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleBuffer(socket: NetSocket, buffer: Buffer) {
        if (!PermissionHelper.checkRemoteIp(socket.remoteAddress().hostAddress())) return
        packetReader.successorProcess(buffer.toString())?.let {
            GlobalScope.launch(vertx.dispatcher()) {
                handlerMap[it.type]?.execute(it, socket)
            }
        }
    }

    private fun handleException(socket: NetSocket, t: Throwable) {
        logger.warn("Connection from ${socket.remoteAddress()} exception, t: ${t.message}")
    }
}