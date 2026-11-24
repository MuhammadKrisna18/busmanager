package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.FinancialTransactionDao
import com.example.busmanager.data.local.entity.FinancialTransactionEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinancialTransactionRepositoryImpl(
    private val financialTransactionDao: FinancialTransactionDao
) : FinancialTransactionRepository {

    override fun getAllTransactionsFlow(): Flow<List<FinancialTransaction>> {
        return financialTransactionDao.getAllTransactionsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactions(): List<FinancialTransaction> {
        return financialTransactionDao.getAllTransactions().map { it.toDomain() }
    }

    override suspend fun recordTransaction(transaction: FinancialTransaction) {
        financialTransactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun clearTransactions() {
        financialTransactionDao.clearAll()
    }

    private fun FinancialTransactionEntity.toDomain(): FinancialTransaction {
        val parsedType = try {
            TransactionType.valueOf(type)
        } catch (e: Exception) {
            TransactionType.INCOME
        }
        val parsedCategory = try {
            TransactionCategory.valueOf(category)
        } catch (e: Exception) {
            TransactionCategory.OTHER
        }
        return FinancialTransaction(
            id = id,
            dateText = dateText,
            timeText = timeText,
            day = day,
            month = month,
            year = year,
            type = parsedType,
            category = parsedCategory,
            amount = Money.rupiah(amount),
            busId = busId,
            busCode = busCode,
            routeId = routeId,
            routeName = routeName,
            description = description,
            createdAtMillis = createdAtMillis
        )
    }

    private fun FinancialTransaction.toEntity(): FinancialTransactionEntity {
        return FinancialTransactionEntity(
            id = id,
            dateText = dateText,
            timeText = timeText,
            day = day,
            month = month,
            year = year,
            type = type.name,
            category = category.name,
            amount = amount.amount,
            busId = busId,
            busCode = busCode,
            routeId = routeId,
            routeName = routeName,
            description = description,
            createdAtMillis = createdAtMillis
        )
    }
}
