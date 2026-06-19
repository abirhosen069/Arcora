package com.arcora.presentation.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.ArcOraStatusCard
import com.arcora.presentation.components.ArcOraTextField
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted
import com.arcora.presentation.theme.ArcoraWarning

@Composable
fun MerchantDashboardScreen(
    onDone: () -> Unit,
    viewModel: MerchantDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("Merchant Suite", "Stablecoin checkout, QR links, settlement, and analytics.")
        Spacer(Modifier.height(24.dp))
        ArcOraCard(elevated = true) {
            KeyValueRow("Handle", state.merchant?.merchantHandle ?: "Not created", if (state.merchant != null) ArcoraGreen else ArcoraMuted)
            Spacer(Modifier.height(10.dp))
            KeyValueRow("Today volume", "${state.dashboard?.dailyVolume ?: "0.00"} USDC")
            Spacer(Modifier.height(10.dp))
            KeyValueRow("Weekly volume", "${state.dashboard?.weeklyVolume ?: "0.00"} USDC")
            Spacer(Modifier.height(10.dp))
            KeyValueRow("Monthly volume", "${state.dashboard?.monthlyVolume ?: "0.00"} USDC")
            Spacer(Modifier.height(10.dp))
            KeyValueRow("Recent tx", "${state.dashboard?.recentTransactions?.size ?: 0}")
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            Text("Checkout link", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            ArcOraTextField(state.checkoutAmount, viewModel::onCheckoutAmountChange, "Amount in USDC")
            Spacer(Modifier.height(10.dp))
            ArcOraTextField(state.checkoutMemo, viewModel::onCheckoutMemoChange, "Memo")
            Spacer(Modifier.height(14.dp))
            ArcOraPrimaryButton("Create merchant", viewModel::createDemoMerchant, loading = state.isLoading && state.merchant == null)
            Spacer(Modifier.height(10.dp))
            ArcOraPrimaryButton("Create checkout link", viewModel::createCheckoutLink, loading = state.isLoading && state.merchant != null)
        }

        state.checkout?.let { checkout ->
            Spacer(Modifier.height(18.dp))
            ArcOraCard {
                Text("Checkout ready", fontWeight = FontWeight.Bold, color = ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Amount", "${checkout.amount} ${checkout.token}")
                Spacer(Modifier.height(8.dp))
                Text(checkout.checkoutUrl, color = ArcoraMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(checkout.payload, color = ArcoraMuted, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }

        state.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            ArcOraStatusCard(
                title = "Merchant API unavailable",
                message = error,
                actionLabel = if (state.merchant == null) "Retry merchant setup" else "Retry dashboard",
                onAction = if (state.merchant == null) viewModel::createDemoMerchant else viewModel::refreshDashboard,
                loading = state.isLoading
            )
        }
        Spacer(Modifier.weight(1f))
        ArcOraPrimaryButton("Back", onDone)
    }
}
