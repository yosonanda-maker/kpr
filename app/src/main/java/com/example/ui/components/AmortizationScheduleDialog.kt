package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AmortizationRow
import com.example.model.KprCalculationResult
import com.example.model.YearlyAmortizationRow
import com.example.util.Formatters

@Composable
fun AmortizationScheduleDialog(
    result: KprCalculationResult,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tahunan, 1: Bulanan
    val horizontalScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("amortization_schedule_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Jadwal Angsuran & Amortisasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Plafon: ${Formatters.formatRupiah(result.loanAmount)} • Tenor ${result.tenorYears} Thn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_schedule_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ringkasan Tahunan") },
                        modifier = Modifier.testTag("tab_yearly_amortization")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Detail Bulanan (${result.schedule.size})") },
                        modifier = Modifier.testTag("tab_monthly_amortization")
                    )
                }

                // Table Container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    if (selectedTab == 0) {
                        YearlyScheduleTable(yearly = result.yearlySchedule)
                    } else {
                        MonthlyScheduleTable(schedule = result.schedule)
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyScheduleTable(yearly: List<YearlyAmortizationRow>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Tahun", 70.dp)
            TableHeaderCell("Porsi Pokok", 120.dp)
            TableHeaderCell("Porsi Bunga/Margin", 130.dp)
            TableHeaderCell("Total Angsuran", 130.dp)
            TableHeaderCell("Sisa Pokok Pinjaman", 140.dp)
            TableHeaderCell("Status", 90.dp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(yearly) { row ->
                val bg = if (row.year % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                Row(
                    modifier = Modifier
                        .background(bg)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell("Thn ke-${row.year}", 70.dp, isBold = true)
                    TableCell(Formatters.formatRupiah(row.totalPrincipal, false), 120.dp)
                    TableCell(
                        Formatters.formatRupiah(row.totalInterest, false),
                        130.dp,
                        textColor = MaterialTheme.colorScheme.error
                    )
                    TableCell(
                        Formatters.formatRupiah(row.totalInstallment, false),
                        130.dp,
                        textColor = MaterialTheme.colorScheme.primary,
                        isBold = true
                    )
                    TableCell(Formatters.formatRupiah(row.endBalance, false), 140.dp)
                    Box(modifier = Modifier.width(90.dp), contentAlignment = Alignment.Center) {
                        Surface(
                            color = if (row.isFloating) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (row.isFloating) "Floating" else "Fixed",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (row.isFloating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.25f))
            }
        }
    }
}

@Composable
private fun MonthlyScheduleTable(schedule: List<AmortizationRow>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Bulan", 75.dp)
            TableHeaderCell("Angsuran Pokok", 120.dp)
            TableHeaderCell("Angsuran Bunga", 120.dp)
            TableHeaderCell("Total Cicilan", 120.dp)
            TableHeaderCell("Sisa Pokok", 130.dp)
            TableHeaderCell("Bunga", 80.dp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(schedule) { row ->
                val bg = if (row.month % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                Row(
                    modifier = Modifier
                        .background(bg)
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell("Bln ${row.month} (Th ${row.year})", 75.dp, isBold = true)
                    TableCell(Formatters.formatRupiah(row.principal, false), 120.dp)
                    TableCell(
                        Formatters.formatRupiah(row.interestOrMargin, false),
                        120.dp,
                        textColor = MaterialTheme.colorScheme.error
                    )
                    TableCell(
                        Formatters.formatRupiah(row.installment, false),
                        120.dp,
                        textColor = MaterialTheme.colorScheme.primary,
                        isBold = true
                    )
                    TableCell(Formatters.formatRupiah(row.remainingBalance, false), 130.dp)
                    Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (row.isFloating) "Floating" else "Fixed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (row.isFloating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    textColor: Color = Color.Unspecified
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = textColor,
        textAlign = TextAlign.End,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp)
    )
}
