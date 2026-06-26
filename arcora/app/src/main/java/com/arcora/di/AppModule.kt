package com.arcora.di

import com.arcora.BuildConfig
import com.arcora.data.repository.ApiAuthRepository
import com.arcora.data.repository.ApiWalletRepository
import com.arcora.data.repository.ApiReputationRepository
import com.arcora.data.repository.ApiProfileRepository
import com.arcora.data.repository.ApiAgentWalletRepository
import com.arcora.data.repository.ApiAgentMarketplaceRepository
import com.arcora.data.repository.ApiMerchantRepository
import com.arcora.data.repository.ApiSubscriptionRepository
import com.arcora.data.local.EncryptedSessionStore
import com.arcora.data.ai.ApiAiAssistantRepository
import com.arcora.data.compliance.ApiCompliancePolicy
import com.arcora.data.compliance.MockCompliancePolicy
import com.arcora.data.qr.ArcOraQrPayloadGenerator
import com.arcora.data.security.BiometricTransactionAuthorizer
import com.arcora.domain.compliance.CompliancePolicy
import com.arcora.domain.repository.AuthRepository
import com.arcora.domain.repository.WalletRepository
import com.arcora.domain.repository.ReputationRepository
import com.arcora.domain.repository.ProfileRepository
import com.arcora.domain.repository.AgentWalletRepository
import com.arcora.domain.repository.AgentMarketplaceRepository
import com.arcora.domain.repository.MerchantRepository
import com.arcora.domain.repository.SubscriptionRepository
import com.arcora.domain.qr.PaymentQrPayloadGenerator
import com.arcora.domain.ai.AiAssistantRepository
import com.arcora.domain.security.SecureSessionStore
import com.arcora.domain.security.TransactionAuthorizer
import dagger.Binds
import dagger.Module
import dagger.Provides
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
    abstract fun bindPaymentQrPayloadGenerator(generator: ArcOraQrPayloadGenerator): PaymentQrPayloadGenerator

    @Binds
    @Singleton
    abstract fun bindAiAssistantRepository(repository: ApiAiAssistantRepository): AiAssistantRepository

    @Binds
    @Singleton
    abstract fun bindReputationRepository(repository: ApiReputationRepository): ReputationRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(repository: ApiProfileRepository): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindAgentWalletRepository(repository: ApiAgentWalletRepository): AgentWalletRepository

    @Binds
    @Singleton
    abstract fun bindAgentMarketplaceRepository(repository: ApiAgentMarketplaceRepository): AgentMarketplaceRepository

    @Binds
    @Singleton
    abstract fun bindMerchantRepository(repository: ApiMerchantRepository): MerchantRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(repository: ApiSubscriptionRepository): SubscriptionRepository

    companion object {
        @Provides
        @Singleton
        fun provideCompliancePolicy(apiPolicy: ApiCompliancePolicy, mockPolicy: MockCompliancePolicy): CompliancePolicy {
            return if (BuildConfig.DEBUG) mockPolicy else apiPolicy
        }
    }
}
