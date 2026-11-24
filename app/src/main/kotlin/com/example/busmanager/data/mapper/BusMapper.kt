package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.OwnedBusEntity
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.BusCondition
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.KaroseriBody

fun OwnedBusEntity.toDomain(chassis: Chassis, body: KaroseriBody): Bus {
    val effectiveClass = busClass?.let { name ->
        runCatching { BusClass.valueOf(name) }.getOrNull()
    } ?: body.busClass
    val effectiveCapacity = passengerCapacity ?: body.passengerCapacity
    val effectiveBody = if (effectiveClass != body.busClass || effectiveCapacity != body.passengerCapacity) {
        body.copy(busClass = effectiveClass, passengerCapacity = effectiveCapacity)
    } else {
        body
    }

    return Bus(
        id = id,
        busCode = busCode,
        chassis = chassis,
        body = effectiveBody,
        condition = BusCondition(
            engineHealthPercent = engineHealthPercent,
            bodyHealthPercent = bodyHealthPercent,
            odometerKm = odometerKm
        ),
        isOperating = isOperating,
        routeId = routeId,
        driverId = driverId,
        currentTerminalId = currentTerminalId
    )
}

fun Bus.toEntity(): OwnedBusEntity = OwnedBusEntity(
    id = id,
    busCode = busCode,
    chassisId = chassis.id,
    bodyId = body.id,
    busClass = body.busClass.name,
    passengerCapacity = body.passengerCapacity,
    currentTerminalId = currentTerminalId,
    engineHealthPercent = condition.engineHealthPercent,
    bodyHealthPercent = condition.bodyHealthPercent,
    odometerKm = condition.odometerKm,
    isOperating = isOperating,
    routeId = routeId,
    driverId = driverId
)
