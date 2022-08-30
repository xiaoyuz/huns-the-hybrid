package com.huns.blockmanager.data.model

import com.huns.common.bean.Member
import com.huns.common.bean.Permission
import io.vertx.sqlclient.Row
import java.time.ZoneOffset

fun convertMember(row: Row) =  Member(
    id = row.getLong("id"),
    createTime = row.getLocalDateTime("create_time").toEpochSecond(ZoneOffset.UTC),
    updateTime = row.getLocalDateTime("update_time").toEpochSecond(ZoneOffset.UTC),
    address = row.getString("address"),
    name = row.getString("name"),
    appId = row.getString("app_id"),
    groupId = row.getString("group_id")
)

fun convertPermission(row: Row) =  Permission(
    id = row.getLong("id"),
    createTime = row.getLocalDateTime("create_time").toEpochSecond(ZoneOffset.UTC),
    updateTime = row.getLocalDateTime("update_time").toEpochSecond(ZoneOffset.UTC),
    tableName = row.getString("table_name"),
    permissionType = row.getShort("permission_type").toByte(),
    publicKey = row.getString("public_key"),
    groupId = row.getString("group_id")
)