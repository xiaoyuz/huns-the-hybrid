package com.huns.blockmanager.data.repository

import com.huns.blockmanager.data.model.convertPermission
import com.huns.common.bean.Permission
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Tuple

class PermissionRepository(private val mysqlPool: MySQLPool) {

    suspend fun findByGroupId(groupId: String): List<Permission> {
        val tuple = Tuple.of(groupId)
        return query(mysqlPool, "SELECT * FROM permission WHERE group_id = ?", tuple).map { convertPermission(it) }
    }
}