package com.huns.wallet.web

import com.huns.chain.common.genPairKey
import com.huns.common.bean.ApiResponse
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext

fun randomPairKey(ctx: RoutingContext) {
    val pairKey = genPairKey()
    ctx.response().end(Json.encode(ApiResponse.success(pairKey)))
}