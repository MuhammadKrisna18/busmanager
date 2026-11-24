package com.example.busmanager.domain.finance.usecase

import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import kotlinx.coroutines.flow.Flow

class GetFinancialTransactionsUseCase(
    private val financialTransactionRepository: FinancialTransactionRepository
) {
    operator fun invoke(): Flow<List<FinancialTransaction>> {
        return financialTransactionRepository.getAllTransactionsFlow()
    }
}
