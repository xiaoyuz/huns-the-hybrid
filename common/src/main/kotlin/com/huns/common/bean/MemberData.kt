package com.huns.common.bean

data class MemberData(
    var members: List<Member> = emptyList()
) : java.io.Serializable