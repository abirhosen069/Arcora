package com.arcora.presentation.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.ArcOraStatusCard
import com.arcora.presentation.components.ArcOraTextField
import com.arcora.presentation.components.Pill
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted

@Composable
fun OnboardingScreen(
    onWalletReady: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.walletReady) {
        if (state.walletReady) onWalletReady()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    viewModel.onGoogleSignInResult(token, account.displayName ?: account.email ?: "Google User")
                }
            } catch (e: ApiException) {
                viewModel.onGoogleSignInResult("demo_google_token", "Google ArcOra User")
            }
        }
    }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthMethodButton("Email", state.authMethod == AuthMethod.EMAIL, { viewModel.selectAuthMethod(AuthMethod.EMAIL) }, Modifier.weight(1f))
                AuthMethodButton("Google", state.authMethod == AuthMethod.GOOGLE, { viewModel.selectAuthMethod(AuthMethod.GOOGLE) }, Modifier.weight(1f))
                AuthMethodButton("Passkey", state.authMethod == AuthMethod.PASSKEY, { viewModel.selectAuthMethod(AuthMethod.PASSKEY) }, Modifier.weight(1f))
            }

            Spacer(Modifier.height(18.dp))

            when (state.authMethod) {
                AuthMethod.EMAIL -> {
                    ArcOraTextField(state.displayName, viewModel::onDisplayNameChange, "Display name")
                    Spacer(Modifier.height(12.dp))
                    ArcOraTextField(state.email, viewModel::onEmailChange, "Email")
                    Spacer(Modifier.height(18.dp))
                    ArcOraPrimaryButton("Create smart wallet", viewModel::createWallet, loading = state.isLoading)
                }
                AuthMethod.GOOGLE -> {
                    ArcOraPrimaryButton("Continue with Google", {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }, loading = state.isLoading)
                }
                AuthMethod.PASSKEY -> {
                    ArcOraTextField(state.email, viewModel::onEmailChange, "Email for passkey")
                    Spacer(Modifier.height(18.dp))
                    ArcOraPrimaryButton("Create passkey", viewModel::createWallet, loading = state.isLoading)
                }
            }

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

            Spacer(Modifier.height(12.dp))
            Text("Biometric approval and passkeys will protect every transaction.", color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AuthMethodButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = if (selected) "✓ $label" else label,
        modifier = modifier
            .background(
                if (selected) ArcoraGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) ArcoraGreen else MaterialTheme.colorScheme.onSurface
    )
}
