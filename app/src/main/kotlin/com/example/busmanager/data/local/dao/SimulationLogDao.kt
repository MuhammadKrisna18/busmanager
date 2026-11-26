package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.busmanager.data.local.entity.SimulationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SimulationLogDao {

    @Query("SELECT * FROM simulation_logs ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentLogsFlow(limit: Int): Flow<List<SimulationLogEntity>>

    @Query("SELECT * FROM simulation_logs ORDER BY timestampMillis DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<SimulationLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SimulationLogEntity)

    @Query("DELETE FROM simulation_logs")
    suspend fun deleteAllLogs()
}
