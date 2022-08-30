package com.huns.chain.web

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

        router.post("/transaction/submit")
            .handler(BodyHandler.create()).handler { handleCtx(it) { submitTransaction(it) } }
        router.post("/transaction/query")
            .handler(BodyHandler.create()).handler { handleCtx(it) { queryTransaction(it) } }

        router.post("/block/insert")
            .handler(BodyHandler.create()).handler { handleCtx(it) { insertBlock(it) } }
        router.get("/block/last").handler { handleCtx(it) { lastBlock(it) } }
        router.get("/block/query").handler { handleCtx(it) { queryBlock(it) } }
        router.get("/block/query/next").handler { handleCtx(it) { queryNextBlock(it) } }

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