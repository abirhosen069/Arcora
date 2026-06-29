package com.arcora

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import io.sentry.SentryOptions

@HiltAndroidApp
class ArcOraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initSentry()
    }

    private fun initSentry() {
        try {
            val sentryDsn = BuildConfig.SENTRY_DSN
            if (sentryDsn.isNotBlank()) {
                SentryAndroid.init(this) { options ->
                    options.dsn = sentryDsn
                    options.environment = if (BuildConfig.DEBUG) "development" else "production"
                    options.tracesSampleRate = 0.1
                }
            }
        } catch (e: Exception) {
            Log.w("ArcOra", "Sentry init skipped: ${e.message}")
        }
    }
}
