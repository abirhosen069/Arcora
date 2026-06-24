package com.arcora.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
fun RegisterScreen(
    onOtpNeeded: (email: String, passwordHash: String, displayName: String, username: String) -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.otpSent) {
        if (state.otpSent) onOtpNeeded(state.email, state.passwordHash, state.displayName, state.username)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(Modifier.height(34.dp))
            ScreenHeader("Create account", "Set up your ArcOra wallet in seconds.")
            Spacer(Modifier.height(24.dp))

            ArcOraCard(elevated = true) {
                ArcOraTextField(state.displayName, viewModel::onDisplayNameChange, "Display name")
                Spacer(Modifier.height(12.dp))
                ArcOraTextField(state.email, viewModel::onEmailChange, "Email")
                Spacer(Modifier.height(12.dp))
                ArcOraTextField(state.username, viewModel::onUsernameChange, "Username (e.g. @alex)")
                Spacer(Modifier.height(12.dp))
                ArcOraTextField(state.password, viewModel::onPasswordChange, "Password", isPassword = true)
                Spacer(Modifier.height(12.dp))
                ArcOraTextField(state.confirmPassword, viewModel::onConfirmPasswordChange, "Re-enter password", isPassword = true)

                state.error?.let { error ->
                    Spacer(Modifier.height(10.dp))
                    ArcOraStatusCard(
                        title = "Registration issue",
                        message = error,
                        actionLabel = "Retry",
                        onAction = viewModel::register,
                        loading = state.isLoading
                    )
                }

                Spacer(Modifier.height(18.dp))
                ArcOraPrimaryButton("Send verification code", viewModel::register, loading = state.isLoading)
            }

            Spacer(Modifier.height(12.dp))
            Text("By creating an account you agree to ArcOra's Terms of Service.", color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(12.dp))
        ArcOraPrimaryButton("Back", onBack)
    }
}
