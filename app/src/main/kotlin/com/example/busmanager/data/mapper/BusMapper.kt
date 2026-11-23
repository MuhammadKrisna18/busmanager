package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.OwnedBusEntity
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusCondition
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.KaroseriBody

fun OwnedBusEntity.toDomain(chassis: Chassis, body: KaroseriBody): Bus = Bus(
    id = id,
    plateNumber = plateNumber,
    nickname = nickname,
    chassis = chassis,
    body = body,
    condition = BusCondition(
        engineHealthPercent = engineHealthPercent,
        bodyHealthPercent = bodyHealthPercent,
        odometerKm = odometerKm
    ),
    isOperating = isOperating
)

fun Bus.toEntity(): OwnedBusEntity = OwnedBusEntity(
    id = id,
    plateNumber = plateNumber,
    nickname = nickname,
    chassisId = chassis.id,
    bodyId = body.id,
    engineHealthPercent = condition.engineHealthPercent,
    bodyHealthPercent = condition.bodyHealthPercent,
    odometerKm = condition.odometerKm,
    isOperating = isOperating
)
