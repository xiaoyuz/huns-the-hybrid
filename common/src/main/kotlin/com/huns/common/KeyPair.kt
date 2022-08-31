package com.huns.common

data class KeyPair(
    var publicKey: String = "",
    var privateKey: String = ""
) : java.io.Serializable