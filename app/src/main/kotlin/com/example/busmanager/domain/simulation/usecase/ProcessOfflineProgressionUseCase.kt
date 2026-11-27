package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.OfflineBusSummary
import com.example.busmanager.domain.simulation.model.OfflineProgressionResult
import com.example.busmanager.domain.simulation.model.SavedSimulationState
import com.example.busmanager.domain.simulation.repository.SimulationStateRepository
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import kotlinx.coroutines.flow.first

class ProcessOfflineProgressionUseCase(
    private val simulationStateRepository: SimulationStateRepository,
    private val simulationTimeRepository: SimulationTimeRepository,
    private val companyRepository: CompanyRepository,
    private val fleetRepository: FleetRepository,
    private val driverRepository: DriverRepository,
    private val routeRepository: RouteRepository,
    private val routeScheduleRepository: RouteScheduleRepository,
    private val routePricingRepository: RoutePricingRepository,
    private val financialTransactionRepository: FinancialTransactionRepository? = null,
    private val terminalPassengerRepository: TerminalPassengerRepository? = null
) {

    suspend operator fun invoke(): OfflineProgressionResult? {
        val state = simulationStateRepository.loadState() ?: return null
        if (!state.isPlaying) return null

        val nowMillis = System.currentTimeMillis()
        val deltaMillis = (nowMillis - state.lastWallClockMillis).coerceAtLeast(0L)
        val gameMinutesElapsed = ((deltaMillis / 1000.0) * state.speed.multiplier).toLong().toInt()
        if (gameMinutesElapsed < 2) return null

        val cappedMinutes = gameMinutesElapsed.coerceAtMost(2880)
        val gameHours = (cappedMinutes / 60).coerceAtLeast(1)

        val updatedCalendar = state.gameCalendar.advanceMinutes(cappedMinutes)
        simulationTimeRepository.setTime(updatedCalendar)
        simulationTimeRepository.setSpeed(state.speed)
        simulationTimeRepository.play()

        val buses = fleetRepository.getOwnedBuses().first()
        val drivers = driverRepository.getAllDrivers().first()
        val busSummaries = mutableListOf<OfflineBusSummary>()

        var totalTrips = 0
        var totalPassengers = 0
        var totalGrossRevenueAmount = 0L
        var totalWagesAmount = 0L

        for (bus in buses) {
            val routeId = bus.routeId ?: continue
            val driverId = bus.driverId ?: continue
            val route = routeRepository.getRouteById(routeId) ?: continue
            val driver = drivers.find { it.id == driverId } ?: continue

            val category = RouteCategory.fromRoute(route)
            val maxTripsPerDay = if (category == RouteCategory.AKAP) 2 else 4
            val maxAllowedTrips = ((cappedMinutes.toDouble() / 1440.0) * maxTripsPerDay).toInt().coerceAtLeast(1)
            val tripDuration = route.totalEstimatedDurationMinutes.coerceAtLeast(60)
            val tripsCompleted = minOf((cappedMinutes / tripDuration).coerceAtLeast(0), maxAllowedTrips)
            if (tripsCompleted <= 0) continue

            val passengersPerTrip = (bus.body.passengerCapacity * 0.85f).toInt().coerceAtLeast(10)
            val busPassengers = passengersPerTrip * tripsCompleted

            val pricings = routePricingRepository.getPricingsListForRoute(route.id)
            val avgPrice = if (pricings.isNotEmpty()) {
                pricings.map { it.price.amount }.average().toLong().coerceAtLeast(50_000L)
            } else {
                75_000L
            }

            val grossAmount = avgPrice * busPassengers
            val wage = driver.calculateTripCommission(grossAmount)
            val netAmount = grossAmount - wage.amount

            val destTerminal = if (tripsCompleted % 2 != 0) {
                route.destinationTerminal
            } else {
                route.originTerminal
            }

            fleetRepository.updateBus(
                bus.copy(
                    isOperating = false,
                    currentTerminalId = destTerminal.id
                )
            )

            val dateText = "${updatedCalendar.dayOfWeekName()}, ${updatedCalendar.formatDate()}"
            val timeText = updatedCalendar.formatTime()

            val ticketTx = FinancialTransaction(
                id = "tx-off-ticket-${bus.id}-${System.nanoTime()}",
                dateText = dateText,
                timeText = timeText,
                day = updatedCalendar.day,
                month = updatedCalendar.month,
                year = updatedCalendar.year,
                type = TransactionType.INCOME,
                category = TransactionCategory.TICKET_SALE,
                amount = Money.rupiah(grossAmount),
                busId = bus.id,
                busCode = bus.busCode,
                routeId = route.id,
                routeName = route.name,
                description = "Pendapatan offline tiket: ${route.name} (${bus.busCode})"
            )
            financialTransactionRepository?.recordTransaction(ticketTx)

            if (wage.amount > 0L) {
                val wageTx = FinancialTransaction(
                    id = "tx-off-wage-${bus.id}-${System.nanoTime()}",
                    dateText = dateText,
                    timeText = timeText,
                    day = updatedCalendar.day,
                    month = updatedCalendar.month,
                    year = updatedCalendar.year,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.DRIVER_WAGE,
                    amount = wage,
                    busId = bus.id,
                    busCode = bus.busCode,
                    routeId = route.id,
                    routeName = route.name,
                    description = "Bagi hasil 30% offline sopir: ${driver.name}"
                )
                financialTransactionRepository?.recordTransaction(wageTx)
            }

            terminalPassengerRepository?.recordPassengerExchange(
                TerminalPassengerRecord(
                    id = "rec-off-${bus.id}-${System.nanoTime()}",
                    terminalId = destTerminal.id,
                    terminalName = destTerminal.name,
                    busId = bus.id,
                    busCode = bus.busCode,
                    routeId = route.id,
                    routeName = route.name,
                    day = updatedCalendar.day,
                    month = updatedCalendar.month,
                    year = updatedCalendar.year,
                    dateFormatted = dateText,
                    timeFormatted = timeText,
                    boardedCount = busPassengers / 2,
                    alightedCount = busPassengers / 2
                )
            )

            for (transitTerm in route.transitTerminals) {
                val transitCount = (busPassengers / (route.transitTerminals.size + 2)).coerceAtLeast(1)
                terminalPassengerRepository?.recordPassengerExchange(
                    TerminalPassengerRecord(
                        id = "rec-off-trans-${bus.id}-${transitTerm.id}-${System.nanoTime()}",
                        terminalId = transitTerm.id,
                        terminalName = transitTerm.name,
                        busId = bus.id,
                        busCode = bus.busCode,
                        routeId = route.id,
                        routeName = route.name,
                        day = updatedCalendar.day,
                        month = updatedCalendar.month,
                        year = updatedCalendar.year,
                        dateFormatted = dateText,
                        timeFormatted = timeText,
                        boardedCount = transitCount,
                        alightedCount = transitCount
                    )
                )
            }

            totalTrips += tripsCompleted
            totalPassengers += busPassengers
            totalGrossRevenueAmount += grossAmount
            totalWagesAmount += wage.amount

            busSummaries.add(
                OfflineBusSummary(
                    busCode = bus.busCode,
                    routeName = route.name,
                    tripsCompleted = tripsCompleted,
                    passengersCarried = busPassengers,
                    grossRevenue = Money.rupiah(grossAmount),
                    driverWage = wage,
                    netEarnings = Money.rupiah(netAmount),
                    currentTerminalName = destTerminal.name
                )
            )
        }

        val totalNetEarningsAmount = totalGrossRevenueAmount - totalWagesAmount
        if (totalNetEarningsAmount > 0L) {
            companyRepository.addBalance(Money.rupiah(totalNetEarningsAmount))
        }

        simulationStateRepository.saveState(
            SavedSimulationState(
                lastWallClockMillis = nowMillis,
                isPlaying = true,
                speed = state.speed,
                gameCalendar = updatedCalendar
            )
        )

        if (busSummaries.isEmpty()) return null

        return OfflineProgressionResult(
            offlineDurationMinutes = cappedMinutes,
            offlineGameHours = gameHours,
            totalTripsCompleted = totalTrips,
            totalPassengersCarried = totalPassengers,
            totalRevenue = Money.rupiah(totalGrossRevenueAmount),
            totalDriverWages = Money.rupiah(totalWagesAmount),
            netEarnings = Money.rupiah(totalNetEarningsAmount),
            activeBusesCount = busSummaries.size,
            busSummaries = busSummaries
        )
    }
}
