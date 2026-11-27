package com.example.busmanager.presentation.route.wizard

enum class SlotTimeFilter(val label: String, val startHour: Int, val endHour: Int) {
    ALL("Semua", 0, 23),
    DAWN("Subuh (00-06)", 0, 5),
    MORNING("Pagi (06-12)", 6, 11),
    AFTERNOON("Siang (12-18)", 12, 17),
    NIGHT("Malam (18-24)", 18, 23)
}
