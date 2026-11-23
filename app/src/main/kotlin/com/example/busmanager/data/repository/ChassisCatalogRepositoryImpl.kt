package com.example.busmanager.data.repository

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.repository.ChassisCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ChassisCatalogRepositoryImpl(
    private val dataSource: ChassisDataSource = ChassisDataSource()
) : ChassisCatalogRepository {

    override fun getAllChassis(): Flow<List<Chassis>> {
        return flowOf(dataSource.getChassisList())
    }

    override fun getChassisByBrand(brand: ChassisBrand): Flow<List<Chassis>> {
        return flowOf(dataSource.getChassisList().filter { it.brand == brand })
    }

    override suspend fun getChassisById(id: String): Chassis? {
        return dataSource.getChassisList().find { it.id == id }
    }
}
