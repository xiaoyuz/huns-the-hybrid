package com.huns.chain.sqlite.data.model

data class SimpleMessage(
    var base: BaseEntity = BaseEntity(),
    var messageId: String = "",
    var content: String = ""
)
