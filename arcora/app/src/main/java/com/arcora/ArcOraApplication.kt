package com.arcora

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid

@HiltAndroidApp
class ArcOraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val sentryDsn = BuildConfig.SENTRY_DSN
        if (sentryDsn.isNotBlank()) {
            SentryAndroid.init(this) { options ->
                options.dsn = sentryDsn
                options.environment = if (BuildConfig.DEBUG) "development" else "production"
                options.tracesSampleRate = 0.1
            }
        }
    }
}
