package com.arcora.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.arcora.domain.security.SecureSessionStore
import com.arcora.domain.security.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedSessionStore @Inject constructor(
    @ApplicationContext context: Context
) : SecureSessionStore {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "arcora_secure_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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
