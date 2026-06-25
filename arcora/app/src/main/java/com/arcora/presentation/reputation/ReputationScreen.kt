package com.arcora.presentation.reputation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.arcora.domain.repository.LeaderboardEntry
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.ArcOraStatusCard
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted
import com.arcora.presentation.theme.ArcoraWarning

@Composable
fun ReputationScreen(
    onDone: () -> Unit,
    viewModel: ReputationViewModel = hiltViewModel()
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
        ScreenHeader("Reputation", "Your trust score based on transaction history and verification.")
        Spacer(Modifier.height(24.dp))

        state.myReputation?.let { rep ->
            ArcOraCard(elevated = true) {
                Text("Your reputation", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                KeyValueRow("Score", "${rep.score}/100", ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Level", rep.level, ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Verified", if (rep.isVerified) "Yes" else "No", ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Sends", rep.sentTransactions.toString())
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Receives", rep.receivedTransactions.toString())
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Agent wallets", rep.agentWallets.toString())
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Total volume", "${rep.totalVolume} USDC")
                if (rep.factors.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Factors", fontWeight = FontWeight.SemiBold)
                    rep.factors.forEach { factor ->
                        Spacer(Modifier.height(4.dp))
                        Text("• $factor", color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            Text("Leaderboard", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (state.leaderboard.isEmpty()) {
                Text("No reputation data yet.", color = ArcoraMuted)
            } else {
                state.leaderboard.forEachIndexed { index, entry ->
                    LeaderboardRow(index + 1, entry)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        state.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            ArcOraStatusCard(
                title = "Reputation unavailable",
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
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    KeyValueRow("#$rank ${entry.displayName}", "${entry.reputationScore}/100", ArcoraGreen)
    Spacer(Modifier.height(4.dp))
    KeyValueRow(entry.username, if (entry.isVerified) "Verified" else "", if (entry.isVerified) ArcoraGreen else ArcoraMuted)
}
