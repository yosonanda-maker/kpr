package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.KprViewModel
import com.example.ui.components.CurrencyInputField
import com.example.util.Formatters

@Composable
fun UpfrontCostScreen(
    viewModel: KprViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val upfront = uiState.upfrontCostBreakdown
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("upfront_cost_screen")
    ) {
        // Title
        Text(
            text = "Kalkulator Biaya Awal Akad KPR",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Total dana tunai yang wajib disiapkan sebelum akad: Uang Muka (DP) ditambah biaya legalitas, pajak daerah, dan perbankan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Grand Total Highlight Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_total_upfront"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Total Dana Awal Yang Wajib Disiapkan",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = Formatters.formatRupiah(upfront.totalUpfront),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DP: ${Formatters.formatShortRupiah(upfront.dpAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Biaya Lain: ${Formatters.formatShortRupiah(upfront.totalBiayaLain)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Cost Breakdown Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Uang Muka (DP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                CostRow(
                    label = "Uang Muka (${String.format("%.1f", uiState.dpPercent)}% dari harga rumah)",
                    amount = upfront.dpAmount,
                    isHighlight = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "2. Pajak & Legalitas (Notaris / BPN)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                CostRow(
                    label = "BPHTB (Pajak Pembeli)",
                    amount = upfront.bphtb,
                    subtitle = "5% × (Harga Rumah - NPOPTKP ${Formatters.formatShortRupiah(uiState.npoptkp)})"
                )

                CostRow(
                    label = "AJB & Bea Balik Nama (BBN)",
                    amount = upfront.ajbBbn,
                    subtitle = "Akta Jual Beli PPAT & pendaftaran balik nama sertifikat (~1%)"
                )

                CostRow(
                    label = "Jasa Notaris & APHT",
                    amount = upfront.notarisApht,
                    subtitle = "Akta Pembebanan Hak Tanggungan & SKMHT"
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "3. Biaya Perbankan & Asuransi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                CostRow(
                    label = "Biaya Provisi Bank",
                    amount = upfront.provisi,
                    subtitle = "1% dari plafon kredit KPR"
                )

                CostRow(
                    label = "Biaya Administrasi Bank",
                    amount = upfront.adminBank,
                    subtitle = "Biaya administrasi & dokumen bank"
                )

                CostRow(
                    label = "Asuransi Jiwa & Kebakaran",
                    amount = upfront.asuransi,
                    subtitle = "Perlindungan debitur dan aset rumah selama masa kredit (~1.5%)"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Customizable Variables Card (NPOPTKP, Notaris, Admin)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pengaturan Nilai Biaya",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sesuaikan nilai acuan dengan daerah dan penawaran developer/bank Anda:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                CurrencyInputField(
                    label = "NPOPTKP (Batas Bebas Pajak BPHTB)",
                    value = uiState.npoptkp,
                    onValueChange = { viewModel.updateNpoptkp(it) },
                    helperText = "DKI Jakarta: Rp 80 Jt, Bodetabek: Rp 60-80 Jt",
                    testTag = "input_npoptkp"
                )

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyInputField(
                    label = "Estimasi Biaya Notaris / APHT",
                    value = uiState.notarisCustom,
                    onValueChange = { viewModel.updateNotaris(it) },
                    testTag = "input_notaris"
                )

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyInputField(
                    label = "Biaya Administrasi Bank",
                    value = uiState.adminBankCustom,
                    onValueChange = { viewModel.updateAdminBank(it) },
                    testTag = "input_admin_bank"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CostRow(
    label: String,
    amount: Double,
    subtitle: String? = null,
    isHighlight: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = if (isHighlight) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = Formatters.formatRupiah(amount),
                style = if (isHighlight) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
