package com.arcora.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class Money(
    val amount: BigDecimal,
    val currency: String = "USDC"
) {
    fun formatted(): String = "${amount.setScale(2, RoundingMode.HALF_UP).toPlainString()} $currency"

    companion object {
        fun usdc(value: String): Money = Money(BigDecimal(value), "USDC")
        fun eurc(value: String): Money = Money(BigDecimal(value), "EURC")
    }
}
