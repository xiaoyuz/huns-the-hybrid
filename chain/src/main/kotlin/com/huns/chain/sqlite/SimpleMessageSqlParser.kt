package com.huns.chain.sqlite

import com.huns.chain.sqlite.data.model.SimpleMessage
import com.huns.chain.sqlite.data.repository.SimpleMessageRepository
import com.huns.common.model.Operation
import io.vertx.core.json.Json

class SimpleMessageSqlParser(
    var simpleMessageRepository: SimpleMessageRepository
) : BaseSqlParser<SimpleMessage>() {

    override suspend fun parse(operation: Byte, id: String, entityStr: String) {
        val entity = Json.decodeValue(entityStr, entityClass())
        if (operation == Operation.ADD.value) {
            entity.base.createTime = System.currentTimeMillis()
            entity.messageId = id
            simpleMessageRepository.save(entity)
        } else if (operation == Operation.DELETE.value) {
            simpleMessageRepository.deleteByMessageId(id)
        } else {
            // TODO update operation
        }
    }

    override fun entityClass() = SimpleMessage::class.java
}