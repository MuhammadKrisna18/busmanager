package com.example.busmanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.busmanager.data.local.dao.CompanyDao
import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.local.entity.CompanyEntity
import com.example.busmanager.data.local.entity.OwnedBusEntity

@Database(
    entities = [CompanyEntity::class, OwnedBusEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BusManagerDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun ownedBusDao(): OwnedBusDao

    companion object {
        private const val DATABASE_NAME = "bus_manager.db"

        @Volatile
        private var instance: BusManagerDatabase? = null

        fun getInstance(context: Context): BusManagerDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BusManagerDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
