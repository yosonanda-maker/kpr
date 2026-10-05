package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.KprRepository
import com.example.data.KprSimulationEntity
import com.example.model.AffordabilityResult
import com.example.model.KprCalculationResult
import com.example.model.LoanType
import com.example.model.UpfrontCostBreakdown
import com.example.util.KprCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val loanType: LoanType = LoanType.KONVENSIONAL,
    val propertyPrice: Double = 600_000_000.0,
    val dpPercent: Double = 10.0,
    val tenorYears: Int = 15,
    val fixedRate: Double = 4.75,
    val fixedYears: Int = 3,
    val floatingRate: Double = 11.0,
    val syariahMarginRate: Double = 7.5,
    val calculationResult: KprCalculationResult = KprCalculator.calculateConventional(
        propertyPrice = 600_000_000.0,
        dpPercent = 10.0,
        tenorYears = 15,
        fixedRate = 4.75,
        fixedYears = 3,
        floatingRate = 11.0
    ),
    // Affordability state
    val monthlyIncome: Double = 15_000_000.0,
    val hasJointIncome: Boolean = false,
    val spouseIncome: Double = 10_000_000.0,
    val otherInstallments: Double = 1_500_000.0,
    val affordabilityResult: AffordabilityResult = KprCalculator.calculateAffordability(
        monthlyIncome = 15_000_000.0,
        otherInstallments = 1_500_000.0,
        proposedKprInstallment = 4_350_000.0
    ),
    // Upfront Cost state
    val npoptkp: Double = 80_000_000.0,
    val adminBankCustom: Double = 750_000.0,
    val notarisCustom: Double = 4_000_000.0,
    val upfrontCostBreakdown: UpfrontCostBreakdown = KprCalculator.calculateUpfrontCosts(
        propertyPrice = 600_000_000.0,
        dpPercent = 10.0,
        npoptkp = 80_000_000.0,
        adminBankCustom = 750_000.0,
        notarisCustom = 4_000_000.0
    ),
    // UI Dialogs
    val showScheduleDialog: Boolean = false,
    val showSaveDialog: Boolean = false,
    val comparisonIds: Set<Long> = emptySet(),
    val activeTab: Int = 0 // 0: Kalkulator, 1: Kemampuan, 2: Biaya Awal, 3: Riwayat
)

data class BankPreset(
    val bankName: String,
    val promoLabel: String,
    val fixedRate: Double,
    val fixedYears: Int,
    val floatingRate: Double,
    val isSyariah: Boolean = false
)

class KprViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KprRepository = KprRepository(
        AppDatabase.getInstance(application).kprDao()
    )

    val savedSimulations: StateFlow<List<KprSimulationEntity>> = repository.allSimulations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    val bankPresets = listOf(
        BankPreset("BCA KPR Fix 3 Th", "Promo 4.65% (Fix 3 Th)", 4.65, 3, 10.5),
        BankPreset("Mandiri KPR Fix 5 Th", "Promo 4.88% (Fix 5 Th)", 4.88, 5, 11.25),
        BankPreset("BTN Promo KPR", "Promo 4.25% (Fix 2 Th)", 4.25, 2, 11.5),
        BankPreset("BSI Hasanah Syariah", "Margin Tetap 7.25%", 7.25, 15, 7.25, isSyariah = true),
        BankPreset("BTN Subsidi (FLPP)", "Bunga Tetap 5.0% Flat", 5.0, 20, 5.0)
    )

    init {
        recalculateAll()
    }

    fun setActiveTab(index: Int) {
        _uiState.value = _uiState.value.copy(activeTab = index)
    }

    fun setLoanType(type: LoanType) {
        _uiState.value = _uiState.value.copy(loanType = type)
        recalculateAll()
    }

    fun updatePropertyPrice(price: Double) {
        _uiState.value = _uiState.value.copy(propertyPrice = price.coerceAtLeast(0.0))
        recalculateAll()
    }

    fun updateDpPercent(percent: Double) {
        val clamped = percent.coerceIn(0.0, 90.0)
        _uiState.value = _uiState.value.copy(dpPercent = clamped)
        recalculateAll()
    }

    fun updateDpAmount(amount: Double) {
        val currentPrice = _uiState.value.propertyPrice
        val percent = if (currentPrice > 0) (amount / currentPrice) * 100.0 else 0.0
        val clamped = percent.coerceIn(0.0, 90.0)
        _uiState.value = _uiState.value.copy(dpPercent = clamped)
        recalculateAll()
    }

    fun updateTenorYears(tenor: Int) {
        val clamped = tenor.coerceIn(1, 35)
        val currentFixedYears = _uiState.value.fixedYears
        val adjustedFixed = if (currentFixedYears > clamped) clamped else currentFixedYears
        _uiState.value = _uiState.value.copy(tenorYears = clamped, fixedYears = adjustedFixed)
        recalculateAll()
    }

    fun updateFixedRate(rate: Double) {
        _uiState.value = _uiState.value.copy(fixedRate = rate.coerceAtLeast(0.0))
        recalculateAll()
    }

    fun updateFixedYears(years: Int) {
        val clamped = years.coerceIn(1, _uiState.value.tenorYears)
        _uiState.value = _uiState.value.copy(fixedYears = clamped)
        recalculateAll()
    }

    fun updateFloatingRate(rate: Double) {
        _uiState.value = _uiState.value.copy(floatingRate = rate.coerceAtLeast(0.0))
        recalculateAll()
    }

    fun updateSyariahMarginRate(rate: Double) {
        _uiState.value = _uiState.value.copy(syariahMarginRate = rate.coerceAtLeast(0.0))
        recalculateAll()
    }

    fun applyPreset(preset: BankPreset) {
        if (preset.isSyariah) {
            _uiState.value = _uiState.value.copy(
                loanType = LoanType.SYARIAH,
                syariahMarginRate = preset.fixedRate
            )
        } else {
            _uiState.value = _uiState.value.copy(
                loanType = LoanType.KONVENSIONAL,
                fixedRate = preset.fixedRate,
                fixedYears = preset.fixedYears.coerceAtMost(_uiState.value.tenorYears),
                floatingRate = preset.floatingRate
            )
        }
        recalculateAll()
    }

    // Affordability updates
    fun updateMonthlyIncome(income: Double) {
        _uiState.value = _uiState.value.copy(monthlyIncome = income.coerceAtLeast(0.0))
        recalculateAffordability()
    }

    fun setJointIncomeEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(hasJointIncome = enabled)
        recalculateAffordability()
    }

    fun updateSpouseIncome(income: Double) {
        _uiState.value = _uiState.value.copy(spouseIncome = income.coerceAtLeast(0.0))
        recalculateAffordability()
    }

    fun updateOtherInstallments(installments: Double) {
        _uiState.value = _uiState.value.copy(otherInstallments = installments.coerceAtLeast(0.0))
        recalculateAffordability()
    }

    // Upfront Cost updates
    fun updateNpoptkp(npoptkp: Double) {
        _uiState.value = _uiState.value.copy(npoptkp = npoptkp.coerceAtLeast(0.0))
        recalculateUpfrontCosts()
    }

    fun updateAdminBank(admin: Double) {
        _uiState.value = _uiState.value.copy(adminBankCustom = admin.coerceAtLeast(0.0))
        recalculateUpfrontCosts()
    }

    fun updateNotaris(notaris: Double) {
        _uiState.value = _uiState.value.copy(notarisCustom = notaris.coerceAtLeast(0.0))
        recalculateUpfrontCosts()
    }

    fun setShowScheduleDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showScheduleDialog = show)
    }

    fun setShowSaveDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSaveDialog = show)
    }

    fun toggleComparison(id: Long) {
        val current = _uiState.value.comparisonIds.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            if (current.size >= 2) {
                // Remove the oldest or keep max 2
                val first = current.first()
                current.remove(first)
            }
            current.add(id)
        }
        _uiState.value = _uiState.value.copy(comparisonIds = current)
    }

    fun clearComparison() {
        _uiState.value = _uiState.value.copy(comparisonIds = emptySet())
    }

    fun saveCurrentSimulation(title: String, notes: String) {
        val current = _uiState.value
        val result = current.calculationResult
        val entity = KprSimulationEntity(
            title = if (title.isBlank()) "Simulasi KPR ${if (current.loanType == LoanType.KONVENSIONAL) "Konvensional" else "Syariah"}" else title.trim(),
            loanType = current.loanType.name,
            propertyPrice = current.propertyPrice,
            dpPercent = current.dpPercent,
            dpAmount = result.dpAmount,
            loanAmount = result.loanAmount,
            tenorYears = current.tenorYears,
            fixedRate = current.fixedRate,
            fixedYears = current.fixedYears,
            floatingRate = current.floatingRate,
            syariahMarginRate = current.syariahMarginRate,
            monthlyInstallmentFixed = result.monthlyInstallmentFixed,
            monthlyInstallmentFloating = result.monthlyInstallmentFloating,
            totalInterestOrMargin = result.totalInterestOrMargin,
            totalPayment = result.totalPayment,
            notes = notes.trim()
        )
        viewModelScope.launch {
            repository.insert(entity)
            setShowSaveDialog(false)
        }
    }

    fun deleteSimulation(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (_uiState.value.comparisonIds.contains(id)) {
                val current = _uiState.value.comparisonIds.toMutableSet()
                current.remove(id)
                _uiState.value = _uiState.value.copy(comparisonIds = current)
            }
        }
    }

    fun loadSimulation(sim: KprSimulationEntity) {
        val lType = if (sim.loanType == LoanType.SYARIAH.name) LoanType.SYARIAH else LoanType.KONVENSIONAL
        _uiState.value = _uiState.value.copy(
            loanType = lType,
            propertyPrice = sim.propertyPrice,
            dpPercent = sim.dpPercent,
            tenorYears = sim.tenorYears,
            fixedRate = sim.fixedRate,
            fixedYears = sim.fixedYears,
            floatingRate = sim.floatingRate,
            syariahMarginRate = sim.syariahMarginRate,
            activeTab = 0
        )
        recalculateAll()
    }

    private fun recalculateAll() {
        val state = _uiState.value
        val calcResult = if (state.loanType == LoanType.KONVENSIONAL) {
            KprCalculator.calculateConventional(
                propertyPrice = state.propertyPrice,
                dpPercent = state.dpPercent,
                tenorYears = state.tenorYears,
                fixedRate = state.fixedRate,
                fixedYears = state.fixedYears,
                floatingRate = state.floatingRate
            )
        } else {
            KprCalculator.calculateSyariah(
                propertyPrice = state.propertyPrice,
                dpPercent = state.dpPercent,
                tenorYears = state.tenorYears,
                marginRatePerYear = state.syariahMarginRate
            )
        }

        _uiState.value = _uiState.value.copy(calculationResult = calcResult)
        recalculateUpfrontCosts()
        recalculateAffordability()
    }

    private fun recalculateUpfrontCosts() {
        val state = _uiState.value
        val upfront = KprCalculator.calculateUpfrontCosts(
            propertyPrice = state.propertyPrice,
            dpPercent = state.dpPercent,
            npoptkp = state.npoptkp,
            adminBankCustom = state.adminBankCustom,
            notarisCustom = state.notarisCustom
        )
        _uiState.value = _uiState.value.copy(upfrontCostBreakdown = upfront)
    }

    private fun recalculateAffordability() {
        val state = _uiState.value
        val totalIncome = if (state.hasJointIncome) {
            state.monthlyIncome + state.spouseIncome
        } else {
            state.monthlyIncome
        }
        val proposedInstallment = state.calculationResult.monthlyInstallmentFixed
        val affordResult = KprCalculator.calculateAffordability(
            monthlyIncome = totalIncome,
            otherInstallments = state.otherInstallments,
            proposedKprInstallment = proposedInstallment
        )
        _uiState.value = _uiState.value.copy(affordabilityResult = affordResult)
    }
}
