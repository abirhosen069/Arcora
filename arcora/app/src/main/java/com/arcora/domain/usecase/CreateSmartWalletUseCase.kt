package com.arcora.domain.usecase

import com.arcora.domain.repository.AuthRepository
import javax.inject.Inject

class CreateSmartWalletUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, displayName: String) =
        authRepository.createSmartWalletWithEmail(email = email, displayName = displayName)
}
