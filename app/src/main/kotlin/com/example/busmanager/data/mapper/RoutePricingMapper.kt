package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.RoutePricingEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.RoutePricing

fun RoutePricingEntity.toDomain(): RoutePricing = RoutePricing(
    id = id,
    routeId = routeId,
    originTerminalId = originTerminalId,
    destinationTerminalId = destinationTerminalId,
    originCity = originCity,
    destinationCity = destinationCity,
    price = Money.rupiah(priceAmount)
)

fun RoutePricing.toEntity(): RoutePricingEntity = RoutePricingEntity(
    id = id,
    routeId = routeId,
    originTerminalId = originTerminalId,
    destinationTerminalId = destinationTerminalId,
    originCity = originCity,
    destinationCity = destinationCity,
    priceAmount = price.amount
)
