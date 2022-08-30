package com.huns.chain.storage

data class DBKV(
    var key: String,
    var value: String
) : java.io.Serializable