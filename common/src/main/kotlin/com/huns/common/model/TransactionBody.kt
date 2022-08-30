package com.huns.common.model

data class TransactionBody(
    var operation: Byte = -1,
    var table: String = "",
    var json: String? = null,
    var oldJson: String? = null,
    var txId: String? = null,
    var sign: String = "",
    var publicKey: String = ""
) : java.io.Serializable {

    constructor(transaction: Transaction) : this(
        operation = transaction.base.operation,
        table = transaction.base.table,
        json = transaction.json,
        oldJson = transaction.base.oldJson,
        txId = transaction.base.txId,
        sign = transaction.sign,
        publicKey = transaction.publicKey
    )
}
