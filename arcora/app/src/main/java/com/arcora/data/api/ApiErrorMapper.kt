package com.arcora.data.api

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object ApiErrorMapper {
    private val gson = Gson()

    private fun HttpException.extractBackendMessage(): String? {
        return try {
            val body = response()?.errorBody()?.string()
            if (body.isNullOrBlank()) null
            else {
                val parsed = gson.fromJson(body, Map::class.java)
                parsed["message"] as? String
            }
        } catch (_: Exception) {
            null
        }
    }

    fun message(error: Throwable): String = when (error) {
        is SocketTimeoutException -> "The ArcOra backend timed out. Check your connection and try again."
        is IOException -> "Cannot reach the ArcOra backend. Confirm the API server is running and your network is available."
        is HttpException -> {
            val backendMsg = error.extractBackendMessage()
            when (error.code()) {
                400 -> backendMsg ?: "The request was not accepted. Check the entered details and try again."
                401 -> backendMsg ?: "Your ArcOra session expired. Sign in again to continue."
                403 -> backendMsg ?: "This action is not allowed for the current ArcOra session."
                404 -> backendMsg ?: "No matching ArcOra record was found."
                409 -> backendMsg ?: "This ArcOra record already exists. Try signing in or choose different details."
                in 500..599 -> backendMsg ?: "ArcOra backend is temporarily unavailable. Try again shortly."
                else -> backendMsg ?: "ArcOra API error ${error.code()}. Try again."
            }
        }
        else -> error.message ?: "Something went wrong. Try again."
    }
}

inline fun <T> mapApiErrors(block: () -> T): T = try {
    block()
} catch (error: Throwable) {
    throw IllegalStateException(ApiErrorMapper.message(error), error)
}
