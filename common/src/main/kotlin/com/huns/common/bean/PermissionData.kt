package com.huns.common.bean

data class PermissionData(
    var permissions: List<Permission> = emptyList()
) : java.io.Serializable