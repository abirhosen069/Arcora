package com.arcora.presentation.agentwallets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcora.data.api.AgentWalletResponse
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
fun AgentWalletsScreen(
    onDone: () -> Unit,
    viewModel: AgentWalletsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("Agent Wallets", "Create and manage sub-wallets with spending limits for AI agents.")
        Spacer(Modifier.height(24.dp))

        ArcOraCard(elevated = true) {
            Text("Create agent wallet", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            ArcOraTextField(state.name, viewModel::onNameChange, "Agent name")
            Spacer(Modifier.height(10.dp))
            ArcOraTextField(state.description, viewModel::onDescriptionChange, "Description (optional)")
            Spacer(Modifier.height(10.dp))
            ArcOraTextField(state.monthlyBudget, viewModel::onBudgetChange, "Monthly budget in USDC")
            Spacer(Modifier.height(14.dp))
            ArcOraPrimaryButton("Create wallet", viewModel::createWallet, loading = state.isLoading && state.wallets.isEmpty())
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            Text("Your agent wallets", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (state.wallets.isEmpty()) {
                Text("No agent wallets yet. Create one to delegate spending to an AI agent.", color = ArcoraMuted)
            } else {
                state.wallets.forEach { wallet ->
                    AgentWalletRow(wallet, viewModel)
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
                title = "Agent wallet error",
                message = error,
                actionLabel = "Retry",
                onAction = viewModel::refresh,
                loading = state.isLoading
            )
        }
        Spacer(Modifier.weight(1f))
        ArcOraPrimaryButton("Back", onDone)
    }
}

@Composable
private fun AgentWalletRow(wallet: AgentWalletResponse, viewModel: AgentWalletsViewModel) {
    KeyValueRow(wallet.name, "${wallet.monthlyBudget} USDC/mo", ArcoraGreen)
    Spacer(Modifier.height(6.dp))
    wallet.description?.let { Text(it, color = ArcoraMuted, style = MaterialTheme.typography.bodySmall) }
    Spacer(Modifier.height(6.dp))
    KeyValueRow("Address", wallet.walletAddress.take(20) + "...", ArcoraMuted)
    Spacer(Modifier.height(6.dp))
    KeyValueRow("Permissions", wallet.permissions.size.toString())
    Spacer(Modifier.height(10.dp))
    ArcOraPrimaryButton("Delete", { viewModel.deleteWallet(wallet.id) })
}
