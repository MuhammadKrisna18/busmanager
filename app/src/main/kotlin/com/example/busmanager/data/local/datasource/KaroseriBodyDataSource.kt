package com.example.busmanager.data.local.datasource

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand

class KaroseriBodyDataSource {

    fun getKaroseriBodyList(): List<KaroseriBody> = listOf(
        KaroseriBody(
            id = "body-ap-jb3-hdd",
            modelName = "Jetbus 3+ HDD",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.HDD,
            busClass = BusClass.BISNIS,
            passengerCapacity = 40,
            luggageCapacityKg = 1800,
            comfortRating = 78,
            price = Money.rupiah(520_000_000)
        ),
        KaroseriBody(
            id = "body-ap-jb3-shd",
            modelName = "Jetbus 3+ SHD",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 32,
            luggageCapacityKg = 2400,
            comfortRating = 85,
            price = Money.rupiah(600_000_000)
        ),
        KaroseriBody(
            id = "body-ap-jb5-shd",
            modelName = "Jetbus 5 SHD",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 30,
            luggageCapacityKg = 2600,
            comfortRating = 90,
            price = Money.rupiah(680_000_000)
        ),
        KaroseriBody(
            id = "body-ap-jb5-uhd",
            modelName = "Jetbus 5 UHD",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.UHD,
            busClass = BusClass.SUPER_EKSEKUTIF,
            passengerCapacity = 32,
            luggageCapacityKg = 4000,
            comfortRating = 94,
            price = Money.rupiah(850_000_000),
            requiresTronton = true
        ),
        KaroseriBody(
            id = "body-ap-jb5-sdd",
            modelName = "Jetbus 5 SDD Double Decker",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.SDD,
            busClass = BusClass.SUITES_CLASS,
            passengerCapacity = 24,
            luggageCapacityKg = 3200,
            comfortRating = 96,
            price = Money.rupiah(1_150_000_000),
            requiresTronton = true
        ),
        KaroseriBody(
            id = "body-ap-jb5-dreamcoach",
            modelName = "Jetbus 5 Dream Coach",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.SLEEPER_SUITES,
            busClass = BusClass.SUITES_CLASS,
            passengerCapacity = 22,
            luggageCapacityKg = 2200,
            comfortRating = 98,
            price = Money.rupiah(880_000_000)
        ),
        KaroseriBody(
            id = "body-lk-sr2-hd",
            modelName = "Legacy SR2 HD Prime",
            brand = KaroseriBrand.LAKSANA,
            bodyType = BusBodyType.HDD,
            busClass = BusClass.BISNIS,
            passengerCapacity = 40,
            luggageCapacityKg = 1800,
            comfortRating = 80,
            price = Money.rupiah(510_000_000)
        ),
        KaroseriBody(
            id = "body-lk-sr3-xhd",
            modelName = "Legacy SR3 XHD Prime",
            brand = KaroseriBrand.LAKSANA,
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 32,
            luggageCapacityKg = 2600,
            comfortRating = 89,
            price = Money.rupiah(650_000_000)
        ),
        KaroseriBody(
            id = "body-lk-sr3-suites",
            modelName = "Legacy SR3 Suites Class",
            brand = KaroseriBrand.LAKSANA,
            bodyType = BusBodyType.SLEEPER_SUITES,
            busClass = BusClass.SUITES_CLASS,
            passengerCapacity = 21,
            luggageCapacityKg = 2000,
            comfortRating = 97,
            price = Money.rupiah(850_000_000)
        ),
        KaroseriBody(
            id = "body-lk-sr3-dd",
            modelName = "Legacy SR3 Double Decker",
            brand = KaroseriBrand.LAKSANA,
            bodyType = BusBodyType.SDD,
            busClass = BusClass.SUITES_CLASS,
            passengerCapacity = 24,
            luggageCapacityKg = 3000,
            comfortRating = 96,
            price = Money.rupiah(1_100_000_000),
            requiresTronton = true
        ),
        KaroseriBody(
            id = "body-tt-avante-h7",
            modelName = "Avante H7",
            brand = KaroseriBrand.TENTREM,
            bodyType = BusBodyType.HDD,
            busClass = BusClass.BISNIS,
            passengerCapacity = 38,
            luggageCapacityKg = 1900,
            comfortRating = 81,
            price = Money.rupiah(520_000_000)
        ),
        KaroseriBody(
            id = "body-tt-avante-h8",
            modelName = "Avante H8",
            brand = KaroseriBrand.TENTREM,
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 32,
            luggageCapacityKg = 2800,
            comfortRating = 88,
            price = Money.rupiah(630_000_000)
        ),
        KaroseriBody(
            id = "body-tt-avante-d2",
            modelName = "Avante D2 Double Decker",
            brand = KaroseriBrand.TENTREM,
            bodyType = BusBodyType.SDD,
            busClass = BusClass.SUITES_CLASS,
            passengerCapacity = 24,
            luggageCapacityKg = 3200,
            comfortRating = 95,
            price = Money.rupiah(1_120_000_000),
            requiresTronton = true
        ),
        KaroseriBody(
            id = "body-tt-avante-grandcaptain",
            modelName = "Avante Grand Captain",
            brand = KaroseriBrand.TENTREM,
            bodyType = BusBodyType.SLEEPER_SUITES,
            busClass = BusClass.SUITES_CLASS,
            passengerCapacity = 24,
            luggageCapacityKg = 2400,
            comfortRating = 96,
            price = Money.rupiah(860_000_000)
        )
    )
}
