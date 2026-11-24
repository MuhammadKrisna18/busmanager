package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.CompanyDao
import com.example.busmanager.data.local.entity.CompanyEntity
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CompanyRepositoryImpl(
    private val companyDao: CompanyDao
) : CompanyRepository {

    override fun getCompany(): Flow<Company?> {
        return companyDao.getCompanyFlow(CompanyEntity.DEFAULT_ID).map { it?.toDomain() }
    }

    override suspend fun createCompany(name: String, initialBalance: Money): Company {
        val entity = CompanyEntity(
            id = CompanyEntity.DEFAULT_ID,
            name = name,
            balance = initialBalance.amount,
            reputation = 100
        )
        companyDao.insertCompany(entity)
        return entity.toDomain()
    }

    override suspend fun updateBalance(newBalance: Money) {
        companyDao.updateBalance(newBalance.amount, CompanyEntity.DEFAULT_ID)
    }

    override suspend fun deductBalance(amount: Money): Boolean {
        val current = companyDao.getCompany(CompanyEntity.DEFAULT_ID) ?: return false
        val currentMoney = Money.rupiah(current.balance)
        if (!currentMoney.canAfford(amount)) {
            return false
        }
        val remaining = currentMoney - amount
        companyDao.updateBalance(remaining.amount, CompanyEntity.DEFAULT_ID)
        return true
    }

    override suspend fun addBalance(amount: Money) {
        val current = companyDao.getCompany(CompanyEntity.DEFAULT_ID) ?: return
        val updated = Money.rupiah(current.balance) + amount
        companyDao.updateBalance(updated.amount, CompanyEntity.DEFAULT_ID)
    }

    override suspend fun resetCompany(name: String, initialBalance: Money) {
        val entity = CompanyEntity(
            id = CompanyEntity.DEFAULT_ID,
            name = name,
            balance = initialBalance.amount,
            reputation = 100
        )
        companyDao.insertCompany(entity)
    }
}
