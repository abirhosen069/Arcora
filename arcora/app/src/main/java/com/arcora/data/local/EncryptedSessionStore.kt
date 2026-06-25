package com.arcora.data.local

import android.content.SharedPreferences
import com.arcora.domain.security.SecureSessionStore
import com.arcora.domain.security.SessionToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedSessionStore @Inject constructor(
    private val prefs: SharedPreferences
) : SecureSessionStore {

    override suspend fun save(token: SessionToken) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token.accessToken)
            .putString(KEY_REFRESH_TOKEN, token.refreshToken)
            .putLong(KEY_EXPIRES_AT, token.expiresAtEpochMillis ?: -1L)
            .apply()
    }

    override suspend fun read(): SessionToken? {
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, -1L).takeIf { it > 0 }
        return SessionToken(
            accessToken = accessToken,
            refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null),
            expiresAtEpochMillis = expiresAt
        )
    }

    override suspend fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
