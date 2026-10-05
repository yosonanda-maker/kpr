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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.KprSimulationEntity
import com.example.model.LoanType
import com.example.ui.KprViewModel
import com.example.util.Formatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun SavedSimulationsScreen(
    viewModel: KprViewModel,
    modifier: Modifier = Modifier
) {
    val savedList by viewModel.savedSimulations.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val comparisonIds = uiState.comparisonIds
    var showCompareDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("saved_simulations_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat & Perbandingan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${savedList.size} simulasi tersimpan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (comparisonIds.size == 2) {
                Button(
                    onClick = { showCompareDialog = true },
                    modifier = Modifier.testTag("btn_show_comparison")
                ) {
                    Icon(imageVector = Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bandingkan (2)")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (comparisonIds.isNotEmpty() && comparisonIds.size < 2) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pilih 1 simulasi lagi untuk membandingkan side-by-side",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = { viewModel.clearComparison() }) {
                        Text("Batal")
                    }
                }
            }
        }

        if (savedList.isEmpty()) {
            EmptySavedState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedList, key = { it.id }) { sim ->
                    SavedSimulationCard(
                        simulation = sim,
                        isSelectedForComparison = comparisonIds.contains(sim.id),
                        onToggleComparison = { viewModel.toggleComparison(sim.id) },
                        onLoad = { viewModel.loadSimulation(sim) },
                        onDelete = { viewModel.deleteSimulation(sim.id) }
                    )
                }
            }
        }
    }

    if (showCompareDialog) {
        val sim1 = savedList.find { it.id == comparisonIds.firstOrNull() }
        val sim2 = savedList.find { it.id == comparisonIds.elementAtOrNull(1) }
        if (sim1 != null && sim2 != null) {
            ComparisonModal(
                sim1 = sim1,
                sim2 = sim2,
                onDismiss = { showCompareDialog = false }
            )
        }
    }
}

@Composable
private fun SavedSimulationCard(
    simulation: KprSimulationEntity,
    isSelectedForComparison: Boolean,
    onToggleComparison: () -> Unit,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val isSyariah = simulation.loanType == LoanType.SYARIAH.name
    val dateString = remember(simulation.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        sdf.format(Date(simulation.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_card_${simulation.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Checkbox(
                        checked = isSelectedForComparison,
                        onCheckedChange = { onToggleComparison() },
                        modifier = Modifier.testTag("check_compare_${simulation.id}")
                    )
                    Column {
                        Text(
                            text = simulation.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$dateString • Tenor ${simulation.tenorYears} Thn",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (isSyariah) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isSyariah) "Syariah" else "Konvensional",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSyariah) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Financial Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cicilan per Bulan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Formatters.formatRupiah(simulation.monthlyInstallmentFixed)} /bln",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Plafon Pinjaman",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatRupiah(simulation.loanAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (simulation.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Catatan: ${simulation.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(6.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onLoad,
                    modifier = Modifier.testTag("btn_load_${simulation.id}")
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buka di Kalkulator")
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("btn_delete_${simulation.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySavedState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.HomeWork,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum Ada Simulasi Tersimpan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Simpan berbagai skenario KPR dari tab Kalkulator untuk membandingkan bunga bank dan cicilan secara berdampingan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ComparisonModal(
    sim1: KprSimulationEntity,
    sim2: KprSimulationEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .testTag("comparison_modal"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Perbandingan Simulasi KPR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Columns Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Metrik", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Text(sim1.title, modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(sim2.title, modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                CompareMetricRow("Jenis", sim1.loanType, sim2.loanType)
                CompareMetricRow("Harga Rumah", Formatters.formatShortRupiah(sim1.propertyPrice), Formatters.formatShortRupiah(sim2.propertyPrice))
                CompareMetricRow("Plafon Pinjaman", Formatters.formatShortRupiah(sim1.loanAmount), Formatters.formatShortRupiah(sim2.loanAmount))
                CompareMetricRow("Tenor", "${sim1.tenorYears} Thn", "${sim2.tenorYears} Thn")
                CompareMetricRow(
                    "Cicilan /bln",
                    Formatters.formatRupiah(sim1.monthlyInstallmentFixed),
                    Formatters.formatRupiah(sim2.monthlyInstallmentFixed),
                    highlight = true
                )
                CompareMetricRow("Total Bunga/Margin", Formatters.formatShortRupiah(sim1.totalInterestOrMargin), Formatters.formatShortRupiah(sim2.totalInterestOrMargin))
                CompareMetricRow("Total Pelunasan", Formatters.formatShortRupiah(sim1.totalPayment), Formatters.formatShortRupiah(sim2.totalPayment))

                // Difference Highlight
                val installmentDiff = abs(sim1.monthlyInstallmentFixed - sim2.monthlyInstallmentFixed)
                val totalDiff = abs(sim1.totalPayment - sim2.totalPayment)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "💡 Selisih Angsuran: ${Formatters.formatRupiah(installmentDiff)} /bulan",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "💡 Selisih Total Bayar: ${Formatters.formatRupiah(totalDiff)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_close_comparison")
                ) {
                    Text("Tutup Perbandingan")
                }
            }
        }
    }
}

@Composable
private fun CompareMetricRow(
    label: String,
    val1: String,
    val2: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = val1,
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = val2,
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
        )
    }
}
