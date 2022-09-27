package com.huns.chain.sqlite.data

import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

suspend fun query(jdbcPool: JDBCPool, queryStr: String, tuple: Tuple): RowSet<Row> {
    val connection = jdbcPool.connection.await()
    return connection
        .preparedQuery(queryStr)
        .execute(tuple).await().apply {
            connection.close()
        }
}