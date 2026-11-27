package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.model.Driver
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

                val wage = Driver.calculateTripCommission(trip.ticketEarnings)
                if (wage.amount > 0L) {
                    val wageTx = FinancialTransaction(
                        id = "tx-wage-${trip.busId}-${System.nanoTime()}",
                        dateText = dateText,
                        timeText = timeText,
                        day = currentTime.day,
                        month = currentTime.month,
                        year = currentTime.year,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.DRIVER_WAGE,
                        amount = wage,
                        busId = trip.busId,
                        busCode = trip.busCode,
                        routeId = trip.route.id,
                        routeName = trip.route.name,
                        description = "Bagi hasil komisi sopir rute ${trip.route.name}"
                    )
                    financialTransactionRepository?.recordTransaction(wageTx)
                    companyRepository?.deductBalance(wage)
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
                val upcomingReturnSchedules = busSchedules
                    .filter { it.direction == returnDir }
                    .sortedBy { it.departureHour * 60 + it.departureMinute }
                val currentTotalMinutes = currentTime.day * 1440L + currentTime.hour * 60L + currentTime.minute
                val effectiveHeadway = trip.headwayMinutes.coerceAtLeast(30)
                val rawNextDeparture = currentTotalMinutes + effectiveHeadway

                val returnSchedule = upcomingReturnSchedules.firstOrNull { sched ->
                    val schedEpochMinutes = currentTime.day * 1440L + sched.departureHour * 60L + sched.departureMinute
                    schedEpochMinutes >= rawNextDeparture
                } ?: upcomingReturnSchedules.firstOrNull()

                if (returnSchedule != null) {
                    val scheduledEpochMinutes = currentTime.day * 1440L + returnSchedule.departureHour * 60L + returnSchedule.departureMinute
                    val targetEpochMinutes = if (scheduledEpochMinutes >= rawNextDeparture) {
                        scheduledEpochMinutes
                    } else {
                        (currentTime.day + 1L) * 1440L + returnSchedule.departureHour * 60L + returnSchedule.departureMinute
                    }
                    val updatedReturnSchedule = returnSchedule.copy(
                        direction = returnDir,
                        headwayMinutes = effectiveHeadway,
                        nextDepartureEpochMinutes = targetEpochMinutes
                    )
                    routeScheduleRepository?.updateSchedule(updatedReturnSchedule)
                } else {
                    val activeSchedule = busSchedules.first()
                    val targetEpochMinutes = calculateNextOperatingDeparture(rawNextDeparture)
                    val resolvedHour = (((targetEpochMinutes) % 1440L) / 60L).toInt()
                    val resolvedMin = ((targetEpochMinutes) % 60L).toInt()
                    val updatedSchedule = activeSchedule.copy(
                        departureHour = resolvedHour,
                        departureMinute = resolvedMin,
                        direction = returnDir,
                        headwayMinutes = effectiveHeadway,
                        nextDepartureEpochMinutes = targetEpochMinutes
                    )
                    routeScheduleRepository?.updateSchedule(updatedSchedule)
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
