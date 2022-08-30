package com.huns.chain.p2p.message

import com.huns.chain.common.bean.NodeData

data class PingMessage(
    var common: CommonInfo = CommonInfo(),
    var nodeData: NodeData = NodeData()
) : java.io.Serializable
