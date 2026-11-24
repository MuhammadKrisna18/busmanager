package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.busmanager.data.local.entity.RoutePricingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePricingDao {

    @Query("SELECT * FROM route_pricings WHERE routeId = :routeId")
    fun getPricingsByRouteFlow(routeId: String): Flow<List<RoutePricingEntity>>

    @Query("SELECT * FROM route_pricings WHERE routeId = :routeId")
    suspend fun getPricingsByRoute(routeId: String): List<RoutePricingEntity>

    @Query("SELECT * FROM route_pricings WHERE routeId = :routeId AND originTerminalId = :originId AND destinationTerminalId = :destinationId LIMIT 1")
    suspend fun getPricing(routeId: String, originId: String, destinationId: String): RoutePricingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricing(pricing: RoutePricingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricings(pricings: List<RoutePricingEntity>)

    @Query("DELETE FROM route_pricings WHERE routeId = :routeId")
    suspend fun deletePricingsByRoute(routeId: String): Int
}
