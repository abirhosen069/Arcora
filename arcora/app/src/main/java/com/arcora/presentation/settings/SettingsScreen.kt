package com.arcora.presentation.settings

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
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted
import com.arcora.presentation.theme.ArcoraWarning

@Composable
fun SettingsScreen(
    onDone: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) onSignedOut()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("Settings", "Manage your ArcOra account and preferences.")
        Spacer(Modifier.height(24.dp))

        state.profile?.let { profile ->
            ArcOraCard(elevated = true) {
                Text("Account", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                KeyValueRow("Name", profile.displayName, ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Username", profile.username)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Email", profile.email)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Reputation", "${profile.reputationScore}/100", ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Verified", if (profile.isVerified) "Yes" else "No", if (profile.isVerified) ArcoraGreen else ArcoraMuted)
            }
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            Text("Wallet", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            state.profile?.let { profile ->
                KeyValueRow("Address", profile.smartAccountAddress.take(20) + "...", ArcoraMuted)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Network", "Arc Testnet", ArcoraGreen)
                Spacer(Modifier.height(8.dp))
                KeyValueRow("Token", "USDC")
            }
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            Text("Security", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            KeyValueRow("Session", "Active", ArcoraGreen)
            Spacer(Modifier.height(8.dp))
            KeyValueRow("Biometric", "Enabled", ArcoraGreen)
            Spacer(Modifier.height(8.dp))
            KeyValueRow("Encryption", "AES-256", ArcoraGreen)
        }

        state.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            ArcOraStatusCard(
                title = "Profile unavailable",
                message = error,
                actionLabel = "Retry",
                onAction = viewModel::refresh,
                loading = state.isLoading
            )
        }

        Spacer(Modifier.height(24.dp))
        ArcOraPrimaryButton("Refresh profile", viewModel::refresh, loading = state.isLoading)
        Spacer(Modifier.height(12.dp))
        ArcOraPrimaryButton("Sign out", viewModel::signOut, loading = false)
        Spacer(Modifier.weight(1f))
        ArcOraPrimaryButton("Back", onDone)
    }
}
