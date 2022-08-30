package com.huns.common.model

data class TransactionBase(
    var operation: Byte = -1,
    var table: String = "",
    var oldJson: String? = null,
    var txId: String? = null
) : java.io.Serializable