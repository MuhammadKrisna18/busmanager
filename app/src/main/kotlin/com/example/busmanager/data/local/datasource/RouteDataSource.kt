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
    private val surodakan = terminalDataSource.getTerminalById("term_surodakan")!!
    private val seloaji = terminalDataSource.getTerminalById("term_seloaji")!!
    private val purboyo = terminalDataSource.getTerminalById("term_purboyo")!!
    private val kertonegoro = terminalDataSource.getTerminalById("term_kertonegoro")!!
    private val anjukLadang = terminalDataSource.getTerminalById("term_anjukladang")!!
    private val rajekwesi = terminalDataSource.getTerminalById("term_rajekwesi")!!
    private val minakKoncar = terminalDataSource.getTerminalById("term_lumajang")!!
    private val bondowoso = terminalDataSource.getTerminalById("term_bondowoso")!!
    private val pacitan = terminalDataSource.getTerminalById("term_pacitan")!!
    private val pilangsari = terminalDataSource.getTerminalById("term_sragen")!!
    private val kembangJoyo = terminalDataSource.getTerminalById("term_pati")!!

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
            MapCoordinate(0.46f, 0.38f, -7.50, 112.71),
            MapCoordinate(0.46f, 0.44f, -7.60, 112.70),
            MapCoordinate(0.45f, 0.55f, -7.78, 112.69),
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
            MapCoordinate(0.46f, 0.34f, -7.45, 112.72),
            MapCoordinate(0.46f, 0.41f, -7.56, 112.70),
            MapCoordinate(0.46f, 0.53f, -7.75, 112.71),
            MapCoordinate(0.45f, 0.61f, -7.88, 112.67),
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
            MapCoordinate(0.46f, 0.42f, -7.57, 112.71),
            MapCoordinate(0.51f, 0.46f, -7.64, 112.90),
            MapCoordinate(0.57f, 0.51f, -7.71, 113.08),
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
            MapCoordinate(0.48f, 0.39f, -7.52, 112.77),
            MapCoordinate(0.52f, 0.46f, -7.63, 112.91),
            MapCoordinate(0.55f, 0.49f, -7.68, 113.03),
            MapCoordinate(0.58f, 0.52f, -7.73, 113.13),
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
            MapCoordinate(0.66f, 0.53f, -7.74, 113.40),
            MapCoordinate(0.72f, 0.51f, -7.71, 113.63),
            MapCoordinate(0.78f, 0.51f, -7.72, 113.82),
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
            MapCoordinate(0.62f, 0.53f, -7.75, 113.28),
            MapCoordinate(0.67f, 0.52f, -7.73, 113.45),
            MapCoordinate(0.74f, 0.50f, -7.70, 113.68),
            MapCoordinate(0.79f, 0.51f, -7.71, 113.88),
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
            MapCoordinate(0.88f, 0.52f, -7.75, 114.15),
            MapCoordinate(0.93f, 0.58f, -7.85, 114.38),
            MapCoordinate(0.95f, 0.65f, -7.95, 114.42),
            MapCoordinate(0.945f, 0.72f, -8.05, 114.40),
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
            MapCoordinate(0.605f, 0.58f, -7.88, 113.20),
            MapCoordinate(0.61f, 0.64f, -7.99, 113.21),
            MapCoordinate(0.61f, 0.72f, -8.13, 113.22),
            MapCoordinate(0.65f, 0.75f, -8.15, 113.34),
            MapCoordinate(0.68f, 0.77f, -8.16, 113.45),
            MapCoordinate(0.71f, 0.79f, -8.18, 113.56),
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
            MapCoordinate(0.76f, 0.81f, -8.19, 113.78),
            MapCoordinate(0.79f, 0.83f, -8.25, 113.91),
            MapCoordinate(0.83f, 0.82f, -8.28, 114.02),
            MapCoordinate(0.87f, 0.80f, -8.32, 114.15),
            MapCoordinate(0.91f, 0.78f, -8.30, 114.28),
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
            MapCoordinate(0.46f, 0.53f, -7.75, 112.71),
            MapCoordinate(0.51f, 0.46f, -7.64, 112.90),
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
            MapCoordinate(0.43f, 0.31f, -7.40, 112.60),
            MapCoordinate(0.41f, 0.34f, -7.44, 112.52),
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
            MapCoordinate(0.43f, 0.33f, -7.42, 112.62),
            MapCoordinate(0.41f, 0.34f, -7.45, 112.54),
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
            MapCoordinate(0.33f, 0.41f, -7.56, 112.25),
            MapCoordinate(0.29f, 0.49f, -7.69, 112.12),
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
            MapCoordinate(0.33f, 0.42f, -7.57, 112.24),
            MapCoordinate(0.29f, 0.50f, -7.70, 112.13),
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
            MapCoordinate(0.24f, 0.66f, -7.95, 111.95),
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
            MapCoordinate(0.27f, 0.74f, -8.08, 112.03),
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
            MapCoordinate(0.36f, 0.75f, -8.10, 112.35),
            MapCoordinate(0.42f, 0.77f, -8.13, 112.56),
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

    val segTlgTrgNonToll = RouteSegment(
        id = "seg_tlg_trg_nontoll",
        fromTerminalId = gayatri.id,
        toTerminalId = surodakan.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 30.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = listOf(
            gayatri.coordinate,
            MapCoordinate(0.20f, 0.72f, -8.06, 111.81),
            surodakan.coordinate
        )
    )

    val segTrgTlgNonToll = RouteSegment(
        id = "seg_trg_tlg_nontoll",
        fromTerminalId = surodakan.id,
        toTerminalId = gayatri.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 30.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = segTlgTrgNonToll.waypoints.reversed()
    )

    val segTrgPnrNonToll = RouteSegment(
        id = "seg_trg_pnr_nontoll",
        fromTerminalId = surodakan.id,
        toTerminalId = seloaji.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 50.0,
        baseDurationMinutes = 70,
        tollFee = Money.ZERO,
        waypoints = listOf(
            surodakan.coordinate,
            MapCoordinate(0.15f, 0.66f, -7.95, 111.60),
            seloaji.coordinate
        )
    )

    val segPnrTrgNonToll = RouteSegment(
        id = "seg_pnr_trg_nontoll",
        fromTerminalId = seloaji.id,
        toTerminalId = surodakan.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 50.0,
        baseDurationMinutes = 70,
        tollFee = Money.ZERO,
        waypoints = segTrgPnrNonToll.waypoints.reversed()
    )

    val segPnrMdnNonToll = RouteSegment(
        id = "seg_pnr_mdn_nontoll",
        fromTerminalId = seloaji.id,
        toTerminalId = purboyo.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 30.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = listOf(
            seloaji.coordinate,
            MapCoordinate(0.115f, 0.51f, -7.72, 111.50),
            purboyo.coordinate
        )
    )

    val segMdnPnrNonToll = RouteSegment(
        id = "seg_mdn_pnr_nontoll",
        fromTerminalId = purboyo.id,
        toTerminalId = seloaji.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 30.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = segPnrMdnNonToll.waypoints.reversed()
    )

    val segMdnNgwToll = RouteSegment(
        id = "seg_mdn_ngw_toll",
        fromTerminalId = purboyo.id,
        toTerminalId = kertonegoro.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 25,
        tollFee = Money.rupiah(26000),
        waypoints = listOf(
            purboyo.coordinate,
            MapCoordinate(0.11f, 0.38f, -7.50, 111.48),
            kertonegoro.coordinate
        )
    )

    val segNgwMdnToll = RouteSegment(
        id = "seg_ngw_mdn_toll",
        fromTerminalId = kertonegoro.id,
        toTerminalId = purboyo.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 25,
        tollFee = Money.rupiah(26000),
        waypoints = segMdnNgwToll.waypoints.reversed()
    )

    val segMdnNgwNonToll = RouteSegment(
        id = "seg_mdn_ngw_nontoll",
        fromTerminalId = purboyo.id,
        toTerminalId = kertonegoro.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 38.0,
        baseDurationMinutes = 55,
        tollFee = Money.ZERO,
        waypoints = listOf(
            purboyo.coordinate,
            MapCoordinate(0.11f, 0.38f, -7.51, 111.47),
            kertonegoro.coordinate
        )
    )

    val segNgwMdnNonToll = RouteSegment(
        id = "seg_ngw_mdn_nontoll",
        fromTerminalId = kertonegoro.id,
        toTerminalId = purboyo.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 38.0,
        baseDurationMinutes = 55,
        tollFee = Money.ZERO,
        waypoints = segMdnNgwNonToll.waypoints.reversed()
    )

    val segMdnNgjToll = RouteSegment(
        id = "seg_mdn_ngj_toll",
        fromTerminalId = purboyo.id,
        toTerminalId = anjukLadang.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 48.0,
        baseDurationMinutes = 35,
        tollFee = Money.rupiah(38000),
        waypoints = listOf(
            purboyo.coordinate,
            MapCoordinate(0.17f, 0.44f, -7.58, 111.72),
            anjukLadang.coordinate
        )
    )

    val segNgjMdnToll = RouteSegment(
        id = "seg_ngj_mdn_toll",
        fromTerminalId = anjukLadang.id,
        toTerminalId = purboyo.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 48.0,
        baseDurationMinutes = 35,
        tollFee = Money.rupiah(38000),
        waypoints = segMdnNgjToll.waypoints.reversed()
    )

    val segMdnNgjNonToll = RouteSegment(
        id = "seg_mdn_ngj_nontoll",
        fromTerminalId = purboyo.id,
        toTerminalId = anjukLadang.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 52.0,
        baseDurationMinutes = 75,
        tollFee = Money.ZERO,
        waypoints = listOf(
            purboyo.coordinate,
            MapCoordinate(0.17f, 0.45f, -7.59, 111.70),
            anjukLadang.coordinate
        )
    )

    val segNgjMdnNonToll = RouteSegment(
        id = "seg_ngj_mdn_nontoll",
        fromTerminalId = anjukLadang.id,
        toTerminalId = purboyo.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 52.0,
        baseDurationMinutes = 75,
        tollFee = Money.ZERO,
        waypoints = segMdnNgjNonToll.waypoints.reversed()
    )

    val segNgjKdrNonToll = RouteSegment(
        id = "seg_ngj_kdr_nontoll",
        fromTerminalId = anjukLadang.id,
        toTerminalId = tamanan.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 50,
        tollFee = Money.ZERO,
        waypoints = listOf(
            anjukLadang.coordinate,
            MapCoordinate(0.25f, 0.51f, -7.71, 111.96),
            tamanan.coordinate
        )
    )

    val segKdrNgjNonToll = RouteSegment(
        id = "seg_kdr_ngj_nontoll",
        fromTerminalId = tamanan.id,
        toTerminalId = anjukLadang.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 50,
        tollFee = Money.ZERO,
        waypoints = segNgjKdrNonToll.waypoints.reversed()
    )

    val segNgjMjkToll = RouteSegment(
        id = "seg_ngj_mjk_toll",
        fromTerminalId = anjukLadang.id,
        toTerminalId = kertajaya.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 65.0,
        baseDurationMinutes = 45,
        tollFee = Money.rupiah(54000),
        waypoints = listOf(
            anjukLadang.coordinate,
            MapCoordinate(0.31f, 0.40f, -7.54, 112.18),
            kertajaya.coordinate
        )
    )

    val segMjkNgjToll = RouteSegment(
        id = "seg_mjk_ngj_toll",
        fromTerminalId = kertajaya.id,
        toTerminalId = anjukLadang.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 65.0,
        baseDurationMinutes = 45,
        tollFee = Money.rupiah(54000),
        waypoints = segNgjMjkToll.waypoints.reversed()
    )

    val segNgjMjkNonToll = RouteSegment(
        id = "seg_ngj_mjk_nontoll",
        fromTerminalId = anjukLadang.id,
        toTerminalId = kertajaya.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 70.0,
        baseDurationMinutes = 105,
        tollFee = Money.ZERO,
        waypoints = listOf(
            anjukLadang.coordinate,
            MapCoordinate(0.30f, 0.41f, -7.55, 112.15),
            kertajaya.coordinate
        )
    )

    val segMjkNgjNonToll = RouteSegment(
        id = "seg_mjk_ngj_nontoll",
        fromTerminalId = kertajaya.id,
        toTerminalId = anjukLadang.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 70.0,
        baseDurationMinutes = 105,
        tollFee = Money.ZERO,
        waypoints = segNgjMjkNonToll.waypoints.reversed()
    )

    val segNgwBjnNonToll = RouteSegment(
        id = "seg_ngw_bjn_nontoll",
        fromTerminalId = kertonegoro.id,
        toTerminalId = rajekwesi.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 70.0,
        baseDurationMinutes = 95,
        tollFee = Money.ZERO,
        waypoints = listOf(
            kertonegoro.coordinate,
            MapCoordinate(0.16f, 0.23f, -7.28, 111.66),
            rajekwesi.coordinate
        )
    )

    val segBjnNgwNonToll = RouteSegment(
        id = "seg_bjn_ngw_nontoll",
        fromTerminalId = rajekwesi.id,
        toTerminalId = kertonegoro.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 70.0,
        baseDurationMinutes = 95,
        tollFee = Money.ZERO,
        waypoints = segNgwBjnNonToll.waypoints.reversed()
    )

    val segBjnNgjNonToll = RouteSegment(
        id = "seg_bjn_ngj_nontoll",
        fromTerminalId = rajekwesi.id,
        toTerminalId = anjukLadang.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 60.0,
        baseDurationMinutes = 85,
        tollFee = Money.ZERO,
        waypoints = listOf(
            rajekwesi.coordinate,
            MapCoordinate(0.23f, 0.30f, -7.38, 111.90),
            anjukLadang.coordinate
        )
    )

    val segNgjBjnNonToll = RouteSegment(
        id = "seg_ngj_bjn_nontoll",
        fromTerminalId = anjukLadang.id,
        toTerminalId = rajekwesi.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 60.0,
        baseDurationMinutes = 85,
        tollFee = Money.ZERO,
        waypoints = segBjnNgjNonToll.waypoints.reversed()
    )

    val segProboLmjNonToll = RouteSegment(
        id = "seg_probo_lmj_nontoll",
        fromTerminalId = bayuangga.id,
        toTerminalId = minakKoncar.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 45.0,
        baseDurationMinutes = 55,
        tollFee = Money.ZERO,
        waypoints = listOf(
            bayuangga.coordinate,
            MapCoordinate(0.605f, 0.58f, -7.88, 113.20),
            MapCoordinate(0.61f, 0.64f, -7.99, 113.21),
            minakKoncar.coordinate
        )
    )

    val segLmjProboNonToll = RouteSegment(
        id = "seg_lmj_probo_nontoll",
        fromTerminalId = minakKoncar.id,
        toTerminalId = bayuangga.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 45.0,
        baseDurationMinutes = 55,
        tollFee = Money.ZERO,
        waypoints = segProboLmjNonToll.waypoints.reversed()
    )

    val segLmjJbrNonToll = RouteSegment(
        id = "seg_lmj_jbr_nontoll",
        fromTerminalId = minakKoncar.id,
        toTerminalId = tawangAlun.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 65.0,
        baseDurationMinutes = 75,
        tollFee = Money.ZERO,
        waypoints = listOf(
            minakKoncar.coordinate,
            MapCoordinate(0.65f, 0.75f, -8.15, 113.34),
            MapCoordinate(0.68f, 0.77f, -8.16, 113.45),
            MapCoordinate(0.71f, 0.79f, -8.18, 113.56),
            tawangAlun.coordinate
        )
    )

    val segJbrLmjNonToll = RouteSegment(
        id = "seg_jbr_lmj_nontoll",
        fromTerminalId = tawangAlun.id,
        toTerminalId = minakKoncar.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 65.0,
        baseDurationMinutes = 75,
        tollFee = Money.ZERO,
        waypoints = segLmjJbrNonToll.waypoints.reversed()
    )

    val segMlgLmjNonToll = RouteSegment(
        id = "seg_mlg_lmj_nontoll",
        fromTerminalId = arjosari.id,
        toTerminalId = minakKoncar.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 110.0,
        baseDurationMinutes = 160,
        tollFee = Money.ZERO,
        waypoints = listOf(
            arjosari.coordinate,
            MapCoordinate(0.48f, 0.68f, -8.05, 112.72),
            MapCoordinate(0.52f, 0.71f, -8.20, 112.87),
            MapCoordinate(0.56f, 0.73f, -8.22, 112.98),
            minakKoncar.coordinate
        )
    )

    val segLmjMlgNonToll = RouteSegment(
        id = "seg_lmj_mlg_nontoll",
        fromTerminalId = minakKoncar.id,
        toTerminalId = arjosari.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 110.0,
        baseDurationMinutes = 160,
        tollFee = Money.ZERO,
        waypoints = segMlgLmjNonToll.waypoints.reversed()
    )

    val segStbBdwNonToll = RouteSegment(
        id = "seg_stb_bdw_nontoll",
        fromTerminalId = situbondo.id,
        toTerminalId = bondowoso.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = listOf(
            situbondo.coordinate,
            MapCoordinate(0.81f, 0.56f, -7.80, 113.92),
            MapCoordinate(0.795f, 0.60f, -7.86, 113.88),
            bondowoso.coordinate
        )
    )

    val segBdwStbNonToll = RouteSegment(
        id = "seg_bdw_stb_nontoll",
        fromTerminalId = bondowoso.id,
        toTerminalId = situbondo.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = segStbBdwNonToll.waypoints.reversed()
    )

    val segBdwJbrNonToll = RouteSegment(
        id = "seg_bdw_jbr_nontoll",
        fromTerminalId = bondowoso.id,
        toTerminalId = tawangAlun.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = listOf(
            bondowoso.coordinate,
            MapCoordinate(0.76f, 0.69f, -8.01, 113.78),
            MapCoordinate(0.745f, 0.75f, -8.11, 113.71),
            tawangAlun.coordinate
        )
    )

    val segJbrBdwNonToll = RouteSegment(
        id = "seg_jbr_bdw_nontoll",
        fromTerminalId = tawangAlun.id,
        toTerminalId = bondowoso.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 35.0,
        baseDurationMinutes = 45,
        tollFee = Money.ZERO,
        waypoints = segBdwJbrNonToll.waypoints.reversed()
    )

    val segProboBdwNonToll = RouteSegment(
        id = "seg_probo_bdw_nontoll",
        fromTerminalId = bayuangga.id,
        toTerminalId = bondowoso.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 70.0,
        baseDurationMinutes = 90,
        tollFee = Money.ZERO,
        waypoints = listOf(
            bayuangga.coordinate,
            MapCoordinate(0.66f, 0.53f, -7.74, 113.35),
            MapCoordinate(0.72f, 0.51f, -7.72, 113.68),
            MapCoordinate(0.75f, 0.57f, -7.82, 113.75),
            bondowoso.coordinate
        )
    )

    val segBdwProboNonToll = RouteSegment(
        id = "seg_bdw_probo_nontoll",
        fromTerminalId = bondowoso.id,
        toTerminalId = bayuangga.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 70.0,
        baseDurationMinutes = 90,
        tollFee = Money.ZERO,
        waypoints = segProboBdwNonToll.waypoints.reversed()
    )

    val segPnrPctNonToll = RouteSegment(
        id = "seg_pnr_pct_nontoll",
        fromTerminalId = seloaji.id,
        toTerminalId = pacitan.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 75.0,
        baseDurationMinutes = 105,
        tollFee = Money.ZERO,
        waypoints = listOf(
            seloaji.coordinate,
            MapCoordinate(0.09f, 0.66f, -7.95, 111.38),
            MapCoordinate(0.075f, 0.72f, -8.05, 111.25),
            MapCoordinate(0.06f, 0.76f, -8.12, 111.15),
            pacitan.coordinate
        )
    )

    val segPctPnrNonToll = RouteSegment(
        id = "seg_pct_pnr_nontoll",
        fromTerminalId = pacitan.id,
        toTerminalId = seloaji.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 75.0,
        baseDurationMinutes = 105,
        tollFee = Money.ZERO,
        waypoints = segPnrPctNonToll.waypoints.reversed()
    )

    val segTrgPctNonToll = RouteSegment(
        id = "seg_trg_pct_nontoll",
        fromTerminalId = surodakan.id,
        toTerminalId = pacitan.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 105.0,
        baseDurationMinutes = 145,
        tollFee = Money.ZERO,
        waypoints = listOf(
            surodakan.coordinate,
            MapCoordinate(0.14f, 0.76f, -8.12, 111.55),
            MapCoordinate(0.10f, 0.79f, -8.21, 111.32),
            MapCoordinate(0.07f, 0.81f, -8.22, 111.19),
            pacitan.coordinate
        )
    )

    val segPctTrgNonToll = RouteSegment(
        id = "seg_pct_trg_nontoll",
        fromTerminalId = pacitan.id,
        toTerminalId = surodakan.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.MOUNTAIN_PASS,
        distanceKm = 105.0,
        baseDurationMinutes = 145,
        tollFee = Money.ZERO,
        waypoints = segTrgPctNonToll.waypoints.reversed()
    )

    val segNgwSrgToll = RouteSegment(
        id = "seg_ngw_srg_toll",
        fromTerminalId = kertonegoro.id,
        toTerminalId = pilangsari.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 55.0,
        baseDurationMinutes = 38,
        tollFee = Money.rupiah(45000),
        waypoints = listOf(
            kertonegoro.coordinate,
            MapCoordinate(0.08f, 0.325f, -7.42, 111.23),
            MapCoordinate(0.06f, 0.328f, -7.43, 111.12),
            pilangsari.coordinate
        )
    )

    val segSrgNgwToll = RouteSegment(
        id = "seg_srg_ngw_toll",
        fromTerminalId = pilangsari.id,
        toTerminalId = kertonegoro.id,
        pathType = RoutePathType.TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 55.0,
        baseDurationMinutes = 38,
        tollFee = Money.rupiah(45000),
        waypoints = segNgwSrgToll.waypoints.reversed()
    )

    val segNgwSrgNonToll = RouteSegment(
        id = "seg_ngw_srg_nontoll",
        fromTerminalId = kertonegoro.id,
        toTerminalId = pilangsari.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 58.0,
        baseDurationMinutes = 75,
        tollFee = Money.ZERO,
        waypoints = listOf(
            kertonegoro.coordinate,
            MapCoordinate(0.08f, 0.335f, -7.43, 111.22),
            MapCoordinate(0.06f, 0.338f, -7.44, 111.11),
            pilangsari.coordinate
        )
    )

    val segSrgNgwNonToll = RouteSegment(
        id = "seg_srg_ngw_nontoll",
        fromTerminalId = pilangsari.id,
        toTerminalId = kertonegoro.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 58.0,
        baseDurationMinutes = 75,
        tollFee = Money.ZERO,
        waypoints = segNgwSrgNonToll.waypoints.reversed()
    )

    val segBjnPtiNonToll = RouteSegment(
        id = "seg_bjn_pti_nontoll",
        fromTerminalId = rajekwesi.id,
        toTerminalId = kembangJoyo.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 120.0,
        baseDurationMinutes = 160,
        tollFee = Money.ZERO,
        waypoints = listOf(
            rajekwesi.coordinate,
            MapCoordinate(0.18f, 0.165f, -7.15, 111.60),
            MapCoordinate(0.13f, 0.15f, -6.97, 111.41),
            MapCoordinate(0.09f, 0.11f, -6.71, 111.34),
            MapCoordinate(0.07f, 0.105f, -6.73, 111.18),
            kembangJoyo.coordinate
        )
    )

    val segPtiBjnNonToll = RouteSegment(
        id = "seg_pti_bjn_nontoll",
        fromTerminalId = kembangJoyo.id,
        toTerminalId = rajekwesi.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 120.0,
        baseDurationMinutes = 160,
        tollFee = Money.ZERO,
        waypoints = segBjnPtiNonToll.waypoints.reversed()
    )

    val segSrgPtiNonToll = RouteSegment(
        id = "seg_srg_pti_nontoll",
        fromTerminalId = pilangsari.id,
        toTerminalId = kembangJoyo.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 105.0,
        baseDurationMinutes = 140,
        tollFee = Money.ZERO,
        waypoints = listOf(
            pilangsari.coordinate,
            MapCoordinate(0.04f, 0.25f, -7.22, 110.98),
            MapCoordinate(0.045f, 0.18f, -7.08, 110.92),
            MapCoordinate(0.048f, 0.14f, -6.90, 111.00),
            kembangJoyo.coordinate
        )
    )

    val segPtiSrgNonToll = RouteSegment(
        id = "seg_pti_srg_nontoll",
        fromTerminalId = kembangJoyo.id,
        toTerminalId = pilangsari.id,
        pathType = RoutePathType.NON_TOLL,
        characteristic = RoadCharacteristic.FLAT,
        distanceKm = 105.0,
        baseDurationMinutes = 140,
        tollFee = Money.ZERO,
        waypoints = segSrgPtiNonToll.waypoints.reversed()
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
        segMlgBltNonToll,
        segTlgTrgNonToll,
        segTrgTlgNonToll,
        segTrgPnrNonToll,
        segPnrTrgNonToll,
        segPnrMdnNonToll,
        segMdnPnrNonToll,
        segMdnNgwToll,
        segNgwMdnToll,
        segMdnNgwNonToll,
        segNgwMdnNonToll,
        segMdnNgjToll,
        segNgjMdnToll,
        segMdnNgjNonToll,
        segNgjMdnNonToll,
        segNgjKdrNonToll,
        segKdrNgjNonToll,
        segNgjMjkToll,
        segMjkNgjToll,
        segNgjMjkNonToll,
        segMjkNgjNonToll,
        segNgwBjnNonToll,
        segBjnNgwNonToll,
        segBjnNgjNonToll,
        segNgjBjnNonToll,
        segProboLmjNonToll,
        segLmjProboNonToll,
        segLmjJbrNonToll,
        segJbrLmjNonToll,
        segMlgLmjNonToll,
        segLmjMlgNonToll,
        segStbBdwNonToll,
        segBdwStbNonToll,
        segBdwJbrNonToll,
        segJbrBdwNonToll,
        segProboBdwNonToll,
        segBdwProboNonToll,
        segPnrPctNonToll,
        segPctPnrNonToll,
        segTrgPctNonToll,
        segPctTrgNonToll,
        segNgwSrgToll,
        segSrgNgwToll,
        segNgwSrgNonToll,
        segSrgNgwNonToll,
        segBjnPtiNonToll,
        segPtiBjnNonToll,
        segSrgPtiNonToll,
        segPtiSrgNonToll
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
            id = "route_sby_bwi_gumitir_toll",
            name = "Surabaya - Banyuwangi (Via Tol & Jember/Gumitir)",
            originTerminal = purabaya,
            destinationTerminal = sritanjung,
            pathType = RoutePathType.TOLL,
            segments = listOf(segSbyProboToll, segProboJbrNonToll, segJbrBwiNonToll),
            transitTerminals = listOf(bayuangga, tawangAlun)
        ),
        BusRoute(
            id = "route_sby_bwi_gumitir_nontoll",
            name = "Surabaya - Banyuwangi (Full Arteri Jember/Gumitir)",
            originTerminal = purabaya,
            destinationTerminal = sritanjung,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segSbyProboNonToll, segProboJbrNonToll, segJbrBwiNonToll),
            transitTerminals = listOf(bayuangga, tawangAlun)
        ),
        BusRoute(
            id = "route_bwi_sby_gumitir_toll",
            name = "Banyuwangi - Surabaya (Via Jember/Gumitir & Tol)",
            originTerminal = sritanjung,
            destinationTerminal = purabaya,
            pathType = RoutePathType.TOLL,
            segments = listOf(segBwiJbrNonToll, segJbrProboNonToll, segProboSbyToll),
            transitTerminals = listOf(tawangAlun, bayuangga)
        ),
        BusRoute(
            id = "route_bwi_sby_gumitir_nontoll",
            name = "Banyuwangi - Surabaya (Full Arteri Jember/Gumitir)",
            originTerminal = sritanjung,
            destinationTerminal = purabaya,
            pathType = RoutePathType.NON_TOLL,
            segments = listOf(segBwiJbrNonToll, segJbrProboNonToll, segProboSbyNonToll),
            transitTerminals = listOf(tawangAlun, bayuangga)
        )
    )
}
