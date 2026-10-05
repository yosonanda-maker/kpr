package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kpr_simulations")
data class KprSimulationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val loanType: String, // "KONVENSIONAL" or "SYARIAH"
    val propertyPrice: Double,
    val dpPercent: Double,
    val dpAmount: Double,
    val loanAmount: Double,
    val tenorYears: Int,
    val fixedRate: Double,
    val fixedYears: Int,
    val floatingRate: Double,
    val syariahMarginRate: Double,
    val monthlyInstallmentFixed: Double,
    val monthlyInstallmentFloating: Double,
    val totalInterestOrMargin: Double,
    val totalPayment: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
