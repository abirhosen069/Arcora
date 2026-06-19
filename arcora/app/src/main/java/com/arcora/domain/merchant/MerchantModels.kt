package com.arcora.domain.merchant

import com.arcora.domain.model.Money

data class MerchantSummary(
    val merchantHandle: String,
    val dailyVolume: Money,
    val pendingSettlement: Money,
    val checkoutLinks: Int,
    val disputeRateLabel: String
)
