package com.huns.chain.p2p.message

import com.huns.chain.EnvConfig
import com.huns.chain.common.bean.NodeData
import com.huns.common.UUID
import com.huns.common.getIp

data class CommonInfo(
    var timeMs: Long = System.currentTimeMillis(),
    var requestId: String = UUID(),
    var responseId: String = "",
    var nodeData: NodeData = NodeData(
        appId = EnvConfig.nodeAppId,
        ip = getIp(),
        port = EnvConfig.tcpPort
    )
) : java.io.Serializable