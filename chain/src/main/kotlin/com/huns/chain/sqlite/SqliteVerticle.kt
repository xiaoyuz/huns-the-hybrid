package com.huns.chain.sqlite

import com.huns.chain.block.model.Block
import com.huns.chain.common.manager.BlockManager
import com.huns.chain.core.DB_SYNC
import com.huns.chain.sqlite.data.repository.SqlSyncRepository
import com.huns.chain.sqlite.data.model.SqlSync
import com.huns.common.model.Transaction
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.sqlclient.PoolOptions

class SqliteVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(SqliteVerticle::class.java)

    private lateinit var mJdbcPool: JDBCPool
    private lateinit var mSqlSyncRepository: SqlSyncRepository
    private lateinit var mTransactionParser: TransactionParser

    private val mBlockManager: BlockManager by lazy { BlockManager(vertx) }

    override suspend fun start() {
        val sqlitePath = config.getString("sqlite_path")
        val jdbcConnectOptions = JDBCConnectOptions().apply {
            jdbcUrl = "jdbc:sqlite:$sqlitePath"
        }
        val poolOptions = PoolOptions().apply { maxSize = 5 }
        mJdbcPool = JDBCPool.pool(vertx, jdbcConnectOptions, poolOptions)

        mSqlSyncRepository = SqlSyncRepository(mJdbcPool)
        mTransactionParser = TransactionParser(mJdbcPool)
    }

    private suspend fun dbSync(message: Message<String>) {
        logger.info("Start db sync")

        val block = mSqlSyncRepository.findTopOrderByIdDesc()?.let {
            val lastBlock = mBlockManager.lastBlock()
            if (lastBlock?.hash == it.hash) {
                logger.info("DB sync finished")
                return
            }
            logger.info("Syncing block, hash: ${it.hash}")
            mBlockManager.nextBlock(it.hash)
        } ?: mBlockManager.firstBlock()
        block?.let {
            execute(it)
            vertx.eventBus().publish(DB_SYNC, "")
        }
    }

    private suspend fun execute(block: Block) {
        val transactions = block.blockBody.transactions
        transactions.forEach {
            it.base.oldJson = it.json
        }
        doSqlParse(transactions)

        val sqlSync = SqlSync(
            hash = block.hash,
            createTime = System.currentTimeMillis() / 1000
        )
        mSqlSyncRepository.save(sqlSync)
    }

    private suspend fun doSqlParse(transactions: List<Transaction>) {
        transactions.forEach {
            mTransactionParser.parse(it.base)
        } 
    }
}