package com.example.busmanager.domain.fleet.model

import com.example.busmanager.domain.company.model.Money

data class Chassis(
    val id: String,
    val modelName: String,
    val brand: ChassisBrand,
    val engineLocation: EngineLocation,
    val suspension: SuspensionType,
    val axleType: AxleType,
    val transmission: TransmissionType,
    val horsepower: Int,
    val torqueNm: Int,
    val fuelCapacityLiters: Int,
    val fuelConsumptionKmPerLiter: Float,
    val price: Money
) {
    val isSpaceFrame: Boolean
        get() = suspension == SuspensionType.AIR_SUSPENSION && engineLocation == EngineLocation.REAR
}
