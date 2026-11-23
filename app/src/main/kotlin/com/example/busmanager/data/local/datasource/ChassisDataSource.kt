package com.example.busmanager.data.local.datasource

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.EngineLocation
import com.example.busmanager.domain.fleet.model.SuspensionType
import com.example.busmanager.domain.fleet.model.TransmissionType

class ChassisDataSource {

    fun getChassisList(): List<Chassis> = listOf(
        Chassis(
            id = "chassis-hino-ak240",
            modelName = "AK 240",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.FRONT,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 240,
            torqueNm = 716,
            fuelCapacityLiters = 200,
            fuelConsumptionKmPerLiter = 3.6f,
            price = Money.rupiah(650_000_000)
        ),
        Chassis(
            id = "chassis-hino-rk8",
            modelName = "RK8 R260",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 750,
            fuelCapacityLiters = 270,
            fuelConsumptionKmPerLiter = 3.2f,
            price = Money.rupiah(750_000_000)
        ),
        Chassis(
            id = "chassis-hino-rn285",
            modelName = "RN 285",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 285,
            torqueNm = 892,
            fuelCapacityLiters = 270,
            fuelConsumptionKmPerLiter = 3.0f,
            price = Money.rupiah(850_000_000)
        ),
        Chassis(
            id = "chassis-hino-rm280",
            modelName = "RM 280 Space Frame",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 280,
            torqueNm = 850,
            fuelCapacityLiters = 350,
            fuelConsumptionKmPerLiter = 3.1f,
            price = Money.rupiah(980_000_000)
        ),
        Chassis(
            id = "chassis-mercy-1526",
            modelName = "OH 1526",
            brand = ChassisBrand.MERCEDES_BENZ,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 950,
            fuelCapacityLiters = 300,
            fuelConsumptionKmPerLiter = 3.1f,
            price = Money.rupiah(880_000_000)
        ),
        Chassis(
            id = "chassis-mercy-1626",
            modelName = "OH 1626 Space Frame",
            brand = ChassisBrand.MERCEDES_BENZ,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 950,
            fuelCapacityLiters = 380,
            fuelConsumptionKmPerLiter = 3.0f,
            price = Money.rupiah(1_100_000_000)
        ),
        Chassis(
            id = "chassis-mercy-1836",
            modelName = "O 500 R 1836",
            brand = ChassisBrand.MERCEDES_BENZ,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 360,
            torqueNm = 1600,
            fuelCapacityLiters = 400,
            fuelConsumptionKmPerLiter = 2.8f,
            price = Money.rupiah(1_450_000_000)
        ),
        Chassis(
            id = "chassis-mercy-2542",
            modelName = "OC 500 RF 2542 Tronton",
            brand = ChassisBrand.MERCEDES_BENZ,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.TRONTON_6X2,
            transmission = TransmissionType.AUTOMATIC,
            horsepower = 420,
            torqueNm = 1900,
            fuelCapacityLiters = 500,
            fuelConsumptionKmPerLiter = 2.5f,
            price = Money.rupiah(1_900_000_000)
        ),
        Chassis(
            id = "chassis-scania-k360",
            modelName = "K360IB Opticruise",
            brand = ChassisBrand.SCANIA,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.OPTICRUISE,
            horsepower = 360,
            torqueNm = 1750,
            fuelCapacityLiters = 400,
            fuelConsumptionKmPerLiter = 2.9f,
            price = Money.rupiah(1_550_000_000)
        ),
        Chassis(
            id = "chassis-scania-k410",
            modelName = "K410IB Tronton",
            brand = ChassisBrand.SCANIA,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.TRONTON_6X2,
            transmission = TransmissionType.OPTICRUISE,
            horsepower = 410,
            torqueNm = 2000,
            fuelCapacityLiters = 450,
            fuelConsumptionKmPerLiter = 2.6f,
            price = Money.rupiah(1_950_000_000)
        ),
        Chassis(
            id = "chassis-volvo-b8r",
            modelName = "B8R",
            brand = ChassisBrand.VOLVO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.AUTOMATIC,
            horsepower = 330,
            torqueNm = 1200,
            fuelCapacityLiters = 350,
            fuelConsumptionKmPerLiter = 2.9f,
            price = Money.rupiah(1_350_000_000)
        ),
        Chassis(
            id = "chassis-volvo-b11r",
            modelName = "B11R 450HP Tronton",
            brand = ChassisBrand.VOLVO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.AIR_SUSPENSION,
            axleType = AxleType.TRONTON_6X2,
            transmission = TransmissionType.I_SHIFT,
            horsepower = 450,
            torqueNm = 2150,
            fuelCapacityLiters = 480,
            fuelConsumptionKmPerLiter = 2.5f,
            price = Money.rupiah(2_000_000_000)
        )
    )
}
