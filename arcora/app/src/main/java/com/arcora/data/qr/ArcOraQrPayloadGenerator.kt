package com.arcora.data.qr

import com.arcora.domain.qr.PaymentQrPayload
import com.arcora.domain.qr.PaymentQrPayloadGenerator
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArcOraQrPayloadGenerator @Inject constructor() : PaymentQrPayloadGenerator {
    override fun staticReceive(username: String, smartAccountAddress: String): String =
        buildPayload(
            "username" to username.removePrefix("@"),
            "address" to smartAccountAddress,
            "chain" to "Arc_Testnet",
            "token" to "USDC"
        )

    override fun paymentRequest(payload: PaymentQrPayload): String = buildPayload(
        "username" to payload.username.removePrefix("@"),
        "address" to payload.smartAccountAddress,
        "chain" to payload.chain,
        "token" to payload.token,
        "amount" to payload.amount?.amount?.toPlainString(),
        "memo" to payload.memo,
        "requestId" to payload.requestId
    )

    private fun buildPayload(vararg params: Pair<String, String?>): String {
        return params
            .mapNotNull { (key, value) -> value?.let { "$key=${it.urlEncode()}" } }
            .joinToString(separator = "&", prefix = "arcora://pay?")
    }

    private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())
}
