package com.arcora.domain.usecase

import com.arcora.domain.repository.WalletRepository
import javax.inject.Inject

class ObserveDashboardUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    fun portfolio() = walletRepository.observePortfolio()
    fun activity() = walletRepository.observeActivity()
    suspend fun refreshPortfolio(smartAccountAddress: String) = walletRepository.refreshPortfolio(smartAccountAddress)
}
