package com.huns.chain.permission

import com.huns.chain.EnvConfig
import com.huns.chain.block.model.Block
import com.huns.chain.common.bean.NodeData
import com.huns.common.model.Transaction
import com.huns.common.getAddress
import com.huns.common.getIp
import com.huns.common.handleMessage
import com.huns.chain.core.*
import com.huns.common.bean.MemberData
import com.huns.common.bean.PermissionData
import com.huns.chain.pbft.PbftConfig
import com.huns.common.crypto.ECDSA
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

private const val SCHEDULE_FETCH_SERVERS_TIME = 1 * 60 * 1000L // 1 min
private const val SCHEDULE_FETCH_PERMISSION_TIME = 1 * 60 * 1000L // 1 min

class FetcherVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(FetcherVerticle::class.java)

    private lateinit var webClient: WebClient
    private var tcpPort: Int = 3000
    private lateinit var managerHost: String
    private lateinit var nodeName: String
    private var managerPort: Int = 0

    override suspend fun start() {
        EnvConfig.nodePublicKey = config.getString("node_public_key")
        EnvConfig.nodePrivateKey = config.getString("node_private_key")
        tcpPort = config.getInteger("tcp_port")
        managerHost = config.getString("manager_host")
        nodeName = config.getString("node_name")
        managerPort = config.getInteger("manager_port")

        val bus = vertx.eventBus()
        bus.consumer(FETCHER_CHECK_TRANSACTIONS_PERMISSION, this::checkTransactionsPermission)
        bus.consumer(FETCHER_CHECK_BLOCK_PERMISSION, this::checkBlockPermission)

        webClient = WebClient.create(vertx)

        fetchSchedule()
    }

    private fun checkTransactionsPermission(message: Message<List<Transaction>>) {
        handleMessage(message, FETCH_CODE_ERROR) {
            val result = PermissionHelper.checkPermission(it.body())
            message.reply(result)
        }
    }

    private fun checkBlockPermission(message: Message<Block>) {
        handleMessage(message, FETCH_CODE_ERROR) {
            val result = PermissionHelper.checkPermission(it.body().blockBody.transactions)
            message.reply(result)
        }
    }

    private fun fetchSchedule() {
        fetchPermissions(0)
        fetchOtherServers(0)
    }

    private fun fetchOtherServers(timerId: Long) {
        launch {
            logger.info("Fetching other servers: name: $nodeName, appId: ${EnvConfig.nodePublicKey}, " +
                    "address: ${getAddress(getIp(), tcpPort)}, manager: $managerHost$managerPort")
            val address = getAddress(getIp(), tcpPort)
            val sign = ECDSA.sign(EnvConfig.nodePrivateKey, address)
            val response = webClient
                .get(managerPort, managerHost, "/member")
                .setQueryParam("name", nodeName)
                .setQueryParam("appId", EnvConfig.nodePublicKey)
                .setQueryParam("address", getAddress(getIp(), tcpPort))
                .setQueryParam("sign", sign)
                .send().await().bodyAsJsonObject()
            if (response.getInteger("code") == 0) {
                val memberData = response.getJsonObject("content").mapTo(MemberData::class.java)
                logger.info("${memberData.members.size} needs connections: $memberData")
                if (memberData.members.isNotEmpty()) {
                    PbftConfig.pbftSize = ((memberData.members.size - 1) / 3).let { if (it > 0) it else 1 }
                    PbftConfig.pbftApproveCount = PbftConfig.pbftSize * 2 + 1
                    // connect nodes in P2P
                    val nodeDatas = memberData.members.map {
                        val (remoteIp, remotePort) = it.ipAndPort()
                        NodeData(appId = it.appId, ip = remoteIp, port = remotePort)
                    }.toSet()
                    PermissionHelper.saveValidRemoteIp(nodeDatas.map { it.ip }.toSet())
                    vertx.eventBus().request<String>(P2P_CONNECT_NODES, nodeDatas).await()
                }
            }
        }
        vertx.setTimer(SCHEDULE_FETCH_SERVERS_TIME, this::fetchOtherServers)
    }

    private fun fetchPermissions(timerId: Long) {
        launch {
            val response = webClient
                .get(managerPort, managerHost, "/permission")
                .setQueryParam("name", nodeName)
                .send().await().bodyAsJsonObject()
            if (response.getInteger("code") == 0) {
                val permissionData = response.getJsonObject("content").mapTo(PermissionData::class.java)
                PermissionHelper.savePermissionList(permissionData.permissions)
            }
        }
        vertx.setTimer(SCHEDULE_FETCH_PERMISSION_TIME, this::fetchPermissions)
    }
}