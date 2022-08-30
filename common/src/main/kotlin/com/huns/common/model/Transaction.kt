package com.huns.common.model

import com.huns.common.bean.GenTransactionBody
import org.apache.commons.codec.binary.Base64

data class Transaction(
    var base: TransactionBase = TransactionBase(),
    var json: String? = null,
    var timeStamp: Long = 0,
    var publicKey: String = "",
    var sign: String = "",
    var hash: String = ""
) : java.io.Serializable {
    constructor(genTransactionBody: GenTransactionBody) : this(
        base = TransactionBase(
            operation = genTransactionBody.operation,
            table = genTransactionBody.table,
            oldJson = genTransactionBody.oldJson,
            txId = genTransactionBody.txId
        ),
        json = genTransactionBody.json,
        timeStamp = System.currentTimeMillis(),
        publicKey = genTransactionBody.publicKey,
        sign = "",
        hash = ""
    )

    constructor(transactionBody: TransactionBody) : this(
        base = TransactionBase(
            operation = transactionBody.operation,
            table = transactionBody.table,
            oldJson = transactionBody.oldJson,
            txId = transactionBody.txId
        ),
        json = transactionBody.json,
        timeStamp = System.currentTimeMillis(),
        publicKey = transactionBody.publicKey,
        sign = transactionBody.sign,
        hash = ""
    )

    fun genInfoString() = listOf(
        base64EncodeStr(base.operation.toString()),
        base64EncodeStr(base.table),
        base64EncodeStr(base.txId ?: ""),
        base64EncodeStr(json ?: "")
    ).joinToString(">>")

    private fun base64EncodeStr(str: String) = Base64.encodeBase64String(str.toByteArray())
}