package com.arcora.presentation.agents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.arcora.data.api.AgentListingResponse
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.Pill
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraBlue
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted
import com.arcora.presentation.theme.ArcoraWarning

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgentMarketplaceScreen(
    onDone: () -> Unit,
    viewModel: AgentMarketplaceViewModel = hiltViewModel()
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
        ScreenHeader("Agent Marketplace", "Delegate limited testnet budgets to verified AI agents.")
        Spacer(Modifier.height(18.dp))

        ArcOraCard(elevated = true) {
            KeyValueRow("Network", state.network, ArcoraGreen)
            Spacer(Modifier.height(10.dp))
            KeyValueRow("Settlement", state.settlementToken)
            state.policy?.let { policy ->
                Spacer(Modifier.height(12.dp))
                Text(policy, color = ArcoraMuted)
            }
        }

        Spacer(Modifier.height(18.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.categories.ifEmpty { listOf("All") }.forEach { category ->
                val selected = category == state.selectedCategory
                Pill(
                    text = if (selected) "✓ $category" else category,
                    modifier = Modifier.clickable { viewModel.selectCategory(category) }
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        if (state.isLoading) {
            ArcOraCard { Text("Loading marketplace...", color = ArcoraMuted) }
        }

        state.visibleAgents.forEach { agent ->
            AgentCard(
                agent = agent,
                onSelect = { viewModel.selectAgent(agent) },
                onRequest = { viewModel.requestDelegatedWallet(agent) }
            )
            Spacer(Modifier.height(14.dp))
        }

        if (!state.isLoading && state.visibleAgents.isEmpty()) {
            ArcOraCard { Text("No agents available for this category yet.", color = ArcoraMuted) }
            Spacer(Modifier.height(14.dp))
        }

        state.selectedAgent?.let { agent ->
            ArcOraCard(elevated = true) {
                Text("Selected agent", fontWeight = FontWeight.Bold, color = ArcoraBlue)
                Spacer(Modifier.height(8.dp))
                KeyValueRow(agent.name, "${agent.monthlyBudget} ${agent.token}/mo", ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                Text(agent.permissions.joinToString(prefix = "Permissions: ", separator = " • "), color = ArcoraMuted)
            }
            Spacer(Modifier.height(14.dp))
        }

        state.result?.let {
            ArcOraCard { Text(it, color = ArcoraGreen) }
            Spacer(Modifier.height(14.dp))
        }

        state.error?.let {
            Text(it, color = ArcoraWarning)
            Spacer(Modifier.height(14.dp))
        }

        ArcOraPrimaryButton("Refresh marketplace", viewModel::refresh, loading = state.isLoading)
        Spacer(Modifier.height(10.dp))
        ArcOraPrimaryButton("Back", onDone)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AgentCard(
    agent: AgentListingResponse,
    onSelect: () -> Unit,
    onRequest: () -> Unit
) {
    ArcOraCard(modifier = Modifier.clickable(onClick = onSelect)) {
        KeyValueRow(agent.name, "${agent.monthlyBudget} ${agent.token}/mo", ArcoraGreen)
        Spacer(Modifier.height(8.dp))
        KeyValueRow(agent.category, agent.riskLevel.uppercase(), if (agent.riskLevel == "low") ArcoraGreen else ArcoraWarning)
        Spacer(Modifier.height(10.dp))
        Text(agent.description, color = ArcoraMuted)
        Spacer(Modifier.height(10.dp))
        Text(agent.reputationLabel, color = ArcoraBlue, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(12.dp))
        ArcOraPrimaryButton("Prepare delegated wallet", onRequest)
    }
}
