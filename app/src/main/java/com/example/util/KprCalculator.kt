package com.example.util

import com.example.model.AffordabilityResult
import com.example.model.AffordabilityStatus
import com.example.model.AmortizationRow
import com.example.model.KprCalculationResult
import com.example.model.LoanType
import com.example.model.UpfrontCostBreakdown
import com.example.model.YearlyAmortizationRow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object KprCalculator {

    fun calculateConventional(
        propertyPrice: Double,
        dpPercent: Double,
        tenorYears: Int,
        fixedRate: Double,
        fixedYears: Int,
        floatingRate: Double
    ): KprCalculationResult {
        val dpAmount = propertyPrice * (dpPercent / 100.0)
        val loanAmount = max(0.0, propertyPrice - dpAmount)
        val totalMonths = tenorYears * 12
        val fixedMonths = min(totalMonths, fixedYears * 12)
        val hasFloating = fixedMonths < totalMonths && fixedRate != floatingRate

        val schedule = ArrayList<AmortizationRow>(totalMonths)
        var remainingPrincipal = loanAmount

        // Phase 1: Fixed Period
        val rFixed = (fixedRate / 100.0) / 12.0
        val installmentFixed = calculateAnnuityPayment(loanAmount, rFixed, totalMonths)

        var totalInterestPaid = 0.0

        for (m in 1..fixedMonths) {
            val interestPart = remainingPrincipal * rFixed
            val principalPart = installmentFixed - interestPart
            remainingPrincipal = max(0.0, remainingPrincipal - principalPart)
            totalInterestPaid += interestPart

            val currentYear = ((m - 1) / 12) + 1
            schedule.add(
                AmortizationRow(
                    month = m,
                    year = currentYear,
                    installment = installmentFixed,
                    principal = principalPart,
                    interestOrMargin = interestPart,
                    remainingBalance = remainingPrincipal,
                    isFloating = false
                )
            )
        }

        // Phase 2: Floating Period
        var installmentFloating = installmentFixed
        if (hasFloating) {
            val remainingMonths = totalMonths - fixedMonths
            val rFloating = (floatingRate / 100.0) / 12.0
            installmentFloating = calculateAnnuityPayment(remainingPrincipal, rFloating, remainingMonths)

            for (m in (fixedMonths + 1)..totalMonths) {
                val interestPart = remainingPrincipal * rFloating
                val principalPart = installmentFloating - interestPart
                remainingPrincipal = max(0.0, remainingPrincipal - principalPart)
                totalInterestPaid += interestPart

                val currentYear = ((m - 1) / 12) + 1
                schedule.add(
                    AmortizationRow(
                        month = m,
                        year = currentYear,
                        installment = installmentFloating,
                        principal = principalPart,
                        interestOrMargin = interestPart,
                        remainingBalance = remainingPrincipal,
                        isFloating = true
                    )
                )
            }
        }

        val totalPayment = loanAmount + totalInterestPaid
        val yearlySchedule = aggregateYearly(schedule, tenorYears)

        return KprCalculationResult(
            loanType = LoanType.KONVENSIONAL,
            propertyPrice = propertyPrice,
            dpAmount = dpAmount,
            loanAmount = loanAmount,
            tenorYears = tenorYears,
            monthlyInstallmentFixed = installmentFixed,
            monthlyInstallmentFloating = if (hasFloating) installmentFloating else installmentFixed,
            hasFloatingPhase = hasFloating,
            fixedYears = fixedYears,
            totalInterestOrMargin = totalInterestPaid,
            totalPayment = totalPayment,
            schedule = schedule,
            yearlySchedule = yearlySchedule
        )
    }

    fun calculateSyariah(
        propertyPrice: Double,
        dpPercent: Double,
        tenorYears: Int,
        marginRatePerYear: Double
    ): KprCalculationResult {
        val dpAmount = propertyPrice * (dpPercent / 100.0)
        val loanAmount = max(0.0, propertyPrice - dpAmount)
        val totalMonths = tenorYears * 12

        val totalMargin = loanAmount * (marginRatePerYear / 100.0) * tenorYears
        val totalPayment = loanAmount + totalMargin
        val monthlyInstallment = if (totalMonths > 0) totalPayment / totalMonths else 0.0
        val monthlyPrincipal = if (totalMonths > 0) loanAmount / totalMonths else 0.0
        val monthlyMargin = if (totalMonths > 0) totalMargin / totalMonths else 0.0

        val schedule = ArrayList<AmortizationRow>(totalMonths)
        var remainingPrincipal = loanAmount

        for (m in 1..totalMonths) {
            remainingPrincipal = max(0.0, remainingPrincipal - monthlyPrincipal)
            val currentYear = ((m - 1) / 12) + 1
            schedule.add(
                AmortizationRow(
                    month = m,
                    year = currentYear,
                    installment = monthlyInstallment,
                    principal = monthlyPrincipal,
                    interestOrMargin = monthlyMargin,
                    remainingBalance = remainingPrincipal,
                    isFloating = false
                )
            )
        }

        val yearlySchedule = aggregateYearly(schedule, tenorYears)

        return KprCalculationResult(
            loanType = LoanType.SYARIAH,
            propertyPrice = propertyPrice,
            dpAmount = dpAmount,
            loanAmount = loanAmount,
            tenorYears = tenorYears,
            monthlyInstallmentFixed = monthlyInstallment,
            monthlyInstallmentFloating = monthlyInstallment,
            hasFloatingPhase = false,
            fixedYears = tenorYears,
            totalInterestOrMargin = totalMargin,
            totalPayment = totalPayment,
            schedule = schedule,
            yearlySchedule = yearlySchedule
        )
    }

    private fun calculateAnnuityPayment(principal: Double, monthlyRate: Double, months: Int): Double {
        if (months <= 0) return 0.0
        if (monthlyRate <= 0.0) return principal / months
        val factor = (1.0 + monthlyRate).pow(months.toDouble())
        return principal * (monthlyRate * factor) / (factor - 1.0)
    }

    private fun aggregateYearly(schedule: List<AmortizationRow>, tenorYears: Int): List<YearlyAmortizationRow> {
        val result = mutableListOf<YearlyAmortizationRow>()
        for (year in 1..tenorYears) {
            val monthsInYear = schedule.filter { it.year == year }
            if (monthsInYear.isNotEmpty()) {
                val totalInstallment = monthsInYear.sumOf { it.installment }
                val totalPrincipal = monthsInYear.sumOf { it.principal }
                val totalInterest = monthsInYear.sumOf { it.interestOrMargin }
                val endBalance = monthsInYear.last().remainingBalance
                val isFloating = monthsInYear.any { it.isFloating }
                result.add(
                    YearlyAmortizationRow(
                        year = year,
                        totalInstallment = totalInstallment,
                        totalPrincipal = totalPrincipal,
                        totalInterest = totalInterest,
                        endBalance = endBalance,
                        isFloating = isFloating
                    )
                )
            }
        }
        return result
    }

    fun calculateUpfrontCosts(
        propertyPrice: Double,
        dpPercent: Double,
        npoptkp: Double = 80_000_000.0,
        adminBankCustom: Double = 750_000.0,
        notarisCustom: Double = 4_000_000.0
    ): UpfrontCostBreakdown {
        val dpAmount = propertyPrice * (dpPercent / 100.0)
        val loanAmount = max(0.0, propertyPrice - dpAmount)

        // BPHTB = 5% * (Harga - NPOPTKP)
        val bphtbTaxable = max(0.0, propertyPrice - npoptkp)
        val bphtb = bphtbTaxable * 0.05

        // Akta Jual Beli & Bea Balik Nama (~1%)
        val ajbBbn = propertyPrice * 0.01

        // Provisi Bank (~1% Plafon)
        val provisi = loanAmount * 0.01

        // Admin Bank
        val adminBank = adminBankCustom

        // Notaris & APHT
        val notarisApht = notarisCustom

        // Asuransi Jiwa & Kebakaran (~1.5% Plafon)
        val asuransi = loanAmount * 0.015

        val totalBiayaLain = bphtb + ajbBbn + provisi + adminBank + notarisApht + asuransi
        val totalUpfront = dpAmount + totalBiayaLain

        return UpfrontCostBreakdown(
            propertyPrice = propertyPrice,
            dpAmount = dpAmount,
            bphtb = bphtb,
            ajbBbn = ajbBbn,
            provisi = provisi,
            adminBank = adminBank,
            notarisApht = notarisApht,
            asuransi = asuransi,
            totalBiayaLain = totalBiayaLain,
            totalUpfront = totalUpfront
        )
    }

    fun calculateAffordability(
        monthlyIncome: Double,
        otherInstallments: Double,
        proposedKprInstallment: Double
    ): AffordabilityResult {
        val totalDebtWithKpr = otherInstallments + proposedKprInstallment

        val currentDsRatio = if (monthlyIncome > 0) (otherInstallments / monthlyIncome) * 100.0 else 0.0
        val totalDsRatio = if (monthlyIncome > 0) (totalDebtWithKpr / monthlyIncome) * 100.0 else 0.0

        val minIncome30 = totalDebtWithKpr / 0.30
        val minIncome40 = totalDebtWithKpr / 0.40

        val maxRecommendedInstallment30 = max(0.0, (monthlyIncome * 0.30) - otherInstallments)

        // Approximate max property price with 20yr tenor, 6% interest, 10% DP
        val r = (6.0 / 100.0) / 12.0
        val n = 20 * 12
        val factor = (1.0 + r).pow(n.toDouble())
        val maxLoan = if (maxRecommendedInstallment30 > 0) {
            maxRecommendedInstallment30 * (factor - 1.0) / (r * factor)
        } else 0.0
        val maxPropertyPrice = maxLoan / 0.90 // 90% loan, 10% DP

        val (status, message) = when {
            totalDsRatio <= 30.0 -> Pair(
                AffordabilityStatus.AMAN,
                "Beban cicilan sangat sehat (< 30%). Peluang persetujuan (ACC) bank sangat tinggi."
            )
            totalDsRatio <= 40.0 -> Pair(
                AffordabilityStatus.WASPADA,
                "Beban cicilan cukup wajar (30% - 40%). Bank biasanya masih dapat menyetujui dengan verifikasi pendapatan ketat."
            )
            else -> Pair(
                AffordabilityStatus.BERISIKO,
                "Beban cicilan melebihi batas anjuran BI/OJK (> 40%). Pertimbangkan perpanjang tenor, tambah DP, atau gabungkan penghasilan (joint income)."
            )
        }

        return AffordabilityResult(
            monthlyIncome = monthlyIncome,
            otherInstallments = otherInstallments,
            proposedKprInstallment = proposedKprInstallment,
            currentDsRatio = currentDsRatio,
            totalDsRatio = totalDsRatio,
            minIncomeRecommendation30 = minIncome30,
            minIncomeRecommendation40 = minIncome40,
            maxRecommendedInstallment30 = maxRecommendedInstallment30,
            maxRecommendedPropertyPrice = maxPropertyPrice,
            status = status,
            statusMessage = message
        )
    }
}
