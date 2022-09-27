package com.huns.chain.sqlite.data.repository

import com.huns.chain.sqlite.data.model.SqlSync
import com.huns.chain.sqlite.data.model.convertSqlSync
import com.huns.chain.sqlite.data.query
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Tuple

class SqlSyncRepository(private val jdbcPool: JDBCPool) {

    suspend fun findTopOrderByIdDesc(): SqlSync? {
        val tuple = Tuple.tuple()
        return query(jdbcPool, "SELECT * FROM sql_sync ORDER BY id desc limit 0, 1", tuple).map { convertSqlSync(it) }.firstOrNull()
    }

    suspend fun save(sqlSync: SqlSync): SqlSync? {
        val tuple = Tuple.of(sqlSync.id, sqlSync.createTime, sqlSync.hash)
        return query(jdbcPool, "INSERT INTO sql_sync (id, create_time, hash) VALUES (?, ?, ?)", tuple).map { convertSqlSync(it) }.firstOrNull()
    }
}