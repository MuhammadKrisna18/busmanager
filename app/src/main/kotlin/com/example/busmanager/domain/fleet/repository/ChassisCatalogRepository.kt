package com.example.busmanager.domain.fleet.repository

import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import kotlinx.coroutines.flow.Flow

interface ChassisCatalogRepository {
    fun getAllChassis(): Flow<List<Chassis>>
    fun getChassisByBrand(brand: ChassisBrand): Flow<List<Chassis>>
    suspend fun getChassisById(id: String): Chassis?
}
