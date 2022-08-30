package com.huns.wallet.web

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

class WebVerticle : CoroutineVerticle() {

    override suspend fun start() {
        val router = Router.router(vertx)
        val httpPort = config.getInteger("http_port")

        router.get("/pairKey/random").handler { handleCtx(it) { randomPairKey(it) } }

        router.post("/wallet/compute_tx")
            .handler(BodyHandler.create()).handler { handleCtx(it) { computeTx(it) } }

        vertx.createHttpServer().requestHandler(router).listen(httpPort).await()
    }

    private fun handleCtx(ctx: RoutingContext, func: suspend (ctx: RoutingContext) -> Unit) {
        launch {
            try {
                func(ctx)
            } catch (e: Exception) {
                ctx.response().end("Server error: ${e.message}")
            }
        }
    }
}