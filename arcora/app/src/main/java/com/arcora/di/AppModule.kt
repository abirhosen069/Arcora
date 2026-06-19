package com.arcora.di

import com.arcora.data.repository.ApiAuthRepository
import com.arcora.data.repository.ApiWalletRepository
import com.arcora.data.local.EncryptedSessionStore
import com.arcora.data.ai.ApiAiAssistantRepository
import com.arcora.data.compliance.ApiCompliancePolicy
import com.arcora.data.qr.ArcOraQrPayloadGenerator
import com.arcora.data.security.BiometricTransactionAuthorizer
import com.arcora.domain.compliance.CompliancePolicy
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.WalletRepository
import com.arcora.domain.qr.PaymentQrPayloadGenerator
import com.arcora.domain.ai.AiAssistantRepository
import com.arcora.domain.security.SecureSessionStore
import com.arcora.domain.security.TransactionAuthorizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: ApiAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(repository: ApiWalletRepository): WalletRepository

    @Binds
    @Singleton
    abstract fun bindSecureSessionStore(store: EncryptedSessionStore): SecureSessionStore

    @Binds
    @Singleton
    abstract fun bindTransactionAuthorizer(authorizer: BiometricTransactionAuthorizer): TransactionAuthorizer

    @Binds
    @Singleton
    abstract fun bindCompliancePolicy(policy: ApiCompliancePolicy): CompliancePolicy

    @Binds
    @Singleton
    abstract fun bindPaymentQrPayloadGenerator(generator: ArcOraQrPayloadGenerator): PaymentQrPayloadGenerator

    @Binds
    @Singleton
    abstract fun bindAiAssistantRepository(repository: ApiAiAssistantRepository): AiAssistantRepository
}