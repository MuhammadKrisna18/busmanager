package com.example.busmanager.data.repository

import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.repository.KaroseriCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class KaroseriCatalogRepositoryImpl(
    private val dataSource: KaroseriBodyDataSource = KaroseriBodyDataSource()
) : KaroseriCatalogRepository {

    override fun getAllBodies(): Flow<List<KaroseriBody>> {
        return flowOf(dataSource.getKaroseriBodyList())
    }

    override fun getBodiesByBrand(brand: KaroseriBrand): Flow<List<KaroseriBody>> {
        return flowOf(dataSource.getKaroseriBodyList().filter { it.brand == brand })
    }

    override suspend fun getBodyById(id: String): KaroseriBody? {
        return dataSource.getKaroseriBodyList().find { it.id == id }
    }
}
