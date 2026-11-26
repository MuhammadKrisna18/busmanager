package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.ActiveTripEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection

fun ActiveBusTrip.toEntity(): ActiveTripEntity {
    return ActiveTripEntity(
        id = id,
        busId = busId,
        busCode = busCode,
        routeId = route.id,
        direction = direction.name,
        departureYear = departureTime.year,
        departureMonth = departureTime.month,
        departureDay = departureTime.day,
        departureHour = departureTime.hour,
        departureMinute = departureTime.minute,
        estimatedArrivalMinutes = estimatedArrivalMinutes,
        elapsedMinutes = elapsedMinutes,
        progressFraction = progressFraction,
        coordX = currentCoordinate.x,
        coordY = currentCoordinate.y,
        coordLat = currentCoordinate.latitude,
        coordLon = currentCoordinate.longitude,
        passengerCount = passengerCount,
        ticketEarningsAmount = ticketEarnings.amount,
        isCompleted = isCompleted,
        maxCapacity = maxCapacity,
        visitedTransitIndicesCsv = visitedTransitStopIndices.joinToString(","),
        totalBoardedPassengers = totalBoardedPassengers,
        totalAlightedPassengers = totalAlightedPassengers,
        cancelNextSchedule = cancelNextSchedule,
        isDwellingAtTransit = isDwellingAtTransit,
        dwellingTerminalName = dwellingTerminalName,
        headwayMinutes = headwayMinutes,
        transitDwellMinutes = transitDwellMinutes,
        ticketPricePerPassengerAmount = ticketPricePerPassenger?.amount
    )
}

fun ActiveTripEntity.toDomain(routeRepository: RouteRepository): ActiveBusTrip? {
    val route = routeRepository.getRouteById(routeId) ?: return null
    val dir = try {
        RouteDirection.valueOf(direction)
    } catch (_: Exception) {
        RouteDirection.ORIGIN_TO_DESTINATION
    }
    val departureCal = GameCalendar(
        year = departureYear,
        month = departureMonth,
        day = departureDay,
        hour = departureHour,
        minute = departureMinute
    )
    val coord = MapCoordinate(
        x = coordX,
        y = coordY,
        latitude = coordLat,
        longitude = coordLon
    )
    val visitedStops = if (visitedTransitIndicesCsv.isBlank()) {
        emptySet()
    } else {
        visitedTransitIndicesCsv.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    return ActiveBusTrip(
        id = id,
        busId = busId,
        busCode = busCode,
        route = route,
        direction = dir,
        departureTime = departureCal,
        estimatedArrivalMinutes = estimatedArrivalMinutes,
        elapsedMinutes = elapsedMinutes,
        progressFraction = progressFraction,
        currentCoordinate = coord,
        passengerCount = passengerCount,
        ticketEarnings = Money.rupiah(ticketEarningsAmount),
        isCompleted = isCompleted,
        maxCapacity = maxCapacity,
        visitedTransitStopIndices = visitedStops,
        totalBoardedPassengers = totalBoardedPassengers,
        totalAlightedPassengers = totalAlightedPassengers,
        cancelNextSchedule = cancelNextSchedule,
        isDwellingAtTransit = isDwellingAtTransit,
        dwellingTerminalName = dwellingTerminalName,
        headwayMinutes = headwayMinutes,
        transitDwellMinutes = transitDwellMinutes,
        ticketPricePerPassenger = ticketPricePerPassengerAmount?.let { Money.rupiah(it) }
    )
}
