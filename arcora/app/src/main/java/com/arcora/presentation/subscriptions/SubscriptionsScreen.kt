package com.arcora.presentation.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcora.data.api.SubscriptionResponse
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.ArcOraStatusCard
import com.arcora.presentation.components.ArcOraTextField
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted

@Composable
fun SubscriptionsScreen(
    onDone: () -> Unit,
    viewModel: SubscriptionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("Subscriptions", "Approve recurring USDC payments with clear limits.")
        Spacer(Modifier.height(24.dp))
        ArcOraCard(elevated = true) {
            Text("Create recurring payment", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            ArcOraTextField(state.amount, viewModel::onAmountChange, "Amount in USDC")
            Spacer(Modifier.height(10.dp))
            ArcOraTextField(state.interval, viewModel::onIntervalChange, "Interval")
            Spacer(Modifier.height(14.dp))
            ArcOraPrimaryButton("Create demo subscription", viewModel::createDemoSubscription, loading = state.isLoading)
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            Text("Active plans", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (state.subscriptions.isEmpty()) {
                Text("No subscriptions yet. Create one to test lifecycle controls.", color = ArcoraMuted)
            } else {
                state.subscriptions.forEach { subscription ->
                    SubscriptionRow(subscription, viewModel)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        state.result?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = ArcoraGreen)
        }
        state.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            ArcOraStatusCard(
                title = "Subscription API unavailable",
                message = error,
                actionLabel = "Retry refresh",
                onAction = viewModel::refresh,
                loading = state.isLoading
            )
        }
        Spacer(Modifier.weight(1f))
        ArcOraPrimaryButton("Back", onDone)
    }
}

@Composable
private fun SubscriptionRow(subscription: SubscriptionResponse, viewModel: SubscriptionsViewModel) {
    KeyValueRow("Plan", subscription.merchantId ?: subscription.agentWalletId ?: subscription.id.take(8), ArcoraGreen)
    Spacer(Modifier.height(6.dp))
    KeyValueRow("Amount", "${subscription.amount} ${subscription.token} / ${subscription.interval}")
    Spacer(Modifier.height(6.dp))
    KeyValueRow("Status", subscription.status)
    subscription.nextChargeAt?.let {
        Spacer(Modifier.height(6.dp))
        Text("Next charge: $it", color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ArcOraPrimaryButton("Pause", { viewModel.pause(subscription.id) }, Modifier.weight(1f))
        ArcOraPrimaryButton("Renew", { viewModel.renew(subscription.id) }, Modifier.weight(1f))
        ArcOraPrimaryButton("Cancel", { viewModel.cancel(subscription.id) }, Modifier.weight(1f))
    }
}
