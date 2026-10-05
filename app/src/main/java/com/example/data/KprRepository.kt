package com.example.data

import kotlinx.coroutines.flow.Flow

class KprRepository(private val kprDao: KprDao) {
    val allSimulations: Flow<List<KprSimulationEntity>> = kprDao.getAllSimulations()

    suspend fun insert(simulation: KprSimulationEntity): Long {
        return kprDao.insertSimulation(simulation)
    }

    suspend fun deleteById(id: Long) {
        kprDao.deleteSimulationById(id)
    }

    suspend fun clearAll() {
        kprDao.clearAll()
    }
}
