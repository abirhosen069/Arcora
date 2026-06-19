package com.arcora.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.ArcOraStatusCard
import com.arcora.presentation.components.ArcOraTextField
import com.arcora.presentation.components.Pill
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraMuted

@Composable
fun OnboardingScreen(
    onWalletReady: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.walletReady) {
        if (state.walletReady) onWalletReady()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(Modifier.height(34.dp))
            Pill("Arc Testnet • USDC-first")
            Spacer(Modifier.height(24.dp))
            Text("ArcOra", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text(
                "A premium stablecoin wallet that feels like fintech, powered by Arc smart accounts.",
                color = ArcoraMuted
            )
        }

        ArcOraCard(elevated = true) {
            ScreenHeader("Create your wallet", "No seed phrase. No gas. Just your account.")
            Spacer(Modifier.height(18.dp))
            ArcOraTextField(state.displayName, viewModel::onDisplayNameChange, "Display name")
            Spacer(Modifier.height(12.dp))
            ArcOraTextField(state.email, viewModel::onEmailChange, "Email")
            state.error?.let { error ->
                Spacer(Modifier.height(10.dp))
                ArcOraStatusCard(
                    title = "Wallet setup needs attention",
                    message = error,
                    actionLabel = "Retry",
                    onAction = viewModel::createWallet,
                    loading = state.isLoading
                )
            }
            Spacer(Modifier.height(18.dp))
            ArcOraPrimaryButton("Create smart wallet", viewModel::createWallet, loading = state.isLoading)
            Spacer(Modifier.height(12.dp))
            Text("Biometric approval and passkeys will protect every transaction.", color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
