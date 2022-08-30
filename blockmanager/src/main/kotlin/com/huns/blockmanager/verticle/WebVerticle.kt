package com.huns.blockmanager.verticle

import com.huns.blockmanager.data.repository.MemberRepository
import com.huns.blockmanager.data.repository.PermissionRepository
import com.huns.common.bean.ApiResponse
import com.huns.common.bean.MemberData
import com.huns.common.bean.PermissionData
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.launch

class WebVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(WebVerticle::class.java)

    private lateinit var mMysqlPool: MySQLPool

    private lateinit var mMemberRepository: MemberRepository
    private lateinit var mPermissionRepository: PermissionRepository

    override suspend fun start() {
        val mysqlConnectionOptions = MySQLConnectOptions().apply {
            host = config.getString("mysql_host")
            port = config.getInteger("mysql_port")
            database = config.getString("mysql_database")
            user = config.getString("mysql_user")
            password = config.getString("mysql_password")
        }
        val poolOptions = PoolOptions().apply { maxSize = 5 }
        mMysqlPool = MySQLPool.pool(vertx, mysqlConnectionOptions, poolOptions)

        mMemberRepository = MemberRepository(mMysqlPool)
        mPermissionRepository = PermissionRepository(mMysqlPool)

        val httpPort = config.getInteger("http_port")
        val router = Router.router(vertx)

        router.get("/member").handler { handleCtx(it) { handleMember(it) } }
        router.get("/permission").handler { handleCtx(it) { handlePermission(it) } }

        try {
            vertx.createHttpServer().requestHandler(router).listen(httpPort).await()
            logger.info("Web Server started on port $httpPort")
        } catch (e: Exception) {
            logger.error("Web Server start failed, $e")
        }


    }

    private suspend fun handleMember(ctx: RoutingContext) {
        logger.info("Request memeber")
        val name = ctx.queryParam("name").firstOrNull() ?: ""
        val appId = ctx.queryParam("appId").firstOrNull() ?: ""
        val address = ctx.queryParam("address").firstOrNull() ?: ""
        val members = mMemberRepository.findFirstByAppId(appId)?.let { member ->
            if (member.name == name && member.address == address) {
                mMemberRepository.findByName(name).firstOrNull()?.groupId?.let { groupId ->
                    mMemberRepository.findByGroupId(groupId)
                }
            } else emptyList()
        } ?: emptyList()
        ctx.response().end(Json.encode(ApiResponse.success(MemberData(members))))
    }

    private suspend fun handlePermission(ctx: RoutingContext) {
        val name = ctx.queryParam("name").firstOrNull() ?: ""
        val permissions = mMemberRepository.findByName(name).firstOrNull()?.groupId?.let { groupId ->
            mPermissionRepository.findByGroupId(groupId)
        } ?: emptyList()
        ctx.response().end(Json.encode(ApiResponse.success(PermissionData(permissions))))
    }

    private fun handleCtx(ctx: RoutingContext, func: suspend (ctx: RoutingContext) -> Unit) {
        launch {
            try {
                func(ctx)
            } catch (e: Exception) {
                ctx.response().end(Json.encode(ApiResponse.failed(-1, "Server error: ${e.message}")))
            }
        }
    }
}