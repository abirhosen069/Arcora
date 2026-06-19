package com.arcora.domain.subscription

import com.arcora.domain.model.Money

data class SubscriptionPlan(
    val id: String,
    val merchantName: String,
    val amount: Money,
    val interval: String,
    val nextChargeLabel: String,
    val status: String
)
