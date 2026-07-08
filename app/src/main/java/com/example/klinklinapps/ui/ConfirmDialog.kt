package com.example.klinklinapps.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Permintaan konfirmasi generik: dipakai untuk tombol ubah status & logout.
 * Simpan di state layar (mis. `var confirm by remember { mutableStateOf<ConfirmRequest?>(null) }`),
 * lalu render [ConfirmActionDialog] saat tidak null.
 */
class ConfirmRequest(
    val title: String,
    val message: String,
    val confirmLabel: String = "Ya, Lanjutkan",
    val destructive: Boolean = false,
    val onConfirm: () -> Unit
)

@Composable
fun ConfirmActionDialog(request: ConfirmRequest, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (request.destructive) Icons.Default.WarningAmber else Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                tint = if (request.destructive) Color.Red else KlinKlinTheme.Primary
            )
        },
        title = { Text(request.title, fontWeight = FontWeight.Black, color = KlinKlinTheme.Foreground) },
        text = { Text(request.message, color = KlinKlinTheme.MutedForeground, fontSize = 14.sp) },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    request.onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (request.destructive) Color.Red else KlinKlinTheme.Primary
                )
            ) { Text(request.confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = KlinKlinTheme.MutedForeground) }
        },
        containerColor = Color.White
    )
}

/** Dialog konfirmasi logout — dipakai semua role. */
@Composable
fun LogoutConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ConfirmActionDialog(
        request = ConfirmRequest(
            title = "Keluar dari Akun?",
            message = "Kamu harus login kembali untuk menggunakan aplikasi. Lanjutkan?",
            confirmLabel = "Ya, Keluar",
            destructive = true,
            onConfirm = onConfirm
        ),
        onDismiss = onDismiss
    )
}
