package com.example.busmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.busmanager.data.local.entity.ActiveTripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveTripDao {

    @Query("SELECT * FROM active_trips")
    fun getAllTripsFlow(): Flow<List<ActiveTripEntity>>

    @Query("SELECT * FROM active_trips")
    suspend fun getAllTrips(): List<ActiveTripEntity>

    @Query("SELECT * FROM active_trips WHERE id = :tripId")
    suspend fun getTripById(tripId: String): ActiveTripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: ActiveTripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<ActiveTripEntity>)

    @Update
    suspend fun updateTrip(trip: ActiveTripEntity)

    @Query("DELETE FROM active_trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)

    @Query("DELETE FROM active_trips WHERE busId = :busId")
    suspend fun deleteTripsForBus(busId: String)

    @Query("DELETE FROM active_trips")
    suspend fun deleteAllTrips()
}
