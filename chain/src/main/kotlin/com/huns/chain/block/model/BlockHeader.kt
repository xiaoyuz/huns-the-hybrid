package com.huns.chain.block.model

data class BlockHeader(
    var version: Int = 0,
    var previousBlockHash: String = "",
    var merkleRootHash: String = "",
    var number: Int = 0,
    var timeStamp: Long = 0,
    var nonce: Long = 0
) : java.io.Serializable