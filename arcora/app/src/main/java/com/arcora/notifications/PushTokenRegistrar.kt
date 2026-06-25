package com.arcora.notifications

import com.arcora.data.api.ArcOraApi
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

        scope.launch {
            runCatching {
                api.registerPushToken(
                    com.arcora.data.api.RegisterPushTokenRequest(
                        token = token,
                        platform = "android"
                    )
                }
            }
        }
    }

    fun removeToken() {
        val token = currentToken ?: return
        currentToken = null

        scope.launch {
            runCatching {
                api.removePushToken(
                    com.arcora.data.api.RemovePushTokenRequest(token = token)
                )
            }
        }
    }
}
