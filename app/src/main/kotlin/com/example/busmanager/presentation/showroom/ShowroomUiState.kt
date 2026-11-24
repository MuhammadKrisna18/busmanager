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

enum class ShowroomStep(val stepNumber: Int, val title: String) {
    CHASSIS(1, "1. Sasis"),
    BODY(2, "2. Bodi"),
    CLASS(3, "3. Kelas"),
    REVIEW(4, "4. Konfirmasi")
}

data class ShowroomUiState(
    val company: Company? = null,
    val currentStep: ShowroomStep = ShowroomStep.CHASSIS,
    val allChassis: List<Chassis> = emptyList(),
    val allBodies: List<KaroseriBody> = emptyList(),
    val selectedChassisFilter: ChassisBrand? = null,
    val selectedKaroseriFilter: KaroseriBrand? = null,
    val selectedChassis: Chassis? = null,
    val selectedBody: KaroseriBody? = null,
    val selectedBusClass: BusClass = BusClass.EKSEKUTIF,
    val customBusCode: String = "",
    val isPurchasing: Boolean = false,
    val purchaseSuccessBus: Bus? = null,
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
        get() = when (selectedBusClass) {
            BusClass.EKONOMI -> Money.rupiah(0L)
            BusClass.BISNIS -> Money.rupiah(20_000_000L)
            BusClass.VIP -> Money.rupiah(40_000_000L)
            BusClass.EKSEKUTIF -> Money.rupiah(60_000_000L)
            BusClass.SUPER_EKSEKUTIF -> Money.rupiah(90_000_000L)
            BusClass.SUITES_CLASS -> Money.rupiah(140_000_000L)
        }

    val calculatedCapacity: Int
        get() {
            val body = selectedBody ?: return 30
            return when (selectedBusClass) {
                BusClass.EKONOMI -> if (body.bodyType == BusBodyType.SDD) 65 else 50
                BusClass.BISNIS -> if (body.bodyType == BusBodyType.SDD) 54 else 40
                BusClass.VIP -> if (body.bodyType == BusBodyType.SDD) 46 else 34
                BusClass.EKSEKUTIF -> if (body.bodyType == BusBodyType.SDD) 40 else 30
                BusClass.SUPER_EKSEKUTIF -> if (body.bodyType == BusBodyType.SDD) 30 else 22
                BusClass.SUITES_CLASS -> if (body.bodyType == BusBodyType.SDD) 32 else 21
            }
        }

    val tailoredBody: KaroseriBody?
        get() {
            val body = selectedBody ?: return null
            return body.copy(
                busClass = selectedBusClass,
                passengerCapacity = calculatedCapacity,
                price = body.price + classPriceAddition
            )
        }

    val totalInvestment: Money
        get() {
            val chassisPrice = selectedChassis?.price ?: Money.ZERO
            val bodyPrice = tailoredBody?.price ?: Money.ZERO
            return chassisPrice + bodyPrice
        }

    val canAfford: Boolean
        get() {
            val balance = company?.balance ?: return false
            return balance.canAfford(totalInvestment)
        }
}
