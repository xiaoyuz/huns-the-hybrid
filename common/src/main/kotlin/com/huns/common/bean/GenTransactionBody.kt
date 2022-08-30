package com.huns.common.bean

data class GenTransactionBody(
    var operation: Byte = -1,
    var table: String = "",
    var json: String? = null,
    var oldJson: String? = null,
    var txId: String? = null,
    var privateKey: String = "",
    var publicKey: String = ""
) : java.io.Serializable
