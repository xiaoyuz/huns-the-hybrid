package com.huns.chain.p2p.message

data class HashMessage(
    var common: CommonInfo = CommonInfo(),
    var hash: String = ""
) : java.io.Serializable
