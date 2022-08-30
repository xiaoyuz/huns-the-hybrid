package com.huns.chain.common.bean

import com.huns.common.model.Transaction

data class TransactionQueryResult(
    var transaction: Transaction = Transaction(),
    var blockHash: String = ""
)
