package com.huns.common

import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface

fun UUID() = java.util.UUID.randomUUID().toString().replace("\\-".toRegex(), "")

fun <T> CoroutineVerticle.handleMessage(
    message: Message<T>,
    errorCode: Int,
    func: suspend (message: Message<T>) -> Unit
) {
    launch {
        try {
            func(message)
        } catch (e: Exception) {
            message.fail(errorCode, e.message)
        }
    }
}

fun getAddress(ip: String, port: Int) = "$ip:$port"

fun getIp() = InetAddress.getLocalHost().hostAddress

fun getLocalIp() = getLocalHostLANAddress()?.hostAddress

fun getLocalHostLANAddress(): InetAddress? {
    try {
        var candidateAddress: InetAddress? = null
        val ifaces = NetworkInterface.getNetworkInterfaces()
        while (ifaces.hasMoreElements()) {
            val iface = ifaces.nextElement() as NetworkInterface
            val inetAddrs = iface.inetAddresses
            while (inetAddrs.hasMoreElements()) {
                val inetAddr = inetAddrs.nextElement() as InetAddress
                if (!inetAddr.isLoopbackAddress) {
                    if (inetAddr.isSiteLocalAddress) {
                        return inetAddr
                    } else if (candidateAddress == null) {
                        candidateAddress = inetAddr
                    }
                }
            }
        }
        return candidateAddress ?: InetAddress.getLocalHost()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}