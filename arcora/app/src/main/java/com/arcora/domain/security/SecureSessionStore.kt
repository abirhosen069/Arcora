package com.arcora.domain.security

interface SecureSessionStore {
    suspend fun save(token: SessionToken)
    suspend fun read(): SessionToken?
    suspend fun clear()
}
