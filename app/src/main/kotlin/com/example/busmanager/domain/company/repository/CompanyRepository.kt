package com.example.busmanager.domain.company.repository

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import kotlinx.coroutines.flow.Flow

interface CompanyRepository {
    fun getCompany(): Flow<Company?>
    suspend fun createCompany(name: String, initialBalance: Money): Company
    suspend fun updateBalance(newBalance: Money)
    suspend fun deductBalance(amount: Money): Boolean
    suspend fun addBalance(amount: Money)
}
