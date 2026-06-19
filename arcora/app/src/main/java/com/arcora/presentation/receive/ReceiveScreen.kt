package com.arcora.presentation.receive

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcora.presentation.components.ArcOraCard
import com.arcora.presentation.components.ArcOraPrimaryButton
import com.arcora.presentation.components.ArcOraTextField
import com.arcora.presentation.components.KeyValueRow
import com.arcora.presentation.components.ScreenHeader
import com.arcora.presentation.theme.ArcoraCardElevated
import com.arcora.presentation.theme.ArcoraGreen
import com.arcora.presentation.theme.ArcoraMuted
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun ReceiveScreen(
    onDone: () -> Unit,
    viewModel: ReceiveViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val qrBitmap = remember(state.activePayload) { generateQrBitmap(state.activePayload) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        ScreenHeader("Receive", "Generate static or amount-locked ArcOra payment QR codes.")
        Spacer(Modifier.height(24.dp))

        ArcOraCard(elevated = true) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .size(236.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(ArcoraGreen)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "ArcOra payment QR",
                        modifier = Modifier
                            .size(208.dp)
                            .clip(RoundedCornerShape(22.dp))
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(state.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("Arc Testnet smart account", color = ArcoraMuted)
                Spacer(Modifier.height(10.dp))
                Text(
                    state.smartAccountAddress,
                    color = ArcoraMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            KeyValueRow("Static receive QR", if (state.isPaymentRequest) "Available" else "Active", if (state.isPaymentRequest) ArcoraMuted else ArcoraGreen)
            Spacer(Modifier.height(10.dp))
            KeyValueRow("Payment request QR", if (state.isPaymentRequest) "Active" else "Ready", if (state.isPaymentRequest) ArcoraGreen else ArcoraMuted)
            Spacer(Modifier.height(10.dp))
            KeyValueRow("Token", "USDC", ArcoraGreen)
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            Text("Create payment request", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            ArcOraTextField(state.amount, viewModel::onAmountChange, "Amount in USDC")
            Spacer(Modifier.height(10.dp))
            ArcOraTextField(state.memo, viewModel::onMemoChange, "Memo (optional)")
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArcOraPrimaryButton(
                    text = "Static",
                    onClick = viewModel::useStaticQr,
                    modifier = Modifier.weight(1f),
                    enabled = state.isPaymentRequest
                )
                ArcOraPrimaryButton(
                    text = "Request QR",
                    onClick = viewModel::usePaymentRequestQr,
                    modifier = Modifier.weight(1f),
                    enabled = state.amount.toBigDecimalOrNull() != null
                )
            }
            state.error?.let { error ->
                Spacer(Modifier.height(10.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(18.dp))
        ArcOraCard {
            Text("Payload", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ArcoraCardElevated)
                    .padding(12.dp)
            ) {
                Text(state.activePayload, color = ArcoraMuted, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(20.dp))
        ArcOraPrimaryButton("Back", onDone)
    }
}

private fun generateQrBitmap(payload: String, size: Int = 512): Bitmap {
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}
