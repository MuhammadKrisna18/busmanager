package com.example.busmanager.data.local.datasource

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.RoadCharacteristic
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment

class RouteDataSource(
    private val terminalDataSource: TerminalDataSource = TerminalDataSource()
) {

    private val purabaya = terminalDataSource.getTerminalById("term_purabaya")!!
    private val arjosari = terminalDataSource.getTerminalById("term_arjosari")!!
    private val bayuangga = terminalDataSource.getTerminalById("term_bayuangga")!!
    private val tawangAlun = terminalDataSource.getTerminalById("term_tawangalun")!!
    private val situbondo = terminalDataSource.getTerminalById("term_situbondo")!!
    private val sritanjung = terminalDataSource.getTerminalById("term_sritanjung")!!
    private val kertajaya = terminalDataSource.getTerminalById("term_kertajaya")!!
    private val tamanan = terminalDataSource.getTerminalById("term_tamanan")!!
    private val gayatri = terminalDataSource.getTerminalById("term_gayatri")!!
    private val patria = terminalDataSource.getTerminalById("term_patria")!!

    val segSbyMlgToll = RouteSegment(
        id = "seg_sby_mlg_toll",
        fromTerminalId = purabaya.id,
        toTerminalId = arjosari.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 90.0,
        baseDurationMinutes = 75,
        tollFee = Money.rupiah(60000),
        waypoints = listOf(
            purabaya.coordinate,
            MapCoordinate(0.19f, 0.28f, -7.50, 112.71),
            MapCoordinate(0.20f, 0.35f, -7.60, 112.70),
            MapCoordinate(0.18f, 0.44f, -7.78, 112.69),
            arjosari.coordinate
        )
    )

    val segSbyMlgNonToll = RouteSegment(
        id = "seg_sby_mlg_nontoll",
        fromTerminalId = purabaya.id,
        toTerminalId = arjosari.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 95.0,
        baseDurationMinutes = 160,
        tollFee = Money.ZERO,
        waypoints = listOf(
            purabaya.coordinate,
            MapCoordinate(0.20f, 0.23f, -7.45, 112.72),
            MapCoordinate(0.21f, 0.32f, -7.56, 112.70),
            MapCoordinate(0.18f, 0.42f, -7.75, 112.71),
            MapCoordinate(0.16f, 0.48f, -7.88, 112.67),
            arjosari.coordinate
        )
    )

    val segMlgSbyToll = RouteSegment(
        id = "seg_mlg_sby_toll",
        fromTerminalId = arjosari.id,
        toTerminalId = purabaya.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 90.0,
        baseDurationMinutes = 75,
        tollFee = Money.rupiah(60000),
        waypoints = segSbyMlgToll.waypoints.reversed()
    )

    val segMlgSbyNonToll = RouteSegment(
        id = "seg_mlg_sby_nontoll",
        fromTerminalId = arjosari.id,
        toTerminalId = purabaya.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 95.0,
        baseDurationMinutes = 160,
        tollFee = Money.ZERO,
        waypoints = segSbyMlgNonToll.waypoints.reversed()
    )

    val segSbyProboToll = RouteSegment(
        id = "seg_sby_probo_toll",
        fromTerminalId = purabaya.id,
        toTerminalId = bayuangga.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 100.0,
        baseDurationMinutes = 75,
        tollFee = Money.rupiah(65000),
        waypoints = listOf(
            purabaya.coordinate,
            MapCoordinate(0.20f, 0.32f, -7.57, 112.71),
            MapCoordinate(0.28f, 0.35f, -7.64, 112.90),
            MapCoordinate(0.36f, 0.38f, -7.71, 113.08),
            bayuangga.coordinate
        )
    )

    val segSbyProboNonToll = RouteSegment(
        id = "seg_sby_probo_nontoll",
        fromTerminalId = purabaya.id,
        toTerminalId = bayuangga.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.COASTAL,
        distanceKm = 105.0,
        baseDurationMinutes = 150,
        tollFee = Money.ZERO,
        waypoints = listOf(
            purabaya.coordinate,
            MapCoordinate(0.23f, 0.28f, -7.52, 112.77),
            MapCoordinate(0.29f, 0.34f, -7.63, 112.91),
            MapCoordinate(0.35f, 0.37f, -7.68, 113.03),
            MapCoordinate(0.40f, 0.40f, -7.73, 113.13),
            bayuangga.coordinate
        )
    )

    val segProboSbyToll = RouteSegment(
        id = "seg_probo_sby_toll",
        fromTerminalId = bayuangga.id,
        toTerminalId = purabaya.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 100.0,
        baseDurationMinutes = 75,
        tollFee = Money.rupiah(65000),
        waypoints = segSbyProboToll.waypoints.reversed()
    )

    val segProboSbyNonToll = RouteSegment(
        id = "seg_probo_sby_nontoll",
        fromTerminalId = bayuangga.id,
        toTerminalId = purabaya.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.COASTAL,
        distanceKm = 105.0,
        baseDurationMinutes = 150,
        tollFee = Money.ZERO,
        waypoints = segSbyProboNonToll.waypoints.reversed()
    )

    val segProboStbToll = RouteSegment(
        id = "seg_probo_stb_toll",
        fromTerminalId = bayuangga.id,
        toTerminalId = situbondo.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.COASTAL,
        distanceKm = 95.0,
        baseDurationMinutes = 70,
        tollFee = Money.rupiah(55000),
        waypoints = listOf(
            bayuangga.coordinate,
            MapCoordinate(0.53f, 0.40f, -7.74, 113.40),
            MapCoordinate(0.62f, 0.39f, -7.71, 113.63),
            MapCoordinate(0.70f, 0.38f, -7.72, 113.82),
            situbondo.coordinate
        )
    )

    val segProboStbNonToll = RouteSegment(
        id = "seg_probo_stb_nontoll",
        fromTerminalId = bayuangga.id,
        toTerminalId = situbondo.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.COASTAL,
        distanceKm = 100.0,
        baseDurationMinutes = 130,
        tollFee = Money.ZERO,
        waypoints = listOf(
            bayuangga.coordinate,
            MapCoordinate(0.48f, 0.41f, -7.75, 113.28),
            MapCoordinate(0.55f, 0.40f, -7.73, 113.45),
            MapCoordinate(0.64f, 0.39f, -7.70, 113.68),
            MapCoordinate(0.72f, 0.38f, -7.71, 113.88),
            situbondo.coordinate
        )
    )

    val segStbProboToll = RouteSegment(
        id = "seg_stb_probo_toll",
        fromTerminalId = situbondo.id,
        toTerminalId = bayuangga.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.COASTAL,
        distanceKm = 95.0,
        baseDurationMinutes = 70,
        tollFee = Money.rupiah(55000),
        waypoints = segProboStbToll.waypoints.reversed()
    )

    val segStbProboNonToll = RouteSegment(
        id = "seg_stb_probo_nontoll",
        fromTerminalId = situbondo.id,
        toTerminalId = bayuangga.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.COASTAL,
        distanceKm = 100.0,
        baseDurationMinutes = 130,
        tollFee = Money.ZERO,
        waypoints = segProboStbNonToll.waypoints.reversed()
    )

    val segStbBwiNonToll = RouteSegment(
        id = "seg_stb_bwi_nontoll",
        fromTerminalId = situbondo.id,
        toTerminalId = sritanjung.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 95.0,
        baseDurationMinutes = 135,
        tollFee = Money.ZERO,
        waypoints = listOf(
            situbondo.coordinate,
            MapCoordinate(0.83f, 0.40f, -7.74, 114.15),
            MapCoordinate(0.88f, 0.46f, -7.86, 114.33),
            MapCoordinate(0.90f, 0.54f, -7.98, 114.38),
            sritanjung.coordinate
        )
    )

    val segBwiStbNonToll = RouteSegment(
        id = "seg_bwi_stb_nontoll",
        fromTerminalId = sritanjung.id,
        toTerminalId = situbondo.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 95.0,
        baseDurationMinutes = 135,
        tollFee = Money.ZERO,
        waypoints = segStbBwiNonToll.waypoints.reversed()
    )

    val segProboJbrNonToll = RouteSegment(
        id = "seg_probo_jbr_nontoll",
        fromTerminalId = bayuangga.id,
        toTerminalId = tawangAlun.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 100.0,
        baseDurationMinutes = 160,
        tollFee = Money.ZERO,
        waypoints = listOf(
            bayuangga.coordinate,
            MapCoordinate(0.48f, 0.48f, -7.88, 113.25),
            MapCoordinate(0.53f, 0.54f, -7.99, 113.34),
            MapCoordinate(0.60f, 0.59f, -8.11, 113.48),
            tawangAlun.coordinate
        )
    )

    val segJbrProboNonToll = RouteSegment(
        id = "seg_jbr_probo_nontoll",
        fromTerminalId = tawangAlun.id,
        toTerminalId = bayuangga.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 100.0,
        baseDurationMinutes = 160,
        tollFee = Money.ZERO,
        waypoints = segProboJbrNonToll.waypoints.reversed()
    )

    val segJbrBwiNonToll = RouteSegment(
        id = "seg_jbr_bwi_nontoll",
        fromTerminalId = tawangAlun.id,
        toTerminalId = sritanjung.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 110.0,
        baseDurationMinutes = 195,
        tollFee = Money.ZERO,
        waypoints = listOf(
            tawangAlun.coordinate,
            MapCoordinate(0.74f, 0.67f, -8.24, 113.82),
            MapCoordinate(0.79f, 0.70f, -8.30, 113.98),
            MapCoordinate(0.85f, 0.69f, -8.26, 114.15),
            sritanjung.coordinate
        )
    )

    val segBwiJbrNonToll = RouteSegment(
        id = "seg_bwi_jbr_nontoll",
        fromTerminalId = sritanjung.id,
        toTerminalId = tawangAlun.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 110.0,
        baseDurationMinutes = 195,
        tollFee = Money.ZERO,
        waypoints = segJbrBwiNonToll.waypoints.reversed()
    )

    val segMlgProboNonToll = RouteSegment(
        id = "seg_mlg_probo_nontoll",
        fromTerminalId = arjosari.id,
        toTerminalId = bayuangga.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 90.0,
        baseDurationMinutes = 140,
        tollFee = Money.ZERO,
        waypoints = listOf(
            arjosari.coordinate,
            MapCoordinate(0.18f, 0.42f, -7.75, 112.71),
            MapCoordinate(0.30f, 0.35f, -7.64, 112.90),
            bayuangga.coordinate
        )
    )

    val segProboMlgNonToll = RouteSegment(
        id = "seg_probo_mlg_nontoll",
        fromTerminalId = bayuangga.id,
        toTerminalId = arjosari.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 90.0,
        baseDurationMinutes = 140,
        tollFee = Money.ZERO,
        waypoints = segMlgProboNonToll.waypoints.reversed()
    )

    val segSbyMjkToll = RouteSegment(
        id = "seg_sby_mjk_toll",
        fromTerminalId = purabaya.id,
        toTerminalId = kertajaya.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 50.0,
        baseDurationMinutes = 40,
        tollFee = Money.rupiah(39000),
        waypoints = listOf(
            purabaya.coordinate,
            MapCoordinate(0.15f, 0.19f, -7.40, 112.60),
            MapCoordinate(0.13f, 0.22f, -7.44, 112.52),
            kertajaya.coordinate
        )
    )

    val segMjkSbyToll = RouteSegment(
        id = "seg_mjk_sby_toll",
        fromTerminalId = kertajaya.id,
        toTerminalId = purabaya.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 50.0,
        baseDurationMinutes = 40,
        tollFee = Money.rupiah(39000),
        waypoints = segSbyMjkToll.waypoints.reversed()
    )

    val segSbyMjkNonToll = RouteSegment(
        id = "seg_sby_mjk_nontoll",
        fromTerminalId = purabaya.id,
        toTerminalId = kertajaya.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 55.0,
        baseDurationMinutes = 85,
        tollFee = Money.ZERO,
        waypoints = listOf(
            purabaya.coordinate,
            MapCoordinate(0.16f, 0.20f, -7.42, 112.62),
            MapCoordinate(0.13f, 0.23f, -7.45, 112.54),
            kertajaya.coordinate
        )
    )

    val segMjkSbyNonToll = RouteSegment(
        id = "seg_mjk_sby_nontoll",
        fromTerminalId = kertajaya.id,
        toTerminalId = purabaya.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 55.0,
        baseDurationMinutes = 85,
        tollFee = Money.ZERO,
        waypoints = segSbyMjkNonToll.waypoints.reversed()
    )

    val segMjkKdrToll = RouteSegment(
        id = "seg_mjk_kdr_toll",
        fromTerminalId = kertajaya.id,
        toTerminalId = tamanan.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 80.0,
        baseDurationMinutes = 65,
        tollFee = Money.rupiah(55000),
        waypoints = listOf(
            kertajaya.coordinate,
            MapCoordinate(0.09f, 0.31f, -7.56, 112.25),
            MapCoordinate(0.07f, 0.37f, -7.69, 112.12),
            tamanan.coordinate
        )
    )

    val segKdrMjkToll = RouteSegment(
        id = "seg_kdr_mjk_toll",
        fromTerminalId = tamanan.id,
        toTerminalId = kertajaya.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 80.0,
        baseDurationMinutes = 65,
        tollFee = Money.rupiah(55000),
        waypoints = segMjkKdrToll.waypoints.reversed()
    )

    val segMjkKdrNonToll = RouteSegment(
        id = "seg_mjk_kdr_nontoll",
        fromTerminalId = kertajaya.id,
        toTerminalId = tamanan.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 85.0,
        baseDurationMinutes = 125,
        tollFee = Money.ZERO,
        waypoints = listOf(
            kertajaya.coordinate,
            MapCoordinate(0.09f, 0.32f, -7.57, 112.24),
            MapCoordinate(0.07f, 0.38f, -7.70, 112.13),
            tamanan.coordinate
        )
    )

    val segKdrMjkNonToll = RouteSegment(
        id = "seg_kdr_mjk_nontoll",
        fromTerminalId = tamanan.id,
        toTerminalId = kertajaya.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 85.0,
        baseDurationMinutes = 125,
        tollFee = Money.ZERO,
        waypoints = segMjkKdrNonToll.waypoints.reversed()
    )

    val segKdrTlgNonToll = RouteSegment(
        id = "seg_kdr_tlg_nontoll",
        fromTerminalId = tamanan.id,
        toTerminalId = gayatri.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 30.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = listOf(
            tamanan.coordinate,
            MapCoordinate(0.045f, 0.53f, -7.95, 111.95),
            gayatri.coordinate
        )
    )

    val segTlgKdrNonToll = RouteSegment(
        id = "seg_tlg_kdr_nontoll",
        fromTerminalId = gayatri.id,
        toTerminalId = tamanan.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 30.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = segKdrTlgNonToll.waypoints.reversed()
    )

    val segTlgBltNonToll = RouteSegment(
        id = "seg_tlg_blt_nontoll",
        fromTerminalId = gayatri.id,
        toTerminalId = patria.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 50,
        tollFee = Money.ZERO,
        waypoints = listOf(
            gayatri.coordinate,
            MapCoordinate(0.065f, 0.63f, -8.08, 112.03),
            patria.coordinate
        )
    )

    val segBltTlgNonToll = RouteSegment(
        id = "seg_blt_tlg_nontoll",
        fromTerminalId = patria.id,
        toTerminalId = gayatri.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 50,
        tollFee = Money.ZERO,
        waypoints = segTlgBltNonToll.waypoints.reversed()
    )

    val segBltMlgNonToll = RouteSegment(
        id = "seg_blt_mlg_nontoll",
        fromTerminalId = patria.id,
        toTerminalId = arjosari.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 80.0,
        baseDurationMinutes = 110,
        tollFee = Money.ZERO,
        waypoints = listOf(
            patria.coordinate,
            MapCoordinate(0.11f, 0.61f, -8.10, 112.35),
            MapCoordinate(0.125f, 0.57f, -8.13, 112.56),
            arjosari.coordinate
        )
    )

    val segMlgBltNonToll = RouteSegment(
        id = "seg_mlg_blt_nontoll",
        fromTerminalId = arjosari.id,
        toTerminalId = patria.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 80.0,
        baseDurationMinutes = 110,
        tollFee = Money.ZERO,
        waypoints = segBltMlgNonToll.waypoints.reversed()
    )

    fun getAllSegments(): List<RouteSegment> = listOf(
        segSbyMlgToll,
        segSbyMlgNonToll,
        segMlgSbyToll,
        segMlgSbyNonToll,
        segSbyProboToll,
        segSbyProboNonToll,
        segProboSbyToll,
        segProboSbyNonToll,
        segProboStbToll,
        segProboStbNonToll,
        segStbProboToll,
        segStbProboNonToll,
        segStbBwiNonToll,
        segBwiStbNonToll,
        segProboJbrNonToll,
        segJbrProboNonToll,
        segJbrBwiNonToll,
        segBwiJbrNonToll,
        segMlgProboNonToll,
        segProboMlgNonToll,
        segSbyMjkToll,
        segMjkSbyToll,
        segSbyMjkNonToll,
        segMjkSbyNonToll,
        segMjkKdrToll,
        segKdrMjkToll,
        segMjkKdrNonToll,
        segKdrMjkNonToll,
        segKdrTlgNonToll,
        segTlgKdrNonToll,
        segTlgBltNonToll,
        segBltTlgNonToll,
        segBltMlgNonToll,
        segMlgBltNonToll
    )

    fun getPredefinedRoutes(): List<BusRoute> = listOf(
        BusRoute(
            id = "route_sby_mlg_toll",
            name = "Surabaya - Malang (Tol Trans-Jawa)",
            originTerminal = purabaya,
            destinationTerminal = arjosari,
            pathType = RoutePathType.TOLL,
            segments = listOf(segSbyMlgToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_sby_mlg_nontoll",
            name = "Surabaya - Malang (Arteri Singosari)",
            originTerminal = purabaya,
            destinationTerminal = arjosari,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segSbyMlgNonToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_mlg_sby_toll",
            name = "Malang - Surabaya (Tol Trans-Jawa)",
            originTerminal = arjosari,
            destinationTerminal = purabaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segMlgSbyToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_mlg_sby_nontoll",
            name = "Malang - Surabaya (Arteri Singosari)",
            originTerminal = arjosari,
            destinationTerminal = purabaya,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segMlgSbyNonToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_sby_probo_toll",
            name = "Surabaya - Probolinggo (Tol Paspro)",
            originTerminal = purabaya,
            destinationTerminal = bayuangga,
            pathType = RoutePathType.TOLL,
            segments = listOf(segSbyProboToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_sby_probo_nontoll",
            name = "Surabaya - Probolinggo (Arteri Pantura)",
            originTerminal = purabaya,
            destinationTerminal = bayuangga,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segSbyProboNonToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_probo_sby_toll",
            name = "Probolinggo - Surabaya (Tol Paspro)",
            originTerminal = bayuangga,
            destinationTerminal = purabaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segProboSbyToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_probo_sby_nontoll",
            name = "Probolinggo - Surabaya (Arteri Pantura)",
            originTerminal = bayuangga,
            destinationTerminal = purabaya,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segProboSbyNonToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_sby_bwi_pantura_toll",
            name = "Surabaya - Banyuwangi (Via Tol & Pantura)",
            originTerminal = purabaya,
            destinationTerminal = sritanjung,
            pathType = RoutePathType.TOLL,
            segments = listOf(segSbyProboToll, segProboStbToll, segStbBwiNonToll),
            transitTerminals = listOf(bayuangga, situbondo)
        ),
        BusRoute(
            id = "route_sby_bwi_pantura_nontoll",
            name = "Surabaya - Banyuwangi (Full Arteri Pantura)",
            originTerminal = purabaya,
            destinationTerminal = sritanjung,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segSbyProboNonToll, segProboStbNonToll, segStbBwiNonToll),
            transitTerminals = listOf(bayuangga, situbondo)
        ),
        BusRoute(
            id = "route_bwi_sby_pantura_toll",
            name = "Banyuwangi - Surabaya (Via Tol & Pantura)",
            originTerminal = sritanjung,
            destinationTerminal = purabaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segBwiStbNonToll, segStbProboToll, segProboSbyToll),
            transitTerminals = listOf(situbondo, bayuangga)
        ),
        BusRoute(
            id = "route_bwi_sby_pantura_nontoll",
            name = "Banyuwangi - Surabaya (Full Arteri Pantura)",
            originTerminal = sritanjung,
            destinationTerminal = purabaya,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segBwiStbNonToll, segStbProboNonToll, segProboSbyNonToll),
            transitTerminals = listOf(situbondo, bayuangga)
        ),
        BusRoute(
            id = "route_sby_bwi_selatan_toll",
            name = "Surabaya - Banyuwangi (Via Tol & Jember/Gumitir)",
            originTerminal = purabaya,
            destinationTerminal = sritanjung,
            pathType = RoutePathType.TOLL,
            segments = listOf(segSbyProboToll, segProboJbrNonToll, segJbrBwiNonToll),
            transitTerminals = listOf(bayuangga, tawangAlun)
        ),
        BusRoute(
            id = "route_sby_bwi_selatan_nontoll",
            name = "Surabaya - Banyuwangi (Full Arteri Jember/Gumitir)",
            originTerminal = purabaya,
            destinationTerminal = sritanjung,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segSbyProboNonToll, segProboJbrNonToll, segJbrBwiNonToll),
            transitTerminals = listOf(bayuangga, tawangAlun)
        ),
        BusRoute(
            id = "route_bwi_sby_selatan_toll",
            name = "Banyuwangi - Surabaya (Via Jember/Gumitir & Tol)",
            originTerminal = sritanjung,
            destinationTerminal = purabaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segBwiJbrNonToll, segJbrProboNonToll, segProboSbyToll),
            transitTerminals = listOf(tawangAlun, bayuangga)
        ),
        BusRoute(
            id = "route_bwi_sby_selatan_nontoll",
            name = "Banyuwangi - Surabaya (Full Arteri Jember/Gumitir)",
            originTerminal = sritanjung,
            destinationTerminal = purabaya,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segBwiJbrNonToll, segJbrProboNonToll, segProboSbyNonToll),
            transitTerminals = listOf(tawangAlun, bayuangga)
        ),
        BusRoute(
            id = "route_mlg_bwi_selatan",
            name = "Malang - Banyuwangi (Via Jember/Gumitir)",
            originTerminal = arjosari,
            destinationTerminal = sritanjung,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segMlgProboNonToll, segProboJbrNonToll, segJbrBwiNonToll),
            transitTerminals = listOf(bayuangga, tawangAlun)
        ),
        BusRoute(
            id = "route_bwi_mlg_selatan",
            name = "Banyuwangi - Malang (Via Jember/Gumitir)",
            originTerminal = sritanjung,
            destinationTerminal = arjosari,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segBwiJbrNonToll, segJbrProboNonToll, segProboMlgNonToll),
            transitTerminals = listOf(tawangAlun, bayuangga)
        ),
        BusRoute(
            id = "route_sby_mjk_toll",
            name = "Surabaya - Mojokerto (Tol SUMO)",
            originTerminal = purabaya,
            destinationTerminal = kertajaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segSbyMjkToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_sby_mjk_nontoll",
            name = "Surabaya - Mojokerto (Arteri Krian)",
            originTerminal = purabaya,
            destinationTerminal = kertajaya,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segSbyMjkNonToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_mjk_sby_toll",
            name = "Mojokerto - Surabaya (Tol SUMO)",
            originTerminal = kertajaya,
            destinationTerminal = purabaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segMjkSbyToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_mjk_sby_nontoll",
            name = "Mojokerto - Surabaya (Arteri Krian)",
            originTerminal = kertajaya,
            destinationTerminal = purabaya,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segMjkSbyNonToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_sby_kdr_toll",
            name = "Surabaya - Kediri (Via Tol SUMO & Kertosono)",
            originTerminal = purabaya,
            destinationTerminal = tamanan,
            pathType = RoutePathType.TOLL,
            segments = listOf(segSbyMjkToll, segMjkKdrToll),
            transitTerminals = listOf(kertajaya)
        ),
        BusRoute(
            id = "route_kdr_sby_toll",
            name = "Kediri - Surabaya (Via Kertosono & Tol SUMO)",
            originTerminal = tamanan,
            destinationTerminal = purabaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segKdrMjkToll, segMjkSbyToll),
            transitTerminals = listOf(kertajaya)
        ),
        BusRoute(
            id = "route_kdr_mlg_selatan",
            name = "Kediri - Malang (Jalur Selatan Tulungagung - Blitar)",
            originTerminal = tamanan,
            destinationTerminal = arjosari,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segKdrTlgNonToll, segTlgBltNonToll, segBltMlgNonToll),
            transitTerminals = listOf(gayatri, patria)
        ),
        BusRoute(
            id = "route_mlg_kdr_selatan",
            name = "Malang - Kediri (Jalur Selatan Blitar - Tulungagung)",
            originTerminal = arjosari,
            destinationTerminal = tamanan,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segMlgBltNonToll, segBltTlgNonToll, segTlgKdrNonToll),
            transitTerminals = listOf(patria, gayatri)
        ),
        BusRoute(
            id = "route_mlg_blt_selatan",
            name = "Malang - Blitar (Arteri Kepanjen - Kesamben)",
            originTerminal = arjosari,
            destinationTerminal = patria,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segMlgBltNonToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_blt_mlg_selatan",
            name = "Blitar - Malang (Arteri Kesamben - Kepanjen)",
            originTerminal = patria,
            destinationTerminal = arjosari,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segBltMlgNonToll),
            transitTerminals = emptyList()
        ),
        BusRoute(
            id = "route_sby_blt_selatan",
            name = "Surabaya - Blitar (Via Tol SUMO & Kediri)",
            originTerminal = purabaya,
            destinationTerminal = patria,
            pathType = RoutePathType.TOLL,
            segments = listOf(segSbyMjkToll, segMjkKdrToll, segKdrTlgNonToll, segTlgBltNonToll),
            transitTerminals = listOf(kertajaya, tamanan, gayatri)
        ),
        BusRoute(
            id = "route_blt_sby_selatan",
            name = "Blitar - Surabaya (Via Kediri & Tol SUMO)",
            originTerminal = patria,
            destinationTerminal = purabaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segBltTlgNonToll, segTlgKdrNonToll, segKdrMjkToll, segMjkSbyToll),
            transitTerminals = listOf(gayatri, tamanan, kertajaya)
        )
    )
}
