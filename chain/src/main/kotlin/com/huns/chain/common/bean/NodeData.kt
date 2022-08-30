package com.huns.chain.common.bean

import com.huns.common.getAddress

data class NodeData(
    var appId: String = "",
    var ip: String = "",
    var port: Int = 0
) : java.io.Serializable {

    fun pingAddress() = getAddress(ip, port)
}