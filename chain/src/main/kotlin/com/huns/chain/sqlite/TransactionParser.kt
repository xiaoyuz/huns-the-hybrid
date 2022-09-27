package com.huns.chain.sqlite

import com.huns.chain.sqlite.data.repository.SimpleMessageRepository
import com.huns.common.model.TransactionBase
import io.vertx.jdbcclient.JDBCPool

class TransactionParser(
    jdbcPool: JDBCPool
) {

    private val mParsers: List<BaseSqlParser<*>> = listOf(
        SimpleMessageSqlParser(SimpleMessageRepository(jdbcPool))
    )

    suspend fun parse(transactionBase: TransactionBase) {
        val operation = transactionBase.operation
        val table = transactionBase.table
        val json = transactionBase.oldJson
        mParsers.forEach {
            if (it.entityClass().name == table) {
                it.parse(operation, transactionBase.txId ?: "", json ?: "")
                return@forEach
            }
        }
    }
}