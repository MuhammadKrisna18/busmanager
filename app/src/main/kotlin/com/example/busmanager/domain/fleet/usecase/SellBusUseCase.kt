package com.example.busmanager.domain.fleet.usecase

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository

sealed class SellBusResult {
    data class Success(val soldBus: Bus, val refundAmount: Money) : SellBusResult()
    object BusOperatingError : SellBusResult()
    object BusNotFound : SellBusResult()
}

class SellBusUseCase(
    private val fleetRepository: FleetRepository,
    private val companyRepository: CompanyRepository,
    private val driverRepository: DriverRepository,
    private val routeScheduleRepository: RouteScheduleRepository,
    private val financialTransactionRepository: FinancialTransactionRepository? = null,
    private val simulationTripRepository: SimulationTripRepository? = null,
    private val simulationTimeRepository: SimulationTimeRepository? = null
) {
    suspend operator fun invoke(busId: String): SellBusResult {
        val bus = fleetRepository.getBusById(busId) ?: return SellBusResult.BusNotFound

        val isOperating = bus.isOperating || (simulationTripRepository?.activeTrips?.value?.any { it.busId == bus.id } == true)
        if (isOperating) {
            return SellBusResult.BusOperatingError
        }

        val refundAmount = Money.rupiah((bus.totalPrice.amount * 0.70).toLong().coerceAtLeast(10_000_000L))

        if (bus.driverId != null) {
            driverRepository.unassignDriver(bus.driverId)
        }

        routeScheduleRepository.deleteSchedulesForBus(bus.id)
        if (bus.routeId != null) {
            val schedules = routeScheduleRepository.getSchedulesListForRoute(bus.routeId)
            schedules.filter { it.assignedBusId == bus.id }.forEach { schedule ->
                routeScheduleRepository.deleteSchedule(schedule.id)
            }
        }

        companyRepository.addBalance(refundAmount)

        val time = simulationTimeRepository?.currentTime?.value ?: GameCalendar.initial()
        val dateText = "${time.dayOfWeekName()}, ${time.formatDate()}"
        val timeText = time.formatTime()

        financialTransactionRepository?.recordTransaction(
            FinancialTransaction(
                id = "tx-sell-${bus.id}-${System.nanoTime()}",
                dateText = dateText,
                timeText = timeText,
                day = time.day,
                month = time.month,
                year = time.year,
                type = TransactionType.INCOME,
                category = TransactionCategory.BUS_SALE,
                amount = refundAmount,
                busId = bus.id,
                busCode = bus.busCode,
                description = "Penjualan armada ${bus.busCode} (${bus.fullName})"
            )
        )

        fleetRepository.removeBus(bus.id)

        return SellBusResult.Success(bus, refundAmount)
    }
}
