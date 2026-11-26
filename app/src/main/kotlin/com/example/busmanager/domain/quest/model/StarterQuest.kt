package com.example.busmanager.domain.quest.model

import com.example.busmanager.domain.company.model.Money

enum class QuestId(
    val title: String,
    val description: String,
    val targetCount: Int,
    val rewardMoney: Money
) {
    BUY_FIRST_BUS(
        title = "Beli Armada Pertama",
        description = "Beli bus pertama PO Anda melalui menu Showroom",
        targetCount = 1,
        rewardMoney = Money.rupiah(50_000_000L)
    ),
    HIRE_FIRST_DRIVER(
        title = "Rekrut Pengemudi Perdana",
        description = "Rekrut pengemudi berlisensi di menu Manajemen Kru",
        targetCount = 1,
        rewardMoney = Money.rupiah(25_000_000L)
    ),
    CREATE_FIRST_ROUTE(
        title = "Buka Trayek Pertama",
        description = "Rancang jalur dan buka trayek perdana melalui Wizard Rute",
        targetCount = 1,
        rewardMoney = Money.rupiah(50_000_000L)
    ),
    OPERATE_FIRST_ROUTE(
        title = "Jalankan Dinas Perdana",
        description = "Tugaskan bus dan sopir pada trayek untuk mulai beroperasi",
        targetCount = 1,
        rewardMoney = Money.rupiah(100_000_000L)
    ),
    BOARD_PASSENGERS(
        title = "Angkut 50 Penumpang",
        description = "Kumpulkan minimal 50 penumpang naik di seluruh terminal",
        targetCount = 50,
        rewardMoney = Money.rupiah(150_000_000L)
    )
}

data class StarterQuestItem(
    val id: QuestId,
    val currentProgress: Int,
    val targetCount: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val rewardMoney: Money
) {
    val progressFraction: Float
        get() = (currentProgress.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
}
