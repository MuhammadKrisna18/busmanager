package com.example.busmanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.busmanager.data.local.dao.CompanyDao
import com.example.busmanager.data.local.dao.DriverDao
import com.example.busmanager.data.local.dao.FinancialTransactionDao
import com.example.busmanager.data.local.dao.TerminalPassengerRecordDao
import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.local.dao.RoutePricingDao
import com.example.busmanager.data.local.dao.RouteScheduleDao
import com.example.busmanager.data.local.entity.CompanyEntity
import com.example.busmanager.data.local.entity.DriverEntity
import com.example.busmanager.data.local.entity.FinancialTransactionEntity
import com.example.busmanager.data.local.entity.OwnedBusEntity
import com.example.busmanager.data.local.entity.RoutePricingEntity
import com.example.busmanager.data.local.entity.RouteScheduleEntity
import com.example.busmanager.data.local.entity.TerminalPassengerRecordEntity

@Database(
    entities = [
        CompanyEntity::class,
        OwnedBusEntity::class,
        DriverEntity::class,
        RoutePricingEntity::class,
        RouteScheduleEntity::class,
        FinancialTransactionEntity::class,
        TerminalPassengerRecordEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class BusManagerDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun ownedBusDao(): OwnedBusDao
    abstract fun driverDao(): DriverDao
    abstract fun routePricingDao(): RoutePricingDao
    abstract fun routeScheduleDao(): RouteScheduleDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun terminalPassengerRecordDao(): TerminalPassengerRecordDao

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
                ).fallbackToDestructiveMigration(true).build().also { instance = it }
            }
        }
    }
}
