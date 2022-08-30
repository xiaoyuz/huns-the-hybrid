package com.huns.chain.web

import com.huns.chain.block.model.Block
import com.huns.chain.block.model.BlockBody
import com.huns.chain.core.BLOCK_INSERT
import com.huns.chain.core.BLOCK_LAST_BLOCK
import com.huns.chain.core.BLOCK_QUERY_BLOCK
import com.huns.chain.core.BLOCK_QUERY_NEXT_BLOCK
import com.huns.common.bean.ApiResponse
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await

suspend fun insertBlock(ctx: RoutingContext) {
    val blockBody = ctx.body().asJsonObject().mapTo(BlockBody::class.java)
    val vertx = ctx.vertx()
    val block = vertx.eventBus().request<Block>(BLOCK_INSERT, blockBody).await().body()
    val response = ApiResponse.success(block)
    ctx.response().end(Json.encode(response))
}

suspend fun lastBlock(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val lastBlock = vertx.eventBus().request<Block>(BLOCK_LAST_BLOCK, "").await().body()
    val response = ApiResponse.success(lastBlock)
    ctx.response().end(Json.encode(response))
}

suspend fun queryBlock(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val hash = ctx.queryParam("hash").firstOrNull() ?: ""
    val block = vertx.eventBus().request<Block>(BLOCK_QUERY_BLOCK, hash).await().body()
    val response = ApiResponse.success(block)
    ctx.response().end(Json.encode(response))
}

suspend fun queryNextBlock(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val hash = ctx.queryParam("hash").firstOrNull() ?: ""
    val block = vertx.eventBus().request<Block>(BLOCK_QUERY_NEXT_BLOCK, hash).await().body()
    val response = ApiResponse.success(block)
    ctx.response().end(Json.encode(response))
}