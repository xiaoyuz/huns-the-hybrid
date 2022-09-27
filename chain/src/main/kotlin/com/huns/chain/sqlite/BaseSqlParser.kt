package com.huns.chain.sqlite

abstract class BaseSqlParser<T> {

    abstract suspend fun parse(operation: Byte, id: String, entityStr: String)

    abstract fun entityClass(): Class<T>
}