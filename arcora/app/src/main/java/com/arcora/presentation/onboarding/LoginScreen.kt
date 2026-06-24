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
import com.arcora.presentation.theme.ArcoraMuted

@Composable
fun LoginScreen(
    onWalletReady: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onWalletReady()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(Modifier.height(34.dp))
            ScreenHeader("Welcome back", "Log in to your ArcOra account.")
            Spacer(Modifier.height(24.dp))

            ArcOraCard(elevated = true) {
                ArcOraTextField(state.email, viewModel::onEmailChange, "Email")
                Spacer(Modifier.height(12.dp))
                ArcOraTextField(state.password, viewModel::onPasswordChange, "Password", isPassword = true)

                state.error?.let { error ->
                    Spacer(Modifier.height(10.dp))
                    ArcOraStatusCard(
                        title = "Login failed",
                        message = error,
                        actionLabel = "Retry",
                        onAction = viewModel::login,
                        loading = state.isLoading
                    )
                }

                Spacer(Modifier.height(18.dp))
                ArcOraPrimaryButton("Log in", viewModel::login, loading = state.isLoading)
            }

            Spacer(Modifier.height(12.dp))
            Text("Biometric approval protects every transaction.", color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(12.dp))
        ArcOraPrimaryButton("Back", onBack)
    }
}
