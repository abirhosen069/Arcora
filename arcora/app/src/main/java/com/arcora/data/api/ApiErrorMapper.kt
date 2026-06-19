package com.arcora.data.api

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object ApiErrorMapper {
    fun message(error: Throwable): String = when (error) {
        is SocketTimeoutException -> "The ArcOra backend timed out. Check your connection and try again."
        is IOException -> "Cannot reach the ArcOra backend. Confirm the API server is running and your network is available."
        is HttpException -> when (error.code()) {
            400 -> "The request was not accepted. Check the entered details and try again."
            401 -> "Your ArcOra session expired. Sign in again to continue."
            403 -> "This action is not allowed for the current ArcOra session."
            404 -> "No matching ArcOra record was found."
            409 -> "This ArcOra record already exists. Try signing in or choose different details."
            in 500..599 -> "ArcOra backend is temporarily unavailable. Try again shortly."
            else -> "ArcOra API error ${error.code()}. Try again."
        }
        else -> error.message ?: "Something went wrong. Try again."
    }
}

inline fun <T> mapApiErrors(block: () -> T): T = try {
    block()
} catch (error: Throwable) {
    throw IllegalStateException(ApiErrorMapper.message(error), error)
}
