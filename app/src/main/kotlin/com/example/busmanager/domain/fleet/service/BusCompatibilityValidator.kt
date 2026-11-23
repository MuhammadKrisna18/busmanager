package com.example.busmanager.domain.fleet.service

import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.EngineLocation
import com.example.busmanager.domain.fleet.model.KaroseriBody

class BusCompatibilityValidator {

    fun isCompatible(chassis: Chassis, body: KaroseriBody): Boolean {
        if (body.bodyType == BusBodyType.SDD && chassis.axleType != AxleType.TRONTON_6X2) {
            return false
        }
        if (body.bodyType == BusBodyType.UHD && chassis.axleType != AxleType.TRONTON_6X2) {
            return false
        }
        if (chassis.engineLocation == EngineLocation.FRONT && body.bodyType != BusBodyType.HDD) {
            return false
        }
        return true
    }
}
