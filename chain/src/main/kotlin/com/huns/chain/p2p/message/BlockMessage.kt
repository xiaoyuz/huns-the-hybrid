package com.huns.chain.p2p.message

import com.huns.chain.block.model.Block

data class BlockMessage(
    var common: CommonInfo = CommonInfo(),
    var block: Block = Block()
) : java.io.Serializable
