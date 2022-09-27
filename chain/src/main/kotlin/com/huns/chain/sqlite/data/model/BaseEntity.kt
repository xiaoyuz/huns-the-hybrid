package com.huns.chain.sqlite.data.model

data class BaseEntity(
    var id: Long = 0,
    var createTime: Long = 0,
    var updateTime: Long = 0,
    var publicKey: String = ""
)
