package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.busmanager.data.local.entity.OwnedBusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnedBusDao {

    @Query("SELECT * FROM owned_buses ORDER BY purchasedAtTimestamp DESC")
    fun getAllBusesFlow(): Flow<List<OwnedBusEntity>>

    @Query("SELECT * FROM owned_buses WHERE id = :busId LIMIT 1")
    suspend fun getBusById(busId: String): OwnedBusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBus(bus: OwnedBusEntity)

    @Update
    suspend fun updateBus(bus: OwnedBusEntity)

    @Query("DELETE FROM owned_buses WHERE id = :busId")
    suspend fun deleteBusById(busId: String): Int

    @Query("DELETE FROM owned_buses")
    suspend fun deleteAllBuses(): Int
}
