package com.arcora.data.notifications

import com.arcora.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class AppNotification(
    val title: String,
    val message: String,
    val type: String
)

@Singleton
class NotificationService @Inject constructor() {
    private var socket: Socket? = null
    private val _notifications = MutableSharedFlow<AppNotification>(extraBufferCapacity = 16)
    val notifications = _notifications.asSharedFlow()

    private val onNotification = Emitter.Listener { args ->
        try {
            val data = args[0] as? JSONObject ?: return@Listener
            val notification = AppNotification(
                title = data.optString("title", "Notification"),
                message = data.optString("message", ""),
                type = data.optString("type", "info")
            )
            _notifications.tryEmit(notification)
        } catch (_: Exception) {}
    }

    private val onTransactionUpdate = Emitter.Listener { args ->
        try {
            val data = args[0] as? JSONObject ?: return@Listener
            _notifications.tryEmit(AppNotification(
                title = "Transaction Update",
                message = "Status: ${data.optString("status", "unknown")}",
                type = "transaction"
            ))
        } catch (_: Exception) {}
    }

    private val onPaymentRequest = Emitter.Listener { args ->
        try {
            val data = args[0] as? JSONObject ?: return@Listener
            _notifications.tryEmit(AppNotification(
                title = "Payment Request",
                message = "You have a new payment request for ${data.optString("amount", "0")} USDC",
                type = "payment_request"
            ))
        } catch (_: Exception) {}
    }

    fun connect(userId: String) {
        try {
            val options = IO.Options.builder()
                .setReconnection(true)
                .setReconnectionAttempts(5)
                .setReconnectionDelay(1000)
                .build()

            socket = IO.socket("${BuildConfig.API_BASE_URL}notifications", options)

            socket?.on(Socket.EVENT_CONNECT) {
                val authData = JSONObject().put("userId", userId)
                socket?.emit("auth", authData)
            }

            socket?.on("notification", onNotification)
            socket?.on("transaction:update", onTransactionUpdate)
            socket?.on("payment:request", onPaymentRequest)

            socket?.connect()
        } catch (_: Exception) {}
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}
