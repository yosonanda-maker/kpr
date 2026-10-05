package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object Formatters {
    private val idLocale = Locale("id", "ID")

    private val rupiahSymbols = DecimalFormatSymbols(idLocale).apply {
        currencySymbol = "Rp "
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
        decimalSeparator = ','
    }

    private val rupiahFormat = DecimalFormat("###,###,###,###", rupiahSymbols)
    private val decimalFormat = DecimalFormat("#,##0.0#", rupiahSymbols)

    fun formatRupiah(amount: Double, withPrefix: Boolean = true): String {
        val rounded = amount.roundToLong()
        val formatted = rupiahFormat.format(rounded)
        return if (withPrefix) "Rp $formatted" else formatted
    }

    fun formatShortRupiah(amount: Double): String {
        val absAmount = abs(amount)
        return when {
            absAmount >= 1_000_000_000 -> {
                val value = amount / 1_000_000_000.0
                String.format(idLocale, "%.2f Miliar", value).replace(",00", "")
            }
            absAmount >= 1_000_000 -> {
                val value = amount / 1_000_000.0
                String.format(idLocale, "%.1f Juta", value).replace(",0", "")
            }
            absAmount >= 1_000 -> {
                val value = amount / 1_000.0
                String.format(idLocale, "%.0f Ribu", value)
            }
            else -> formatRupiah(amount)
        }
    }

    fun formatPercent(rate: Double): String {
        return "${decimalFormat.format(rate)}%"
    }

    fun parseCleanNumber(input: String): Double {
        val clean = input.filter { it.isDigit() }
        return clean.toDoubleOrNull() ?: 0.0
    }
}
