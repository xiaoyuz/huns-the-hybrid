package com.huns.chain

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await

class MainVerticle : CoroutineVerticle() {

    override suspend fun start() {
        val envConfigJson = loadEnvConfig(vertx)
        val configJson = Vertx.currentContext().config()

        val managerHost = envConfigJson.getString("MANAGER_HOST")
        val nodeName = envConfigJson.getString("NODE_NAME")
        val nodeAppId = envConfigJson.getString("NODE_APP_ID")

        if (managerHost?.isEmpty() == false) {
            configJson.put("manager_host", managerHost)
        }
        if (nodeName?.isEmpty() == false) {
            configJson.put("node_name", nodeName)
        }
        if (nodeAppId?.isEmpty() == false) {
            configJson.put("node_app_id", nodeAppId)
        }

        deployVerticles(vertx, configJson)
    }
}

// For debugging
suspend fun main() {
    val vertx = Vertx.vertx()
    val configJson = loadFileConfig(vertx)
    deployVerticles(vertx, configJson)
}

private suspend fun loadEnvConfig(vertx: Vertx): JsonObject {
    val envRetriever = ConfigRetriever.create(vertx,
        ConfigRetrieverOptions()
            .addStore(
                ConfigStoreOptions()
                    .setType("env")
                    .setConfig(JsonObject().put("raw-data", true)
                    )
            )
    )
    return envRetriever.config.await()
}

private suspend fun loadFileConfig(vertx: Vertx): JsonObject {
    val retriever = ConfigRetriever.create(vertx,
        ConfigRetrieverOptions().addStore(
            ConfigStoreOptions().setType("file").setConfig(
                JsonObject().put("path", "conf/prod/config.json")
            )
        )
    )
    return retriever.config.await()
}

private suspend fun deployVerticles(vertx: Vertx, config: JsonObject) {
    vertx.deployVerticle(
        "com.huns.chain.web.WebVerticle", DeploymentOptions().setConfig(config)
    ).await()
    vertx.deployVerticle(
        "com.huns.chain.storage.StorageVerticle", DeploymentOptions().setConfig(config)
    ).await()
    vertx.deployVerticle(
        "com.huns.chain.block.BlockVerticle", DeploymentOptions().setConfig(config)
    ).await()
    vertx.deployVerticle(
        "com.huns.chain.permission.FetcherVerticle", DeploymentOptions().setConfig(config)
    ).await()
    vertx.deployVerticle(
        "com.huns.chain.p2p.P2PVerticle", DeploymentOptions().setConfig(config)
    ).await()
    vertx.deployVerticle(
        "com.huns.chain.pbft.PbftVerticle", DeploymentOptions().setConfig(config)
    ).await()
    vertx.deployVerticle(
        "com.huns.chain.transaction.TransactionVerticle", DeploymentOptions().setConfig(config)
    ).await()
}