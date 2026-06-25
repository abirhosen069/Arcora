package com.arcora.presentation.bridge

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

@Composable
fun BridgeScreen(
    onDone: () -> Unit,
    viewModel: BridgeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("One-tap bridge", "Move USDC into Arc without exposing burn, attestation, or mint steps.")
        Spacer(Modifier.height(24.dp))
        ArcOraCard(elevated = true) {
            ArcOraTextField(state.sourceChain, viewModel::onSourceChainChange, "Source chain")
            Spacer(Modifier.height(12.dp))
            ArcOraTextField(state.amount, viewModel::onAmountChange, "Amount in USDC")
            Spacer(Modifier.height(18.dp))
            ArcOraPrimaryButton("Preview route", viewModel::previewRoute, loading = state.isLoading && state.quote == null)
        }
        state.quote?.let { quote ->
            Spacer(Modifier.height(18.dp))
            ArcOraCard {
                KeyValueRow("Route", quote.routeSummary)
                Spacer(Modifier.height(10.dp))
                KeyValueRow("Estimated time", quote.estimatedTime)
                Spacer(Modifier.height(10.dp))
                KeyValueRow("Destination", quote.destinationChain, ArcoraGreen)
                quote.fee?.let { fee ->
                    Spacer(Modifier.height(10.dp))
                    KeyValueRow("Estimated fee", fee)
                }
                Spacer(Modifier.height(18.dp))
                ArcOraPrimaryButton("Bridge to Arc", viewModel::executeBridge, loading = state.isLoading)
            }
        }
        Spacer(Modifier.height(14.dp))
        state.error?.let { error ->
            ArcOraStatusCard(
                title = "Bridge route unavailable",
                message = error,
                actionLabel = if (state.quote == null) "Retry quote" else "Retry bridge",
                onAction = if (state.quote == null) viewModel::previewRoute else viewModel::executeBridge,
                loading = state.isLoading
            )
        }
        state.result?.let {
            Text(it, color = ArcoraGreen)
            Spacer(Modifier.height(12.dp))
            ArcOraPrimaryButton("Done", onDone)
        }
        Spacer(Modifier.height(16.dp))
        Text("Powered by Arc App Kit bridge primitives and Circle CCTP concepts.", color = ArcoraMuted)
    }
}
