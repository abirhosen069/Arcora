package com.arcora.presentation.dashboard

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcora.domain.repository.AuthRepository
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraStatusCard
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.Pill
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted

@Composable
fun DashboardScreen(
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onBridge: () -> Unit,
    onActivity: () -> Unit,
    onAssistant: () -> Unit,
    onMerchant: () -> Unit,
    onSubscriptions: () -> Unit,
    onAgents: () -> Unit,
    onAgentWallets: () -> Unit = {},
    onReputation: () -> Unit = {},
    onSettings: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val portfolio = state.portfolio
    val user = viewModel.currentUser

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                val greeting = when {
                    java.time.LocalTime.now().hour < 12 -> "Good morning"
                    java.time.LocalTime.now().hour < 18 -> "Good afternoon"
                    else -> "Good evening"
                }
                Text(greeting, color = ArcoraMuted)
                Text(
                    user?.displayName ?: "Your ArcOra",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                user?.username?.let { Text(it, color = ArcoraGreen, style = MaterialTheme.typography.bodyLarge) }
            }
            Pill("Arc_Testnet")
        }

        if (user != null) {
            Spacer(Modifier.height(12.dp))
            ArcOraCard {
                Text("Account", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Username", user.username, ArcoraGreen)
                Spacer(Modifier.height(6.dp))
                KeyValueRow("Address", user.smartAccountAddress.take(16) + "...", ArcoraMuted)
                Spacer(Modifier.height(6.dp))
                KeyValueRow("Reputation", "${user.reputationScore}/100", ArcoraGreen)
                Spacer(Modifier.height(6.dp))
                KeyValueRow("Status", if (user.isVerified) "Verified" else "Active", ArcoraGreen)
            }
        }
        Spacer(Modifier.height(24.dp))
        ArcOraCard(elevated = true) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total portfolio", color = ArcoraMuted)
                AssistChip(
                    onClick = viewModel::refreshLiveBalance,
                    label = { Text(if (state.isRefreshing) "Syncing" else "Live RPC") }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(portfolio?.totalBalance?.formatted() ?: "—", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text("Available on Arc: ${portfolio?.availableOnArc?.formatted() ?: "—"}", color = ArcoraGreen)
            state.liveBalanceError?.let { error ->
                Spacer(Modifier.height(12.dp))
                ArcOraStatusCard(
                    title = "Live balance unavailable",
                    message = error,
                    actionLabel = "Retry sync",
                    onAction = viewModel::refreshLiveBalance,
                    loading = state.isRefreshing
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton("Send", onSend, Modifier.weight(1f))
            ActionButton("Receive", onReceive, Modifier.weight(1f))
            ActionButton("Bridge", onBridge, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        ActionButton("Ask ArcOra AI", onAssistant, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton("Merchant", onMerchant, Modifier.weight(1f))
            ActionButton("Subs", onSubscriptions, Modifier.weight(1f))
            ActionButton("Agents", onAgents, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton("Wallets", onAgentWallets, Modifier.weight(1f))
            ActionButton("Reputation", onReputation, Modifier.weight(1f))
            ActionButton("Settings", onSettings, Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        Text("Unified balance", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        ArcOraCard {
            portfolio?.balances?.forEachIndexed { index, balance ->
                KeyValueRow(balance.chainName, balance.balance.formatted(), if (balance.isArcNative) ArcoraGreen else MaterialTheme.colorScheme.onSurface)
                if (index != portfolio.balances.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Recent activity", fontWeight = FontWeight.Bold)
            TextButton(onClick = onActivity) { Text("View all") }
        }
        ArcOraCard {
            state.activity.take(3).forEachIndexed { index, item ->
                KeyValueRow(item.title, item.amount.formatted())
                Text(item.subtitle, color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
                if (index != state.activity.take(3).lastIndex) Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Button(onClick = onClick, modifier = modifier.height(52.dp)) { Text(text) }
}
