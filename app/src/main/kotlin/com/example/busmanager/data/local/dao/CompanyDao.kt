package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.busmanager.data.local.entity.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    @Query("SELECT * FROM companies WHERE id = :id LIMIT 1")
    fun getCompanyFlow(id: String): Flow<CompanyEntity?>

    @Query("SELECT * FROM companies WHERE id = :id LIMIT 1")
    suspend fun getCompany(id: String): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity)

    @Update
    suspend fun updateCompany(company: CompanyEntity)

    @Query("UPDATE companies SET balance = :newBalance WHERE id = :id")
    suspend fun updateBalance(newBalance: Long, id: String): Int
}

