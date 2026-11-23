package com.example.busmanager.domain.fleet.model

import com.example.busmanager.domain.company.model.Money

data class KaroseriBody(
    val id: String,
    val modelName: String,
    val brand: KaroseriBrand,
    val bodyType: BusBodyType,
    val busClass: BusClass,
    val passengerCapacity: Int,
    val luggageCapacityKg: Int,
    val comfortRating: Int,
    val price: Money,
    val requiresTronton: Boolean = bodyType == BusBodyType.SDD
)
