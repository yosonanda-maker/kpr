package com.example

import com.example.model.AffordabilityStatus
import com.example.model.LoanType
import com.example.util.Formatters
import com.example.util.KprCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testConventionalCalculation() {
        val result = KprCalculator.calculateConventional(
            propertyPrice = 500_000_000.0,
            dpPercent = 20.0,
            tenorYears = 10,
            fixedRate = 6.0,
            fixedYears = 3,
            floatingRate = 10.0
        )

        assertEquals(100_000_000.0, result.dpAmount, 0.01)
        assertEquals(400_000_000.0, result.loanAmount, 0.01)
        assertTrue(result.monthlyInstallmentFixed > 4_000_000.0)
        assertTrue(result.monthlyInstallmentFloating > result.monthlyInstallmentFixed)
        assertEquals(120, result.schedule.size)
        assertEquals(10, result.yearlySchedule.size)
    }

    @Test
    fun testSyariahCalculation() {
        val result = KprCalculator.calculateSyariah(
            propertyPrice = 600_000_000.0,
            dpPercent = 10.0,
            tenorYears = 15,
            marginRatePerYear = 7.5
        )

        assertEquals(60_000_000.0, result.dpAmount, 0.01)
        assertEquals(540_000_000.0, result.loanAmount, 0.01)
        assertEquals(LoanType.SYARIAH, result.loanType)

        // Murabahah: Total Margin = 540jt * 7.5% * 15 = 607.5jt
        val expectedTotalMargin = 540_000_000.0 * 0.075 * 15
        assertEquals(expectedTotalMargin, result.totalInterestOrMargin, 0.01)

        val expectedMonthly = (540_000_000.0 + expectedTotalMargin) / (15 * 12)
        assertEquals(expectedMonthly, result.monthlyInstallmentFixed, 0.01)
    }

    @Test
    fun testUpfrontCostCalculation() {
        val upfront = KprCalculator.calculateUpfrontCosts(
            propertyPrice = 500_000_000.0,
            dpPercent = 10.0,
            npoptkp = 80_000_000.0,
            adminBankCustom = 750_000.0,
            notarisCustom = 4_000_000.0
        )

        assertEquals(50_000_000.0, upfront.dpAmount, 0.01)
        // BPHTB = 5% * (500jt - 80jt) = 21jt
        assertEquals(21_000_000.0, upfront.bphtb, 0.01)
        // AJB/BBN = 1% * 500jt = 5jt
        assertEquals(5_000_000.0, upfront.ajbBbn, 0.01)
        // Provisi = 1% * 450jt = 4.5jt
        assertEquals(4_500_000.0, upfront.provisi, 0.01)
        assertTrue(upfront.totalUpfront > upfront.dpAmount)
    }

    @Test
    fun testAffordabilityStatus() {
        val safe = KprCalculator.calculateAffordability(
            monthlyIncome = 20_000_000.0,
            otherInstallments = 1_000_000.0,
            proposedKprInstallment = 4_000_000.0
        )
        // Total debt = 5jt / 20jt = 25% (< 30%)
        assertEquals(AffordabilityStatus.AMAN, safe.status)

        val risky = KprCalculator.calculateAffordability(
            monthlyIncome = 10_000_000.0,
            otherInstallments = 2_000_000.0,
            proposedKprInstallment = 3_000_000.0
        )
        // Total debt = 5jt / 10jt = 50% (> 40%)
        assertEquals(AffordabilityStatus.BERISIKO, risky.status)
    }

    @Test
    fun testFormatters() {
        val formatted = Formatters.formatRupiah(500_000_000.0)
        assertTrue(formatted.contains("500.000.000"))

        val shortFormatted = Formatters.formatShortRupiah(1_200_000_000.0)
        assertTrue(shortFormatted.contains("Miliar"))

        val clean = Formatters.parseCleanNumber("Rp 750.000.000")
        assertEquals(750_000_000.0, clean, 0.01)
    }
}
