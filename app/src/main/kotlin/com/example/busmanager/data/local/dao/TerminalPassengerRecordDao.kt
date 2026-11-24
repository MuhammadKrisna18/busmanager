package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.busmanager.data.local.entity.TerminalPassengerRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TerminalPassengerRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TerminalPassengerRecordEntity)

    @Query("SELECT * FROM terminal_passenger_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TerminalPassengerRecordEntity>>

    @Query("SELECT * FROM terminal_passenger_records WHERE terminalId = :terminalId ORDER BY timestamp DESC")
    fun getRecordsByTerminal(terminalId: String): Flow<List<TerminalPassengerRecordEntity>>

    @Query("SELECT * FROM terminal_passenger_records WHERE terminalId = :terminalId AND day = :day AND month = :month AND year = :year ORDER BY timestamp DESC")
    fun getRecordsByTerminalAndDate(
        terminalId: String,
        day: Int,
        month: Int,
        year: Int
    ): Flow<List<TerminalPassengerRecordEntity>>

    @Query("SELECT * FROM terminal_passenger_records")
    suspend fun getAllRecordsSnapshot(): List<TerminalPassengerRecordEntity>
}
