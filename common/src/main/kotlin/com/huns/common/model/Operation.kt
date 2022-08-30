package com.huns.common.model

enum class Operation(val value: Byte) {
    ADD(1),
    DELETE(-1),
    UPDATE(2)
}