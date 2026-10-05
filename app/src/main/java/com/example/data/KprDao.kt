package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KprDao {
    @Query("SELECT * FROM kpr_simulations ORDER BY createdAt DESC")
    fun getAllSimulations(): Flow<List<KprSimulationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulation(simulation: KprSimulationEntity): Long

    @Query("DELETE FROM kpr_simulations WHERE id = :id")
    suspend fun deleteSimulationById(id: Long)

    @Query("SELECT * FROM kpr_simulations WHERE id = :id")
    suspend fun getSimulationById(id: Long): KprSimulationEntity?

    @Query("DELETE FROM kpr_simulations")
    suspend fun clearAll()
}
