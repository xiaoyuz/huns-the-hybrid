package com.huns.blockmanager.data.repository

import com.huns.blockmanager.data.model.convertMember
import com.huns.common.bean.Member
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Tuple

class MemberRepository(private val mysqlPool: MySQLPool) {

    suspend fun findByName(name: String): List<Member> {
        val tuple = Tuple.of(name)
        return query(mysqlPool, "SELECT * FROM member WHERE name = ?", tuple).map { convertMember(it) }
    }

    suspend fun findByGroupIdAndAppIdNot(id: String, groupId: String): List<Member> {
        val tuple = Tuple.of(id, groupId)
        return query(mysqlPool, "SELECT * FROM member WHERE group_id = ? and id <> ?", tuple).map { convertMember(it) }
    }

    suspend fun findFirstByAppId(appId: String): Member? {
        val tuple = Tuple.of(appId)
        return query(mysqlPool, "SELECT * FROM member WHERE app_id = ?", tuple).map { convertMember(it) }.firstOrNull()
    }

    suspend fun findByGroupId(groupId : String): List<Member> {
        val tuple = Tuple.of(groupId)
        return query(mysqlPool, "SELECT * FROM member WHERE group_id = ?", tuple).map { convertMember(it) }
    }
}