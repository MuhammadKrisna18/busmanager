package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.DriverEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.driver.model.Driver

fun DriverEntity.toDomain(): Driver = Driver(
    id = id,
    name = name,
    wagePerDay = Money.rupiah(wagePerDayAmount),
    assignedBusId = assignedBusId,
    isHired = isHired,
    isPendingRelease = isPendingRelease
)

fun Driver.toEntity(): DriverEntity = DriverEntity(
    id = id,
    name = name,
    wagePerDayAmount = wagePerDay.amount,
    assignedBusId = assignedBusId,
    isHired = isHired,
    isPendingRelease = isPendingRelease
)
