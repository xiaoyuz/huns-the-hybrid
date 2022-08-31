package com.huns.chain.permission

import com.huns.chain.block.model.Block
import com.huns.common.model.Transaction
import com.huns.chain.common.PERMISSION_TYPE_ALL
import com.huns.chain.common.PERMISSION_TYPE_OWNER
import com.huns.common.bean.Permission

object PermissionHelper {

    private val permissionMap = mutableMapOf<String, MutableList<Permission>>()
    private val validRemoteIps = mutableSetOf<String>()

    fun checkPermission(block: Block) = checkPermission(block.blockBody.transactions)

    fun checkRemoteIp(ip: String) = validRemoteIps.contains(ip)

    fun checkPermission(transactions: List<Transaction>): Boolean {
        transactions.forEach {
            val publicKey = it.publicKey
            val tableName = it.base.table
            val operation = it.base.operation
            // TODO: too many loops
            if (!checkOperation(publicKey, tableName, operation)) {
                return false
            }
        }
        return true
    }

    fun savePermissionList(permissions: List<Permission>) {
        permissionMap.clear()
        permissions.forEach {
            val key = it.tableName
            if (!permissionMap.containsKey(key)) permissionMap[key] = mutableListOf()
            permissionMap[key]?.add(it)
        }
    }

    fun saveValidRemoteIp(addresses: Set<String>) {
        validRemoteIps.clear()
        validRemoteIps.addAll(addresses)
    }

    private fun checkOperation(publicKey: String, tableName: String, operation: Byte) =
        permissionMap[tableName]?.let { permissionList ->
            val userPermissionSet = permissionList.filter {
                "*" == it.publicKey || publicKey == it.publicKey
            }.map { it.permissionType }.toSet()
            userPermissionSet.contains(PERMISSION_TYPE_OWNER) ||
                    userPermissionSet.contains(PERMISSION_TYPE_ALL) ||
                    userPermissionSet.contains(operation)
        } ?: false
}