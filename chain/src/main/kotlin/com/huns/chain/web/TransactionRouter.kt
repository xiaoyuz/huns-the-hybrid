package com.huns.chain.web

import com.huns.chain.block.model.Block
import com.huns.common.bean.ApiResponse
import com.huns.common.TransactionHelper
import com.huns.common.model.TransactionBody
import com.huns.chain.common.bean.TransactionQueryResult
import com.huns.chain.core.TRANSACTION_QUERY_BLOCK
import com.huns.chain.core.TRANSACTION_SUBMIT_POOL
import com.huns.common.exception.Errors
import com.huns.common.exception.KeyException
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await

suspend fun submitTransaction(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val transactionBody = ctx.body().asJsonObject().mapTo(TransactionBody::class.java)
    val transaction = TransactionHelper.buildTransaction(transactionBody)
    val response = if (!TransactionHelper.checkSign(transaction)) {
        throw KeyException(Errors.SIGN_ERROR)
    } else if (!TransactionHelper.checkContent(transaction)) {
        throw KeyException(Errors.INVALID_CONTENT.apply {
            this.message = "Invalid content, DELETE and UPDATE must with id and json"
        })
    } else {
        vertx.eventBus().request<Block>(TRANSACTION_SUBMIT_POOL, transaction).await()
        ApiResponse.success(transaction)
    }
    ctx.response().end(Json.encode(response))
}

suspend fun queryTransaction(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val hash = ctx.queryParam("hash").firstOrNull() ?: ""
    val block = vertx.eventBus().request<Block>(TRANSACTION_QUERY_BLOCK, hash).await().body()
    val response = block.blockBody.transactions.firstOrNull { it.hash == hash }?.let {
        TransactionQueryResult(transaction = it, blockHash = block.hash).let { ApiResponse.success(it) }
    } ?: throw KeyException(Errors.INVALID_PARAM_ERROR)
    ctx.response().end(Json.encode(response))
}