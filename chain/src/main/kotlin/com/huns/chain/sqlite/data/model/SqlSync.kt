package com.huns.chain.sqlite.data.model

data class SqlSync(
    var id: Long = 0,
    var createTime: Long = 0,
    var hash: String = ""
) : java.io.Serializable
