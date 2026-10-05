package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.LoanType
import com.example.ui.BankPreset
import com.example.ui.KprViewModel
import com.example.ui.components.AmortizationScheduleDialog
import com.example.ui.components.CurrencyInputField
import com.example.ui.components.ResultSummaryCard
import com.example.ui.components.SaveSimulationDialog
import com.example.util.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(
    viewModel: KprViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("calculator_screen")
    ) {
        // Loan Type Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.loanType == LoanType.KONVENSIONAL,
                onClick = { viewModel.setLoanType(LoanType.KONVENSIONAL) },
                label = { Text("Konvensional (Anuitas)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_conventional"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            FilterChip(
                selected = uiState.loanType == LoanType.SYARIAH,
                onClick = { viewModel.setLoanType(LoanType.SYARIAH) },
                label = { Text("Syariah (Murabahah)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_syariah"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }

        // Quick Bank Presets
        Text(
            text = "Preset Promo Bank Populer:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            viewModel.bankPresets.forEach { preset ->
                AssistChip(
                    onClick = { viewModel.applyPreset(preset) },
                    label = { Text(preset.bankName) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.testTag("preset_${preset.bankName.replace(" ", "_")}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Property Price Input
        CurrencyInputField(
            label = "Harga Properti / Rumah",
            value = uiState.propertyPrice,
            onValueChange = { viewModel.updatePropertyPrice(it) },
            quickChips = listOf(50_000_000L, 100_000_000L, 500_000_000L, 1_000_000_000L),
            testTag = "input_property_price"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Down Payment (DP) Card
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
                    Text(
                        text = "Uang Muka (DP)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format("%.1f", uiState.dpPercent)}% (${Formatters.formatShortRupiah(uiState.calculationResult.dpAmount)})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = uiState.dpPercent.toFloat(),
                    onValueChange = { viewModel.updateDpPercent(it.toDouble()) },
                    valueRange = 0f..50f,
                    steps = 9,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_dp_percent"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Quick DP % Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(5.0, 10.0, 15.0, 20.0, 30.0).forEach { pct ->
                        AssistChip(
                            onClick = { viewModel.updateDpPercent(pct) },
                            label = { Text("${pct.toInt()}%") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (uiState.dpPercent == pct) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calculated Plafon KPR
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Plafon Pinjaman KPR:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatRupiah(uiState.calculationResult.loanAmount),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tenor Slider
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
                    Text(
                        text = "Jangka Waktu (Tenor)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.tenorYears} Tahun (${uiState.tenorYears * 12} Bulan)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = uiState.tenorYears.toFloat(),
                    onValueChange = { viewModel.updateTenorYears(it.toInt()) },
                    valueRange = 5f..30f,
                    steps = 24,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_tenor"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(5, 10, 15, 20, 25).forEach { yr ->
                        AssistChip(
                            onClick = { viewModel.updateTenorYears(yr) },
                            label = { Text("$yr Thn") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (uiState.tenorYears == yr) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interest Rates / Margin Configuration
        if (uiState.loanType == LoanType.KONVENSIONAL) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Suku Bunga Konvensional",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.fixedRate.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { rate -> viewModel.updateFixedRate(rate) } },
                            label = { Text("Bunga Fixed (%)") },
                            singleLine = true,
                            trailingIcon = { Text("%  ") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_fixed_rate")
                        )

                        OutlinedTextField(
                            value = uiState.fixedYears.toString(),
                            onValueChange = { it.toIntOrNull()?.let { yrs -> viewModel.updateFixedYears(yrs) } },
                            label = { Text("Masa Fixed") },
                            singleLine = true,
                            trailingIcon = { Text("Thn  ") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_fixed_years")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.floatingRate.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { rate -> viewModel.updateFloatingRate(rate) } },
                        label = { Text("Bunga Floating (Setelah Masa Fixed)") },
                        singleLine = true,
                        trailingIcon = { Text("%  ") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_floating_rate")
                    )

                    Text(
                        text = "💡 Rata-rata suku bunga floating di Indonesia saat ini berkisar antara 10% - 12% p.a.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Margin KPR Syariah (Murabahah)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.syariahMarginRate.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { rate -> viewModel.updateSyariahMarginRate(rate) } },
                        label = { Text("Margin Keuntungan Bank (% per tahun)") },
                        singleLine = true,
                        trailingIcon = { Text("%  ") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_syariah_margin")
                    )

                    Text(
                        text = "✨ Pada akad Murabahah syariah, cicilan bulanan bersifat TETAP (fixed) sampai akhir tenor tanpa fluktuasi floating suku bunga BI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Results Card
        ResultSummaryCard(
            result = uiState.calculationResult,
            onViewSchedule = { viewModel.setShowScheduleDialog(true) },
            onSaveSimulation = { viewModel.setShowSaveDialog(true) },
            onShare = {
                shareSimulationSummary(context, uiState.calculationResult, uiState.loanType)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Amortization Schedule Dialog
    if (uiState.showScheduleDialog) {
        AmortizationScheduleDialog(
            result = uiState.calculationResult,
            onDismiss = { viewModel.setShowScheduleDialog(false) }
        )
    }

    // Save Simulation Dialog
    if (uiState.showSaveDialog) {
        SaveSimulationDialog(
            initialTitle = "Properti ${Formatters.formatShortRupiah(uiState.propertyPrice)}",
            onDismiss = { viewModel.setShowSaveDialog(false) },
            onSave = { title, notes ->
                viewModel.saveCurrentSimulation(title, notes)
            }
        )
    }
}

private fun shareSimulationSummary(
    context: Context,
    result: com.example.model.KprCalculationResult,
    type: LoanType
) {
    val text = buildString {
        appendLine("🏠 *SIMULASI KPR INDONESIA*")
        appendLine("Jenis KPR: ${type.displayName}")
        appendLine("Harga Properti: ${Formatters.formatRupiah(result.propertyPrice)}")
        appendLine("Uang Muka (DP): ${Formatters.formatRupiah(result.dpAmount)}")
        appendLine("Plafon Pinjaman: ${Formatters.formatRupiah(result.loanAmount)}")
        appendLine("Jangka Waktu: ${result.tenorYears} Tahun (${result.tenorYears * 12} Bulan)")
        appendLine("-----------------------------")
        if (result.hasFloatingPhase) {
            appendLine("💰 Cicilan Masa Fixed: ${Formatters.formatRupiah(result.monthlyInstallmentFixed)} /bln (Th 1-${result.fixedYears})")
            appendLine("📈 Estimasi Cicilan Floating: ${Formatters.formatRupiah(result.monthlyInstallmentFloating)} /bln")
        } else {
            appendLine("💰 Cicilan Bulanan: ${Formatters.formatRupiah(result.monthlyInstallmentFixed)} /bln")
        }
        appendLine("Total Bunga/Margin: ${Formatters.formatRupiah(result.totalInterestOrMargin)}")
        appendLine("Total Pembayaran: ${Formatters.formatRupiah(result.totalPayment)}")
        appendLine("-----------------------------")
        appendLine("Dihitung dengan Kalkulator KPR")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        this.type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "Bagikan Hasil Simulasi KPR"))
}
