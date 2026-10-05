package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.util.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CurrencyInputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "currency_input",
    quickChips: List<Long> = emptyList(),
    helperText: String? = null
) {
    val formattedDisplay = remember(value) {
        if (value <= 0.0) "" else Formatters.formatRupiah(value, withPrefix = false)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = formattedDisplay,
            onValueChange = { input ->
                val cleanNumber = Formatters.parseCleanNumber(input)
                onValueChange(cleanNumber)
            },
            label = { Text(label) },
            prefix = {
                Text(
                    text = "Rp ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (value > 0.0) {
                    IconButton(
                        onClick = { onValueChange(0.0) },
                        modifier = Modifier.testTag("${testTag}_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus input"
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )

        // Helper text showing readable short Rupiah
        val subtitle = if (value > 0.0) {
            "${Formatters.formatShortRupiah(value)} ${if (helperText != null) "• $helperText" else ""}"
        } else {
            helperText ?: "Masukkan nominal dalam Rupiah"
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
        )

        if (quickChips.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                quickChips.forEach { chipValue ->
                    val chipLabel = if (chipValue >= 1_000_000_000) {
                        "+${chipValue / 1_000_000_000} M"
                    } else {
                        "+${chipValue / 1_000_000} Jt"
                    }
                    AssistChip(
                        onClick = { onValueChange(value + chipValue.toDouble()) },
                        label = { Text(chipLabel, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )
                }
            }
        }
    }
}
