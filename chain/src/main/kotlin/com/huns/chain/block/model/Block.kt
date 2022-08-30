package com.huns.chain.block.model

import com.huns.common.crypto.SHA256
import com.huns.common.crypto.toHex

data class Block(
    var blockHeader: BlockHeader = BlockHeader(),
    var blockBody: BlockBody = BlockBody(),
    var hash: String = ""
) : java.io.Serializable {

    fun calculateHash() = SHA256.sha256(blockHeader.toString() + blockBody.toString()).toHex()
}
