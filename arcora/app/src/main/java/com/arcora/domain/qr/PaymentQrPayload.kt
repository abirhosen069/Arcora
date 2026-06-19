package com.arcora.domain.qr

import com.arcora.domain.model.Money

data class PaymentQrPayload(
    val username: String,
    val smartAccountAddress: String,
    val chain: String = "Arc_Testnet",
    val token: String = "USDC",
    val amount: Money? = null,
    val memo: String? = null,
    val requestId: String? = null
)

interface PaymentQrPayloadGenerator {
    fun staticReceive(username: String, smartAccountAddress: String): String
    fun paymentRequest(payload: PaymentQrPayload): String
}
