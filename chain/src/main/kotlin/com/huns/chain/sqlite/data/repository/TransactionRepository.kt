package com.huns.chain.sqlite.data.repository

import com.huns.chain.sqlite.data.query
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Tuple

class TransactionRepository(private val jdbcPool: JDBCPool) {

    suspend fun executeSql(queryStr: String) {
        val tuple = Tuple.tuple()
        query(jdbcPool, queryStr, tuple)
    }
}