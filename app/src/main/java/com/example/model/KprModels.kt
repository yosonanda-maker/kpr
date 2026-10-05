package com.example.model

enum class LoanType(val displayName: String) {
    KONVENSIONAL("KPR Konvensional"),
    SYARIAH("KPR Syariah (Murabahah)")
}

enum class AffordabilityStatus(val label: String) {
    AMAN("Sangat Aman (< 30%)"),
    WASPADA("Cukup Aman / Waspada (30% - 40%)"),
    BERISIKO("Beban Tinggi (> 40%)")
}

data class AmortizationRow(
    val month: Int,
    val year: Int,
    val installment: Double,
    val principal: Double,
    val interestOrMargin: Double,
    val remainingBalance: Double,
    val isFloating: Boolean = false
)

data class YearlyAmortizationRow(
    val year: Int,
    val totalInstallment: Double,
    val totalPrincipal: Double,
    val totalInterest: Double,
    val endBalance: Double,
    val isFloating: Boolean = false
)

data class KprCalculationResult(
    val loanType: LoanType,
    val propertyPrice: Double,
    val dpAmount: Double,
    val loanAmount: Double,
    val tenorYears: Int,
    val monthlyInstallmentFixed: Double,
    val monthlyInstallmentFloating: Double = 0.0,
    val hasFloatingPhase: Boolean = false,
    val fixedYears: Int = 0,
    val totalInterestOrMargin: Double,
    val totalPayment: Double,
    val schedule: List<AmortizationRow>,
    val yearlySchedule: List<YearlyAmortizationRow>
)

data class UpfrontCostBreakdown(
    val propertyPrice: Double,
    val dpAmount: Double,
    val bphtb: Double,
    val ajbBbn: Double,
    val provisi: Double,
    val adminBank: Double,
    val notarisApht: Double,
    val asuransi: Double,
    val totalBiayaLain: Double,
    val totalUpfront: Double
)

data class AffordabilityResult(
    val monthlyIncome: Double,
    val otherInstallments: Double,
    val proposedKprInstallment: Double,
    val currentDsRatio: Double,
    val totalDsRatio: Double,
    val minIncomeRecommendation30: Double,
    val minIncomeRecommendation40: Double,
    val maxRecommendedInstallment30: Double,
    val maxRecommendedPropertyPrice: Double,
    val status: AffordabilityStatus,
    val statusMessage: String
)
