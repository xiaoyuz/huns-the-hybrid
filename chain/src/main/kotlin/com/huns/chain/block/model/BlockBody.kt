package com.huns.chain.block.model

import com.huns.common.model.Transaction

data class BlockBody(
    var transactions: List<Transaction> = emptyList()
) : java.io.Serializable