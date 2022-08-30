package com.huns.common.bean

data class Permission(
    var id: Long = 0,
    var createTime: Long = 0,
    var updateTime: Long = 0,
    var tableName: String = "",
    var permissionType: Byte = -1,
    var publicKey: String = "",
    var groupId: String = ""
) : java.io.Serializable