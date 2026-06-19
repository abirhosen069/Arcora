package com.arcora.presentation.assistant

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcora.domain.ai.AiActionType
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.ArcOraTextField
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted
import com.arcora.presentation.theme.ArcoraWarning

@Composable
fun AssistantScreen(
    onDone: () -> Unit,
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("ArcOra AI", "Tell your wallet what you want. You always confirm before money moves.")
        Spacer(Modifier.height(24.dp))
        ArcOraCard(elevated = true) {
            ArcOraTextField(state.prompt, viewModel::onPromptChange, "Try: Send 50 USDC to @alex")
            Spacer(Modifier.height(18.dp))
            ArcOraPrimaryButton("Parse intent", viewModel::parse, loading = state.isLoading)
        }
        Spacer(Modifier.height(18.dp))
        state.parsedIntent?.let { intent ->
            ArcOraCard {
                Text(intent.confirmationTitle, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                KeyValueRow("Action", intent.action.name, ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Confidence", "${(intent.confidence * 100).toInt()}%")
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Confirmation", if (intent.requiresConfirmation) "Required" else "Not required")
                intent.amount?.let { amount ->
                    Spacer(Modifier.height(8.dp))
                    KeyValueRow("Amount", amount.formatted())
                }
                intent.recipient?.let { recipient ->
                    Spacer(Modifier.height(8.dp))
                    KeyValueRow("Recipient", recipient)
                }
                intent.sourceChain?.let { source ->
                    Spacer(Modifier.height(8.dp))
                    KeyValueRow("Source", source)
                }
                Spacer(Modifier.height(14.dp))
                Text(intent.executionCopy(), color = ArcoraMuted)
                Spacer(Modifier.height(14.dp))
                ArcOraPrimaryButton(
                    text = intent.confirmButtonLabel(),
                    onClick = viewModel::executeParsedIntent,
                    loading = state.isExecuting,
                    enabled = intent.action != AiActionType.Unknown
                )
            }
        }
        state.executionResult?.let { result ->
            Spacer(Modifier.height(18.dp))
            ArcOraCard {
                Text("Result", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(result, color = ArcoraGreen)
            }
        }
        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = ArcoraWarning)
        }
        Spacer(Modifier.weight(1f))
        ArcOraPrimaryButton("Back", onDone)
    }
}

private fun com.arcora.domain.ai.AiIntent.executionCopy(): String = when (action) {
    AiActionType.SendPayment -> "This will prepare a payment quote and require transaction approval before broadcast."
    AiActionType.RequestPayment -> "This prepares request details. Use Receive to generate a request QR for collection."
    AiActionType.BridgeToArc -> "This prepares a bridge route and requires transaction approval before execution."
    AiActionType.ShowSpending -> "This routes to spending analytics once backend aggregation is available."
    AiActionType.Unknown -> "Add more details so ArcOra can safely prepare the right action."
}

private fun com.arcora.domain.ai.AiIntent.confirmButtonLabel(): String = when (action) {
    AiActionType.SendPayment -> "Prepare send"
    AiActionType.RequestPayment -> "Prepare request"
    AiActionType.BridgeToArc -> "Prepare bridge"
    AiActionType.ShowSpending -> "Show summary"
    AiActionType.Unknown -> "Need details"
}
