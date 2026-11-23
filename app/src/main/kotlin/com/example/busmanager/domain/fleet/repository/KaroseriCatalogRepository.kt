package com.example.busmanager.domain.fleet.repository

import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import kotlinx.coroutines.flow.Flow

interface KaroseriCatalogRepository {
    fun getAllBodies(): Flow<List<KaroseriBody>>
    fun getBodiesByBrand(brand: KaroseriBrand): Flow<List<KaroseriBody>>
    suspend fun getBodyById(id: String): KaroseriBody?
}
