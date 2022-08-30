package com.huns.chain.p2p.message

import com.huns.chain.common.bean.NodeData
import com.huns.common.UUID
import com.huns.common.getIp

data class CommonInfo(
    var timeMs: Long = System.currentTimeMillis(),
    var requestId: String = UUID(),
    var responseId: String = "",
    var nodeData: NodeData = NodeData(
        appId = com.huns.chain.EnvConfig.nodeAppId,
        ip = getIp(),
        port = com.huns.chain.EnvConfig.tcpPort
    )
) : java.io.Serializable