package com.arcora.notifications

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.RegisterPushTokenRequest
import com.arcora.data.api.RemovePushTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushTokenRegistrar @Inject constructor(
    private val api: ArcOraApi
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentToken: String? = null

    fun registerToken(token: String) {
        if (token == currentToken) return
        currentToken = token
        val request = RegisterPushTokenRequest(token = token, platform = "android")
        scope.launch {
            runCatching { api.registerPushToken(request) }
        }
    }

    fun removeToken() {
        val token = currentToken ?: return
        currentToken = null
        val request = RemovePushTokenRequest(token = token)
        scope.launch {
            runCatching { api.removePushToken(request) }
        }
    }
}
