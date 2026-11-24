package com.example.busmanager.domain.finance.repository

import com.example.busmanager.domain.finance.model.FinancialTransaction
import kotlinx.coroutines.flow.Flow

interface FinancialTransactionRepository {
    fun getAllTransactionsFlow(): Flow<List<FinancialTransaction>>
    suspend fun getTransactions(): List<FinancialTransaction>
    suspend fun recordTransaction(transaction: FinancialTransaction)
    suspend fun clearTransactions()
}
