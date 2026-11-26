package com.example.busmanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.busmanager.data.local.dao.ActiveTripDao
import com.example.busmanager.data.local.dao.CompanyDao
import com.example.busmanager.data.local.dao.CustomRouteDao
import com.example.busmanager.data.local.dao.DriverDao
import com.example.busmanager.data.local.dao.FinancialTransactionDao
import com.example.busmanager.data.local.dao.OwnedBusDao
import com.example.busmanager.data.local.dao.RoutePricingDao
import com.example.busmanager.data.local.dao.RouteScheduleDao
import com.example.busmanager.data.local.dao.SimulationLogDao
import com.example.busmanager.data.local.dao.TerminalPassengerRecordDao
import com.example.busmanager.data.local.entity.ActiveTripEntity
import com.example.busmanager.data.local.entity.CompanyEntity
import com.example.busmanager.data.local.entity.CustomRouteEntity
import com.example.busmanager.data.local.entity.DriverEntity
import com.example.busmanager.data.local.entity.FinancialTransactionEntity
import com.example.busmanager.data.local.entity.OwnedBusEntity
import com.example.busmanager.data.local.entity.RoutePricingEntity
import com.example.busmanager.data.local.entity.RouteScheduleEntity
import com.example.busmanager.data.local.entity.SimulationLogEntity
import com.example.busmanager.data.local.entity.TerminalPassengerRecordEntity

@Database(
    entities = [
        CompanyEntity::class,
        OwnedBusEntity::class,
        DriverEntity::class,
        RoutePricingEntity::class,
        RouteScheduleEntity::class,
        FinancialTransactionEntity::class,
        TerminalPassengerRecordEntity::class,
        CustomRouteEntity::class,
        ActiveTripEntity::class,
        SimulationLogEntity::class
    ],
    version = 10,
    exportSchema = true
)
abstract class BusManagerDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun ownedBusDao(): OwnedBusDao
    abstract fun driverDao(): DriverDao
    abstract fun routePricingDao(): RoutePricingDao
    abstract fun routeScheduleDao(): RouteScheduleDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun terminalPassengerRecordDao(): TerminalPassengerRecordDao
    abstract fun customRouteDao(): CustomRouteDao
    abstract fun activeTripDao(): ActiveTripDao
    abstract fun simulationLogDao(): SimulationLogDao

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
