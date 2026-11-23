package com.example.busmanager.data.repository

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.repository.ShowroomCatalogRepository
import com.example.busmanager.domain.fleet.service.BusCompatibilityValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class ShowroomCatalogRepositoryImpl(
    private val chassisDataSource: ChassisDataSource = ChassisDataSource(),
    private val bodyDataSource: KaroseriBodyDataSource = KaroseriBodyDataSource(),
    private val validator: BusCompatibilityValidator = BusCompatibilityValidator()
) : ShowroomCatalogRepository {

    override fun getReadyStockBuses(): Flow<List<Bus>> {
        val chassisList = chassisDataSource.getChassisList()
        val bodyList = bodyDataSource.getKaroseriBodyList()

        val readyStocks = listOf(
            createPresetBus("stock-1", "TN-01", "chassis-hino-rk8", "body-ap-jb3-shd", chassisList, bodyList),
            createPresetBus("stock-2", "TN-02", "chassis-hino-rm280", "body-tt-avante-h8", chassisList, bodyList),
            createPresetBus("stock-3", "TN-03", "chassis-mercy-1626", "body-lk-sr3-xhd", chassisList, bodyList),
            createPresetBus("stock-4", "TN-04", "chassis-mercy-1626", "body-lk-sr3-suites", chassisList, bodyList),
            createPresetBus("stock-5", "TN-05", "chassis-scania-k360", "body-ap-jb5-shd", chassisList, bodyList),
            createPresetBus("stock-6", "TN-06", "chassis-scania-k410", "body-ap-jb5-sdd", chassisList, bodyList),
            createPresetBus("stock-7", "TN-07", "chassis-volvo-b11r", "body-lk-sr3-dd", chassisList, bodyList)
        ).filterNotNull()

        return flowOf(readyStocks)
    }

    override fun getCompatibleBodies(chassisId: String): Flow<List<KaroseriBody>> {
        val chassis = chassisDataSource.getChassisList().find { it.id == chassisId }
            ?: return flowOf(emptyList())

        val compatible = bodyDataSource.getKaroseriBodyList().filter { body ->
            validator.isCompatible(chassis, body)
        }
        return flowOf(compatible)
    }

    override suspend fun createCustomBus(
        busCode: String,
        chassisId: String,
        bodyId: String
    ): Bus? {
        val chassis = chassisDataSource.getChassisList().find { it.id == chassisId } ?: return null
        val body = bodyDataSource.getKaroseriBodyList().find { it.id == bodyId } ?: return null

        if (!validator.isCompatible(chassis, body)) {
            return null
        }

        return Bus(
            id = UUID.randomUUID().toString(),
            busCode = busCode,
            chassis = chassis,
            body = body
        )
    }

    private fun createPresetBus(
        id: String,
        busCode: String,
        chassisId: String,
        bodyId: String,
        chassisList: List<com.example.busmanager.domain.fleet.model.Chassis>,
        bodyList: List<KaroseriBody>
    ): Bus? {
        val chassis = chassisList.find { it.id == chassisId } ?: return null
        val body = bodyList.find { it.id == bodyId } ?: return null
        return Bus(
            id = id,
            busCode = busCode,
            chassis = chassis,
            body = body
        )
    }
}
