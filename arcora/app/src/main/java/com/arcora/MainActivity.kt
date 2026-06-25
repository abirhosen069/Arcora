package com.arcora

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.arcora.data.security.ArcOraActivityHolder
import com.arcora.presentation.ArcOraApp
import com.arcora.presentation.theme.ArcOraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcOraTheme {
                ArcOraApp(initialDeepLink = parseDeepLink(intent))
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        ArcOraActivityHolder.setCurrentActivity(this)
    }

    override fun onPause() {
        ArcOraActivityHolder.clearCurrentActivity(this)
        super.onPause()
    }

    private fun parseDeepLink(intent: Intent): DeepLinkData? {
        val uri = intent.data ?: return null
        if (uri.scheme != "arcora") return null

        return when (uri.host) {
            "pay" -> DeepLinkData.Send(
                recipient = uri.getQueryParameter("to") ?: uri.getQueryParameter("recipient"),
                amount = uri.getQueryParameter("amount"),
                note = uri.getQueryParameter("note")
            )
            "checkout" -> DeepLinkData.Checkout(
                merchantId = uri.getQueryParameter("merchantId"),
                amount = uri.getQueryParameter("amount"),
                memo = uri.getQueryParameter("memo"),
                reference = uri.getQueryParameter("ref")
            )
            else -> null
        }
    }
}

sealed class DeepLinkData {
    data class Send(
        val recipient: String?,
        val amount: String?,
        val note: String?
    ) : DeepLinkData()

    data class Checkout(
        val merchantId: String?,
        val amount: String?,
        val memo: String?,
        val reference: String?
    ) : DeepLinkData()
}
