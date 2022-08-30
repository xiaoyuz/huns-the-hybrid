package com.huns.chain.storage

interface DBStore {

    suspend fun put(key: String, value: String)

    suspend fun get(key: String): String?

    suspend fun remove(key: String)
}