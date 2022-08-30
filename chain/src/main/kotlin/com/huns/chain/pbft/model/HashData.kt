package com.huns.chain.pbft.model

data class HashData(
    var hash: String = "",
    var preHash: String = "",
    var appId: String = ""
) : java.io.Serializable
