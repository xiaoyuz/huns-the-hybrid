package com.huns.wallet.web

import com.huns.common.TransactionHelper
import com.huns.common.bean.ApiResponse
import com.huns.common.bean.GenTransactionBody
import com.huns.common.exception.Errors
import com.huns.common.exception.KeyException
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext

fun computeTx(ctx: RoutingContext) {
    val genTransactionBody = ctx.body().asJsonObject().mapTo(GenTransactionBody::class.java)
    val response = if (!TransactionHelper.checkKeyPair(genTransactionBody)) {
        throw KeyException(Errors.INVALID_KEY_PAIR)
    } else {
        ApiResponse.success(TransactionHelper.computeTransactionBody(genTransactionBody))
    }
    ctx.response().end(Json.encode(response))
}