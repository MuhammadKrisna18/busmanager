package com.example.busmanager.domain.fleet.model

import com.example.busmanager.domain.company.model.Money

data class Bus(
    val id: String,
    val plateNumber: String,
    val nickname: String,
    val chassis: Chassis,
    val body: KaroseriBody,
    val condition: BusCondition = BusCondition(),
    val isOperating: Boolean = false
) {
    init {
        if (body.requiresTronton) {
            require(chassis.axleType == AxleType.TRONTON_6X2) {
                "Bodi ${body.modelName} memerlukan sasis Tronton (6x2)"
            }
        }
    }

    val totalPrice: Money
        get() = chassis.price + body.price

    val fullName: String
        get() = "${chassis.brand.displayName} ${chassis.modelName} - ${body.brand.displayName} ${body.modelName}"
}
