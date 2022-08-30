package com.huns.chain.p2p.message

data class NextBlockMessage(
    var common: CommonInfo = CommonInfo(),
    var hash: String = "",
    var prevHash: String = ""
) : java.io.Serializable
