package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.busmanager.data.local.entity.CustomRouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRouteDao {

    @Query("SELECT * FROM custom_routes ORDER BY createdAtMillis ASC")
    fun getAllCustomRoutesFlow(): Flow<List<CustomRouteEntity>>

    @Query("SELECT * FROM custom_routes ORDER BY createdAtMillis ASC")
    fun getAllCustomRoutesSync(): List<CustomRouteEntity>

    @Query("SELECT * FROM custom_routes ORDER BY createdAtMillis ASC")
    suspend fun getAllCustomRoutes(): List<CustomRouteEntity>

    @Query("SELECT * FROM custom_routes WHERE id = :id LIMIT 1")
    suspend fun getCustomRouteById(id: String): CustomRouteEntity?

    @Query("SELECT * FROM custom_routes WHERE id = :id LIMIT 1")
    fun getCustomRouteByIdSync(id: String): CustomRouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomRoute(route: CustomRouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomRouteSync(route: CustomRouteEntity)

    @Query("DELETE FROM custom_routes WHERE id = :id")
    fun deleteCustomRouteById(id: String): Int

    @Query("DELETE FROM custom_routes")
    fun deleteAllCustomRoutes(): Int
}
