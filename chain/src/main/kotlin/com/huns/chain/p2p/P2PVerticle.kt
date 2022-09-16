package com.huns.chain.p2p

import com.huns.chain.EnvConfig
import com.huns.common.getIp
import com.huns.common.handleMessage
import com.huns.chain.core.*
import com.huns.chain.common.bean.NodeData
import com.huns.chain.p2p.client.P2PClient
import com.huns.chain.p2p.message.P2PMessage
import com.huns.chain.p2p.message.PING
import com.huns.chain.p2p.message.PingMessage
import com.huns.chain.p2p.server.P2PServer
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.kotlin.coroutines.CoroutineVerticle

private const val SCHEDULE_PING_TIME = 5 * 1000L // 5s

class P2PVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(P2PVerticle::class.java)

    private lateinit var p2pClient: P2PClient
    private lateinit var p2pServer: P2PServer

    override suspend fun start() {
        EnvConfig.tcpPort = config.getInteger("tcp_port")

        p2pClient = P2PClient(vertx)
        p2pServer = P2PServer(vertx)

        val bus = vertx.eventBus()
        bus.consumer(P2P_CONNECT_NODES, this::refreshClients)
        bus.consumer(P2P_BROADCAST, this::broadcast)
        bus.consumer(P2P_BROADCAST_INCLUDE_SELF, this::broadcastIncludeSelf)
        bus.consumer(P2P_PING, this::ping)
        bus.consumer(P2P_SEND, this::send)

        vertx.setTimer(SCHEDULE_PING_TIME, this::pingAllServers)
    }

    private fun pingAllServers(timerId: Long) {
        logger.info("Ping other servers")
        val pingMessage = PingMessage(
            nodeData = NodeData(
                appId = EnvConfig.nodeAppId,
                ip = getIp(),
                port = EnvConfig.tcpPort
            )
        )
        p2pClient.broadcast(
            P2PMessage(
                type = PING,
                data = Json.encode(pingMessage)
            )
        )
        vertx.setTimer(SCHEDULE_PING_TIME, this::pingAllServers)
    }

    private fun refreshClients(message: Message<Set<NodeData>>) {
        logger.info("Refreshing p2p clients: ${message.body()}")
        handleMessage(message, P2P_CODE_ERROR) {
            p2pClient.refreshClients(it.body())
            message.reply("")
        }
    }

    private fun broadcast(message: Message<P2PMessage>) {
        logger.info("Broadcast message: ${message.body()}")
        handleMessage(message, P2P_CODE_ERROR) {
            p2pClient.broadcast(it.body())
            message.reply("")
        }
    }

    private fun send(message: Message<Pair<NodeData, P2PMessage>>) {
        logger.info("Send message: ${message.body()}")
        handleMessage(message, P2P_CODE_ERROR) {
            p2pClient.send(it.body().first, it.body().second)
            message.reply("")
        }
    }

    private fun broadcastIncludeSelf(message: Message<P2PMessage>) {
        logger.info("Broadcast message include self: ${message.body()}")
        handleMessage(message, P2P_CODE_ERROR) {
            p2pClient.broadcast(it.body(), true)
            message.reply("")
        }
    }

    private fun ping(message: Message<NodeData>) {
        handleMessage(message, P2P_CODE_ERROR) {
            if (!p2pClient.socketExisted(it.body())) {
                p2pClient.addClient(it.body())
            }
            message.reply("")
        }
    }
}