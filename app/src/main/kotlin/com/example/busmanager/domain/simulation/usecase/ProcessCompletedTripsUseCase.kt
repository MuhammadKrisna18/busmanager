package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository

class ProcessCompletedTripsUseCase(
    private val fleetRepository: FleetRepository,
    private val simulationTripRepository: SimulationTripRepository,
    private val companyRepository: CompanyRepository? = null,
    private val financialTransactionRepository: FinancialTransactionRepository? = null,
    private val terminalPassengerRepository: TerminalPassengerRepository? = null,
    private val routeScheduleRepository: RouteScheduleRepository? = null,
    private val driverRepository: DriverRepository? = null,
    private val recordSimulationLogUseCase: RecordSimulationLogUseCase? = null
) {
    suspend operator fun invoke(
        completedTrips: List<ActiveBusTrip>,
        currentTime: GameCalendar
    ) {
        val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
        val timeText = currentTime.formatTime()

        for (trip in completedTrips) {
            val destTerminal = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                trip.route.destinationTerminal
            } else {
                trip.route.originTerminal
            }

            terminalPassengerRepository?.recordPassengerExchange(
                TerminalPassengerRecord(
                    id = "rec-term-arr-${trip.busId}-${System.nanoTime()}",
                    terminalId = destTerminal.id,
                    terminalName = destTerminal.name,
                    busId = trip.busId,
                    busCode = trip.busCode,
                    routeId = trip.route.id,
                    routeName = trip.route.name,
                    day = currentTime.day,
                    month = currentTime.month,
                    year = currentTime.year,
                    dateFormatted = dateText,
                    timeFormatted = timeText,
                    boardedCount = 0,
                    alightedCount = trip.passengerCount
                )
            )

            val bus = fleetRepository.getBusById(trip.busId)
            if (bus != null) {
                val updatedBus = bus.copy(
                    isOperating = false,
                    currentTerminalId = destTerminal.id
                )
                fleetRepository.updateBus(updatedBus)
            }

            if (trip.ticketEarnings.amount > 0L) {
                val ticketTx = FinancialTransaction(
                    id = "tx-ticket-${trip.busId}-${System.nanoTime()}",
                    dateText = dateText,
                    timeText = timeText,
                    day = currentTime.day,
                    month = currentTime.month,
                    year = currentTime.year,
                    type = TransactionType.INCOME,
                    category = TransactionCategory.TICKET_SALE,
                    amount = trip.ticketEarnings,
                    busId = trip.busId,
                    busCode = trip.busCode,
                    routeId = trip.route.id,
                    routeName = trip.route.name,
                    description = "Pendapatan tiket rute ${trip.route.name} (${trip.passengerCount} pnp)"
                )
                financialTransactionRepository?.recordTransaction(ticketTx)
                companyRepository?.addBalance(trip.ticketEarnings)

                val wageAmount = (trip.ticketEarnings.amount * 0.30).toLong()
                if (wageAmount > 0L) {
                    val wageTx = FinancialTransaction(
                        id = "tx-wage-${trip.busId}-${System.nanoTime()}",
                        dateText = dateText,
                        timeText = timeText,
                        day = currentTime.day,
                        month = currentTime.month,
                        year = currentTime.year,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.DRIVER_WAGE,
                        amount = Money.rupiah(wageAmount),
                        busId = trip.busId,
                        busCode = trip.busCode,
                        routeId = trip.route.id,
                        routeName = trip.route.name,
                        description = "Bagi hasil komisi sopir rute ${trip.route.name}"
                    )
                    financialTransactionRepository?.recordTransaction(wageTx)
                    companyRepository?.deductBalance(Money.rupiah(wageAmount))
                }
            }

            val busSchedules = routeScheduleRepository?.getSchedulesForBus(trip.busId)
                ?.filter { it.routeId == trip.route.id } ?: emptyList()
            if (busSchedules.isNotEmpty() && !trip.cancelNextSchedule) {
                val returnDir = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                    RouteDirection.DESTINATION_TO_ORIGIN
                } else {
                    RouteDirection.ORIGIN_TO_DESTINATION
                }
                val returnSchedule = busSchedules.find { it.direction == returnDir }
                val currentTotalMinutes = currentTime.day * 1440L + currentTime.hour * 60L + currentTime.minute
                val effectiveHeadway = trip.headwayMinutes.coerceAtLeast(30)
                val rawNextDeparture = currentTotalMinutes + effectiveHeadway
                val resolvedMinutes = calculateNextOperatingDeparture(rawNextDeparture)
                val resolvedHour = (((resolvedMinutes) % 1440L) / 60L).toInt()
                val resolvedMin = ((resolvedMinutes) % 60L).toInt()

                if (returnSchedule != null) {
                    val updatedReturnSchedule = returnSchedule.copy(
                        departureHour = resolvedHour,
                        departureMinute = resolvedMin,
                        direction = returnDir,
                        headwayMinutes = effectiveHeadway,
                        nextDepartureEpochMinutes = resolvedMinutes
                    )
                    routeScheduleRepository?.updateSchedule(updatedReturnSchedule)
                }
            }

            bus?.driverId?.let { driverId ->
                val driver = driverRepository?.getDriverById(driverId)
                if (driver?.isPendingRelease == true) {
                    driverRepository.unassignDriver(driverId)
                }
            }

            recordSimulationLogUseCase?.recordArrival(
                busCode = trip.busCode,
                routeName = trip.route.name,
                destTerminalName = destTerminal.name,
                passengerCount = trip.passengerCount,
                earningsAmount = trip.ticketEarnings.amount,
                timeText = timeText,
                dateText = dateText,
                busId = trip.busId,
                routeId = trip.route.id
            )

            simulationTripRepository.completeTrip(trip.id)
        }
    }

    private fun calculateNextOperatingDeparture(baseEpochMinutes: Long): Long {
        val minuteOfDay = (baseEpochMinutes % 1440L).toInt()
        return when {
            minuteOfDay < 4 * 60 -> {
                val dayEpoch = (baseEpochMinutes / 1440L) * 1440L
                dayEpoch + 4 * 60L
            }
            minuteOfDay > 23 * 60 + 59 -> {
                val nextDayEpoch = (baseEpochMinutes / 1440L + 1L) * 1440L
                nextDayEpoch + 4 * 60L
            }
            else -> baseEpochMinutes
        }
    }
}
