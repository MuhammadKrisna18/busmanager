package com.example.busmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey
    val id: String = DEFAULT_ID,
    val name: String,
    val balance: Long,
    val reputation: Int = 100
) {
    companion object {
        const val DEFAULT_ID = "primary_company"
    }
}
