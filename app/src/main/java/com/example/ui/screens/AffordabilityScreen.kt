package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AffordabilityStatus
import com.example.ui.KprViewModel
import com.example.ui.components.CurrencyInputField
import com.example.util.Formatters

@Composable
fun AffordabilityScreen(
    viewModel: KprViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val afford = uiState.affordabilityResult
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("affordability_screen")
    ) {
        // Title Header
        Text(
            text = "Analisis Kemampuan Gaji (DSR)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Uji kelayakan beban cicilan berdasarkan anjuran Bank Indonesia & OJK (maksimal 30% - 40% dari penghasilan).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Monthly Income
        CurrencyInputField(
            label = "Penghasilan Bersih Bulanan (Take-Home Pay)",
            value = uiState.monthlyIncome,
            onValueChange = { viewModel.updateMonthlyIncome(it) },
            quickChips = listOf(1_000_000L, 5_000_000L, 10_000_000L),
            testTag = "input_monthly_income"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Joint Income Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gabungkan Penghasilan (Joint Income)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Penghasilan pasangan (suami/istri) untuk memperbesar peluang disetujui bank.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.hasJointIncome,
                        onCheckedChange = { viewModel.setJointIncomeEnabled(it) },
                        modifier = Modifier.testTag("switch_joint_income"),
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                AnimatedVisibility(visible = uiState.hasJointIncome) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        CurrencyInputField(
                            label = "Penghasilan Bersih Pasangan",
                            value = uiState.spouseIncome,
                            onValueChange = { viewModel.updateSpouseIncome(it) },
                            quickChips = listOf(1_000_000L, 5_000_000L),
                            testTag = "input_spouse_income"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Other Monthly Debts / Installments
        CurrencyInputField(
            label = "Cicilan Berjalan Lainnya per Bulan",
            value = uiState.otherInstallments,
            onValueChange = { viewModel.updateOtherInstallments(it) },
            helperText = "Cicilan motor/mobil, kartu kredit, paylater, atau pinjaman lain",
            quickChips = listOf(500_000L, 1_000_000L, 2_000_000L),
            testTag = "input_other_debts"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current DSR Evaluation Card
        val statusColor = when (afford.status) {
            AffordabilityStatus.AMAN -> MaterialTheme.colorScheme.primary
            AffordabilityStatus.WASPADA -> MaterialTheme.colorScheme.tertiary
            AffordabilityStatus.BERISIKO -> MaterialTheme.colorScheme.error
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_dsr_result"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rasio Beban Cicilan (DSR)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = String.format("%.1f%%", afford.totalDsRatio),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar Indicator
                val progress = (afford.totalDsRatio / 100.0).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = MaterialTheme.typography.labelSmall)
                    Text("30% (Ideal)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("40% (Batas Maksimal)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Message Box
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = when (afford.status) {
                                AffordabilityStatus.AMAN -> Icons.Default.CheckCircle
                                AffordabilityStatus.WASPADA -> Icons.Default.Info
                                AffordabilityStatus.BERISIKO -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = afford.status.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = afford.statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Minimum Income Recommendation
                Text(
                    text = "Rekomendasi Penghasilan untuk KPR Ini:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Standar Ideal (DSR 30%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatRupiah(afford.minIncomeRecommendation30),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Batas Minimal (DSR 40%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatRupiah(afford.minIncomeRecommendation40),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Max Property Price recommendation
                Text(
                    text = "Maksimal Harga Rumah Terjangkau:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatRupiah(afford.maxRecommendedPropertyPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "*Dengan asumsi DP 10%, bunga 6%, tenor 20 tahun, dan DSR 30%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pro Tips Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tips Lolos Persetujuan KPR Bank:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val tips = listOf(
                    "Pastikan skor SLIK OJK / BI Checking bersih tanpa tunggakan kartu kredit atau pinjol.",
                    "Lunasi cicilan kecil yang berjalan sebelum mengajukan KPR agar kapasitas cicilan longgar.",
                    "Sediakan rekening koran 3-6 bulan terakhir dengan mutasi kas yang rapi dan konsisten.",
                    "Jika DSR Anda di atas 40%, pertimbangkan pengajuan Joint Income atau menambah uang muka (DP)."
                )

                tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", fontWeight = FontWeight.Bold)
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
