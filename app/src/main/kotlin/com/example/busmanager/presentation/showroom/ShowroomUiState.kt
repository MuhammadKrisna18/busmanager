package com.example.busmanager.presentation.showroom

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.EngineLocation
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand

enum class ShowroomMode {
    CUSTOM_ASSEMBLY,
    INSTANT_PURCHASE
}

enum class ShowroomStep(val stepNumber: Int, val title: String) {
    CHASSIS(1, "1. Sasis"),
    BODY(2, "2. Bodi"),
    CLASS(3, "3. Kelas"),
    REVIEW(4, "4. Konfirmasi")
}

data class ShowroomUiState(
    val mode: ShowroomMode = ShowroomMode.CUSTOM_ASSEMBLY,
    val company: Company? = null,
    val currentStep: ShowroomStep = ShowroomStep.CHASSIS,
    val allChassis: List<Chassis> = emptyList(),
    val allBodies: List<KaroseriBody> = emptyList(),
    val readyStockBuses: List<Bus> = emptyList(),
    val selectedChassisFilter: ChassisBrand? = null,
    val selectedKaroseriFilter: KaroseriBrand? = null,
    val selectedChassis: Chassis? = null,
    val selectedBody: KaroseriBody? = null,
    val selectedBusClass: BusClass = BusClass.EKSEKUTIF,
    val customBusCode: String = "",
    val batchQuantity: Int = 1,
    val instantBatchQuantities: Map<String, Int> = emptyMap(),
    val isPurchasing: Boolean = false,
    val purchaseSuccessBus: Bus? = null,
    val purchaseSuccessBuses: List<Bus> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val ownedBusCount: Int = 0
) {
    val filteredChassis: List<Chassis>
        get() = if (selectedChassisFilter == null) allChassis else allChassis.filter { it.brand == selectedChassisFilter }

    val compatibleBodies: List<KaroseriBody>
        get() {
            val chassis = selectedChassis ?: return emptyList()
            val baseList = allBodies.filter { body ->
                if (body.requiresTronton && chassis.axleType != AxleType.TRONTON_6X2) {
                    false
                } else if (body.bodyType == BusBodyType.UHD && chassis.axleType != AxleType.TRONTON_6X2) {
                    false
                } else if (chassis.engineLocation == EngineLocation.FRONT && body.bodyType != BusBodyType.HDD) {
                    false
                } else {
                    true
                }
            }
            return if (selectedKaroseriFilter == null) baseList else baseList.filter { it.brand == selectedKaroseriFilter }
        }

    val classPriceAddition: Money
        get() = Money.ZERO

    val calculatedCapacity: Int
        get() {
            val body = selectedBody ?: return 30
            val chassis = selectedChassis
            val isTronton = chassis?.axleType == AxleType.TRONTON_6X2 || body.requiresTronton
            val isDoubleDecker = body.bodyType == BusBodyType.SDD

            return when (selectedBusClass) {
                BusClass.EKONOMI -> if (isDoubleDecker) 60 else if (isTronton) 54 else 48
                BusClass.BISNIS -> if (isDoubleDecker) 50 else if (isTronton) 44 else 38
                BusClass.VIP -> if (isDoubleDecker) 42 else if (isTronton) 38 else 34
                BusClass.EKSEKUTIF -> if (isDoubleDecker) 36 else if (isTronton) 32 else 28
                BusClass.SUPER_EKSEKUTIF -> if (isDoubleDecker) 28 else if (isTronton) 26 else 22
                BusClass.SUITES_CLASS -> if (isDoubleDecker) 24 else 21
            }
        }

    val tailoredBody: KaroseriBody?
        get() {
            val body = selectedBody ?: return null
            return body.copy(
                busClass = selectedBusClass,
                passengerCapacity = calculatedCapacity,
                price = body.price
            )
        }

    val unitInvestment: Money
        get() {
            val chassisPrice = selectedChassis?.price ?: Money.ZERO
            val bodyPrice = tailoredBody?.price ?: Money.ZERO
            return chassisPrice + bodyPrice
        }

    val totalInvestment: Money
        get() = unitInvestment * batchQuantity.toLong()

    val canAfford: Boolean
        get() {
            val balance = company?.balance ?: return false
            return balance.canAfford(totalInvestment)
        }

    fun getInstantQuantity(busId: String): Int = instantBatchQuantities[busId] ?: 1

    fun getInstantTotalCost(bus: Bus): Money = bus.totalPrice * getInstantQuantity(bus.id).toLong()

    fun canAffordInstant(bus: Bus): Boolean {
        val balance = company?.balance ?: return false
        return balance.canAfford(getInstantTotalCost(bus))
    }
}
