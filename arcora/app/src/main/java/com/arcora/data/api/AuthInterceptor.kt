package com.arcora.data.api

import com.arcora.data.local.AccessTokenProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val accessTokenProvider: AccessTokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = accessTokenProvider.getAccessToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
