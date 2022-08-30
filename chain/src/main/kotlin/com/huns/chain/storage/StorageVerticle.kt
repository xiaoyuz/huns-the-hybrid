package com.huns.chain.storage

import com.huns.common.handleMessage
import com.huns.chain.core.STORAGE_CODE_DB_ERROR
import com.huns.chain.core.STORAGE_GET
import com.huns.chain.core.STORAGE_PUT
import com.huns.chain.core.STORAGE_REMOVE
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.iq80.leveldb.DB
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory
import java.io.File

class StorageVerticle : CoroutineVerticle() {

    private lateinit var mLevelDB: DB
    private lateinit var mDBStore: DBStore

    override suspend fun start() {
        val levelPath = config.getString("level_path")

        val options = Options().apply {
            createIfMissing(true)
        }
        try {
            mLevelDB = Iq80DBFactory.factory.open(File(levelPath), options)
            mDBStore = LevelDBStore(mLevelDB)

            val bus = vertx.eventBus()
            bus.consumer(STORAGE_PUT, this::put)
            bus.consumer(STORAGE_GET, this::get)
            bus.consumer(STORAGE_REMOVE, this::remove)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun get(message: Message<String>) {
        handleMessage(message, STORAGE_CODE_DB_ERROR) { mes ->
            mDBStore.get(mes.body()).let {
                mes.reply(it)
            }
        }
    }

    private fun put(message: Message<DBKV>) {
        handleMessage(message, STORAGE_CODE_DB_ERROR) { mes ->
            val kv = mes.body()
            mDBStore.put(kv.key, kv.value)
            mes.reply(kv)
        }
    }

    private fun remove(message: Message<String>) {
        handleMessage(message, STORAGE_CODE_DB_ERROR) { mes ->
            mDBStore.remove(mes.body())
        }
    }
}