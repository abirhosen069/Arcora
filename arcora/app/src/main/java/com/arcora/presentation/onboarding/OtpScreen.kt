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
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted

@Composable
fun OtpScreen(
    email: String,
    passwordHash: String,
    displayName: String,
    username: String,
    onWalletReady: () -> Unit,
    onBack: () -> Unit,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.verified) {
        if (state.verified) onWalletReady()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(Modifier.height(34.dp))
            ScreenHeader("Verify your email", "Enter the 6-digit code sent to $email")
            Spacer(Modifier.height(24.dp))

            ArcOraCard(elevated = true) {
                ArcOraTextField(state.code, viewModel::onCodeChange, "Verification code")
                Spacer(Modifier.height(18.dp))

                state.error?.let { error ->
                    ArcOraStatusCard(
                        title = "Verification failed",
                        message = error,
                        actionLabel = "Retry",
                        onAction = { viewModel.verify(email, passwordHash, displayName, username) },
                        loading = state.isLoading
                    )
                    Spacer(Modifier.height(12.dp))
                }

                ArcOraPrimaryButton("Verify and create account", {
                    viewModel.verify(email, passwordHash, displayName, username)
                }, loading = state.isLoading)
            }

            Spacer(Modifier.height(12.dp))
            Text("Check your email inbox (and spam folder).", color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(12.dp))
        ArcOraPrimaryButton("Back", onBack)
    }
}
