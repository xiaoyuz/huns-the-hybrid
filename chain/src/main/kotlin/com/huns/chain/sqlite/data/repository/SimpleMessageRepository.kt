package com.huns.chain.sqlite.data.repository

import com.huns.chain.sqlite.data.model.SimpleMessage
import com.huns.chain.sqlite.data.model.SqlSync
import com.huns.chain.sqlite.data.model.convertSimpleMessage
import com.huns.chain.sqlite.data.model.convertSqlSync
import com.huns.chain.sqlite.data.query
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Tuple

class SimpleMessageRepository(private val jdbcPool: JDBCPool) {

    suspend fun save(simpleMessage: SimpleMessage): SqlSync? {
        val tuple = Tuple.of(
            simpleMessage.base.id,
            simpleMessage.base.createTime,
            simpleMessage.base.updateTime,
            simpleMessage.base.publicKey,
            simpleMessage.messageId,
            simpleMessage.content
        )
        return query(jdbcPool, "INSERT INTO simple_message (id, create_time, update_time, public_key, message_id, content) VALUES (?, ?, ?, ?, ?, ?)", tuple).map { convertSqlSync(it) }.firstOrNull()
    }

    suspend fun deleteByMessageId(messageId: String) {
        val tuple = Tuple.of(messageId)
        query(jdbcPool, "DELETE FROM simple_message WHERE message_id = ?", tuple)
    }

    suspend fun findByMessageId(messageId: String): SimpleMessage? {
        val tuple = Tuple.of(messageId)
        return query(jdbcPool, "SELECT * FROM simple_message WHERE message_id = ?", tuple).map { convertSimpleMessage(it) }.firstOrNull()
    }
}