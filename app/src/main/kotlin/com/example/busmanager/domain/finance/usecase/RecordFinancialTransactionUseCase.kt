package com.example.busmanager.domain.finance.usecase

import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository

class RecordFinancialTransactionUseCase(
    private val financialTransactionRepository: FinancialTransactionRepository,
    private val companyRepository: CompanyRepository
) {
    suspend operator fun invoke(transaction: FinancialTransaction) {
        financialTransactionRepository.recordTransaction(transaction)
        when (transaction.type) {
            TransactionType.INCOME -> companyRepository.addBalance(transaction.amount)
            TransactionType.EXPENSE -> companyRepository.deductBalance(transaction.amount)
        }
    }
}
