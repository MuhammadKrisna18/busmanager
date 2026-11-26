package com.example.busmanager.presentation.simulation

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.usecase.RecordFinancialTransactionUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.ValidateBusOperationUseCase
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.usecase.DispatchBusUseCase
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository

class SimulationDispatchCoordinator(
    private val dispatchBusUseCase: DispatchBusUseCase,
    private val routePricingRepository: RoutePricingRepository?,
    private val fleetRepository: FleetRepository?,
    private val recordFinancialTransactionUseCase: RecordFinancialTransactionUseCase?,
    private val companyRepository: CompanyRepository?,
    private val driverRepository: DriverRepository?,
    private val terminalPassengerRepository: TerminalPassengerRepository?,
    private val validateBusOperationUseCase: ValidateBusOperationUseCase?
) {

    suspend fun handleScheduledDepartures(
        time: GameCalendar,
        schedules: List<RouteSchedule>,
        buses: List<Bus>,
        activeTrips: List<ActiveBusTrip>,
        routes: List<BusRoute>,
        onDispatch: suspend (Bus, BusRoute, RouteDirection, GameCalendar) -> Unit
    ) {
        val isOperatingHours = time.hour in 4..23
        if (!isOperatingHours) return

        val currentTotalMinutes = time.day * 1440L + time.hour * 60L + time.minute
        val currentDayMinutes = time.hour * 60 + time.minute
        val dispatchedBusIds = mutableSetOf<String>()
        val dispatchedRouteDirections = mutableSetOf<Pair<String, RouteDirection>>()

        for (schedule in schedules) {
            val busId = schedule.assignedBusId ?: continue
            if (dispatchedBusIds.contains(busId)) continue

            val bus = buses.find { it.id == busId } ?: continue
            if (bus.isOperating) continue

            val isAlreadyOnTrip = activeTrips.any { it.busId == bus.id }
            if (isAlreadyOnTrip) continue

            val status = validateBusOperationUseCase?.invoke(bus)
            if (status != null && !status.canOperate) continue

            val route = routes.find { it.id == schedule.routeId } ?: continue

            val expectedDepartureTerminal = if (schedule.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                route.originTerminal
            } else {
                route.destinationTerminal
            }

            if (bus.currentTerminalId != null && bus.currentTerminalId != expectedDepartureTerminal.id) {
                continue
            }

            val routeDirKey = Pair(route.id, schedule.direction)
            if (dispatchedRouteDirections.contains(routeDirKey)) {
                continue
            }

            val routeTripsInSameDirection = activeTrips.filter {
                it.route.id == route.id && it.direction == schedule.direction && !it.isCompleted
            }
            if (routeTripsInSameDirection.any { it.elapsedMinutes < 60 }) {
                continue
            }

            val shouldDepart = if (schedule.nextDepartureEpochMinutes > 0L) {
                currentTotalMinutes >= schedule.nextDepartureEpochMinutes
            } else {
                val schedDayMinutes = schedule.departureHour * 60 + schedule.departureMinute
                currentDayMinutes >= schedDayMinutes
            }

            if (shouldDepart) {
                dispatchedBusIds.add(bus.id)
                dispatchedRouteDirections.add(routeDirKey)
                onDispatch(bus, route, schedule.direction, time)
            }
        }
    }

    suspend fun executeDispatchAndRevenue(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar,
        schedules: List<RouteSchedule>,
        drivers: List<Driver>
    ): Bus {
        val schedule = schedules.find {
            it.assignedBusId == bus.id && it.routeId == route.id && it.direction == direction
        } ?: schedules.find {
            it.assignedBusId == bus.id && it.routeId == route.id
        }
        val headway = schedule?.headwayMinutes ?: 60
        val dwell = schedule?.transitDwellMinutes ?: 10

        val depTerminal = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal
        } else {
            route.destinationTerminal
        }

        val updatedBus = bus.copy(isOperating = true, currentTerminalId = depTerminal.id)

        val pricings = routePricingRepository?.getPricingsListForRoute(route.id) ?: emptyList()
        val avgTicketPrice = if (pricings.isNotEmpty()) {
            val avg = pricings.map { it.price.amount }.average().toLong()
            Money.rupiah(avg.coerceAtLeast(30_000L))
        } else {
            Money.rupiah((650L * route.totalDistanceKm).toLong())
        }

        val trip = dispatchBusUseCase(
            bus = bus,
            route = route,
            direction = direction,
            currentTime = currentTime,
            headwayMinutes = headway,
            transitDwellMinutes = dwell,
            ticketPricePerPassenger = avgTicketPrice
        )

        fleetRepository?.updateBus(updatedBus)
        val passengers = trip.passengerCount
        val ticketEarnings = trip.ticketEarnings

        val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
        val timeText = currentTime.formatTime()
        val ticketTransaction = FinancialTransaction(
            id = "tx-ticket-${bus.id}-${System.nanoTime()}",
            dateText = dateText,
            timeText = timeText,
            day = currentTime.day,
            month = currentTime.month,
            year = currentTime.year,
            type = TransactionType.INCOME,
            category = TransactionCategory.TICKET_SALE,
            amount = ticketEarnings,
            busId = bus.id,
            busCode = bus.busCode,
            routeId = route.id,
            routeName = route.name,
            description = "Tiket ${route.name} (${bus.busCode})"
        )

        if (recordFinancialTransactionUseCase != null) {
            recordFinancialTransactionUseCase.invoke(ticketTransaction)
        } else {
            companyRepository?.addBalance(ticketEarnings)
        }

        val driverWageAmount = (ticketEarnings.amount * 0.30).toLong().coerceAtLeast(0L)
        if (driverWageAmount > 0L) {
            val driverWage = Money.rupiah(driverWageAmount)
            val driver = bus.driverId?.let { driverRepository?.getDriverById(it) }
                ?: drivers.find { it.id == bus.driverId }
            val driverName = driver?.name ?: bus.busCode
            val wageTransaction = FinancialTransaction(
                id = "tx-wage-${bus.id}-${System.nanoTime()}",
                dateText = dateText,
                timeText = timeText,
                day = currentTime.day,
                month = currentTime.month,
                year = currentTime.year,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.DRIVER_WAGE,
                amount = driverWage,
                busId = bus.id,
                busCode = bus.busCode,
                routeId = route.id,
                routeName = route.name,
                description = "Bagi hasil 30% sopir: $driverName"
            )

            if (recordFinancialTransactionUseCase != null) {
                recordFinancialTransactionUseCase.invoke(wageTransaction)
            } else {
                companyRepository?.deductBalance(driverWage)
            }
        }

        val originTerminal = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal
        } else {
            route.destinationTerminal
        }
        terminalPassengerRepository?.recordPassengerExchange(
            TerminalPassengerRecord(
                id = "rec-term-${bus.id}-${System.nanoTime()}",
                terminalId = originTerminal.id,
                terminalName = originTerminal.name,
                busId = bus.id,
                busCode = bus.busCode,
                routeId = route.id,
                routeName = route.name,
                day = currentTime.day,
                month = currentTime.month,
                year = currentTime.year,
                dateFormatted = dateText,
                timeFormatted = timeText,
                boardedCount = passengers,
                alightedCount = 0
            )
        )

        return updatedBus
    }
}
