package com.arcora.presentation.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun SendPaymentScreen(
    onDone: () -> Unit,
    initialRecipient: String = "",
    initialAmount: String = "",
    initialNote: String = "",
    viewModel: SendPaymentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(initialRecipient, initialAmount, initialNote) {
        if (initialRecipient.isNotBlank()) viewModel.onRecipientChange(initialRecipient)
        if (initialAmount.isNotBlank()) viewModel.onAmountChange(initialAmount)
        if (initialNote.isNotBlank()) viewModel.onNoteChange(initialNote)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("Send USDC", "Pay by username. ArcOra resolves the wallet address privately.")
        Spacer(Modifier.height(24.dp))
        ArcOraCard(elevated = true) {
            ArcOraTextField(state.recipient, viewModel::onRecipientChange, "Recipient (@alex)")
            Spacer(Modifier.height(12.dp))
            ArcOraTextField(state.amount, viewModel::onAmountChange, "Amount in USDC")
            Spacer(Modifier.height(12.dp))
            ArcOraTextField(state.note, viewModel::onNoteChange, "Note optional")
            Spacer(Modifier.height(18.dp))
            KeyValueRow("Network", "Arc Testnet", ArcoraGreen)
            Spacer(Modifier.height(8.dp))
            KeyValueRow("Fee", "Covered by smart wallet")
            Spacer(Modifier.height(18.dp))
            ArcOraPrimaryButton("Approve with biometric", viewModel::confirmWithBiometricApproval, loading = state.isLoading)
        }
        Spacer(Modifier.height(16.dp))
        state.error?.let { error ->
            ArcOraStatusCard(
                title = "Payment not ready",
                message = error,
                actionLabel = "Retry approval",
                onAction = viewModel::confirmWithBiometricApproval,
                loading = state.isLoading
            )
        }
        state.result?.let {
            Text(it, color = ArcoraGreen)
            Spacer(Modifier.height(12.dp))
            ArcOraPrimaryButton("Done", onDone)
        }
        Spacer(Modifier.height(16.dp))
        Text("Transactions are never executed without explicit confirmation.", color = ArcoraMuted)
    }
}
