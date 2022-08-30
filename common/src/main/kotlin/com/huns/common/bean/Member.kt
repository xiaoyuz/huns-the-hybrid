package com.huns.common.bean

import com.huns.common.exception.Errors
import com.huns.common.exception.KeyException

data class Member(
    var id: Long = 0,
    var createTime: Long = 0,
    var updateTime: Long = 0,
    var appId: String = "",
    var name: String = "",
    var address: String = "",
    var groupId: String = ""
) : java.io.Serializable {

    fun ipAndPort() = address.split(":").let {
        if (it.size < 2) throw KeyException(Errors.INVALID_PARAM_ERROR)
        it[0] to it[1].toInt()
    }
}