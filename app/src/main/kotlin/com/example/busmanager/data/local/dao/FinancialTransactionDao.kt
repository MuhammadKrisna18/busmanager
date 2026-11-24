package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.busmanager.data.local.entity.FinancialTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialTransactionDao {

    @Query("SELECT * FROM financial_transactions ORDER BY createdAtMillis DESC")
    fun getAllTransactionsFlow(): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions ORDER BY createdAtMillis DESC")
    suspend fun getAllTransactions(): List<FinancialTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransactionEntity)

    @Query("DELETE FROM financial_transactions")
    suspend fun clearAll()
}
