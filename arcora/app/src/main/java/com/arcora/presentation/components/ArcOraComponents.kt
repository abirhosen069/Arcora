package com.arcora.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arcora.presentation.theme.ArcoraBlack
import com.arcora.presentation.theme.ArcoraCard
import com.arcora.presentation.theme.ArcoraCardElevated
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted
import com.arcora.presentation.theme.ArcoraWarning

@Composable
fun ArcOraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(containerColor = ArcoraGreen, contentColor = ArcoraBlack),
        shape = RoundedCornerShape(18.dp)
    ) {
        if (loading) CircularProgressIndicator(color = ArcoraBlack) else Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ArcOraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun ArcOraCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (elevated) ArcoraCardElevated else ArcoraCard),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = ArcoraMuted)
        }
    }
}

@Composable
fun ArcOraLoadingScreen(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = ArcoraGreen)
            Text(message, color = ArcoraMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ArcOraStatusCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = true,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    loading: Boolean = false
) {
    ArcOraCard(modifier = modifier) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            color = if (isError) ArcoraWarning else ArcoraGreen
        )
        Spacer(Modifier.height(6.dp))
        Text(message, color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onAction, enabled = !loading) {
                Text(if (loading) "Working…" else actionLabel)
            }
        }
    }
}

@Composable
fun KeyValueRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = ArcoraMuted)
        Text(value, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(ArcoraCardElevated, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = ArcoraMuted, style = MaterialTheme.typography.labelMedium) }
}
