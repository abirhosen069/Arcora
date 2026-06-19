package com.arcora.presentation.activity

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
import com.arcora.domain.model.TransactionStatus
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted
import com.arcora.presentation.theme.ArcoraWarning

@Composable
fun ActivityScreen(
    onBack: () -> Unit,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val activity by viewModel.activity.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("Inbox", "Payments, requests, bridges, and receipts in one place.")
        Spacer(Modifier.height(24.dp))
        activity.forEach { tx ->
            ArcOraCard {
                KeyValueRow(tx.title, tx.amount.formatted())
                Spacer(Modifier.height(8.dp))
                Text(tx.subtitle, color = ArcoraMuted)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${tx.status} • ${tx.createdAtLabel}",
                    color = if (tx.status == TransactionStatus.Pending) ArcoraWarning else ArcoraGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(8.dp))
        ArcOraPrimaryButton("Back", onBack)
    }
}
