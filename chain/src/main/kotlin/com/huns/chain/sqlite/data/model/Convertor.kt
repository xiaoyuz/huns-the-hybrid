package com.huns.chain.sqlite.data.model

import io.vertx.sqlclient.Row
import java.time.ZoneOffset

fun convertSqlSync(row: Row) = SqlSync(
    id = row.getLong("id"),
    createTime = row.getLocalDateTime("create_time").toEpochSecond(ZoneOffset.UTC),
    hash = row.getString("hash")
)

fun convertSimpleMessage(row: Row) = SimpleMessage(
    base = BaseEntity(
        id = row.getLong("id"),
        createTime = row.getLocalDateTime("create_time").toEpochSecond(ZoneOffset.UTC),
        updateTime = row.getLocalDateTime("update_time").toEpochSecond(ZoneOffset.UTC),
        publicKey = row.getString("public_key")
    ),
    messageId = row.getString("message_id"),
    content = row.getString("content")
)