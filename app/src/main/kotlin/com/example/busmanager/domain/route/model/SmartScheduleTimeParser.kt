package com.example.busmanager.domain.route.model

object SmartScheduleTimeParser {
    data class ParsedTime(
        val hour: Int,
        val minute: Int
    ) {
        val formattedTime: String
            get() = String.format("%02d:%02d WIB", hour, minute)

        val descriptionText: String
            get() {
                val displayHour12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                val period = when (hour) {
                    in 4..10 -> "Pagi"
                    in 11..14 -> "Siang"
                    in 15..18 -> "Sore"
                    else -> "Malam"
                }
                return if (minute == 0) {
                    "$formattedTime ($displayHour12.00 $period)"
                } else {
                    String.format("%s (%02d.%02d %s)", formattedTime, displayHour12, minute, period)
                }
            }
    }

    fun parse(input: String): ParsedTime? {
        val cleaned = input.trim().replace("\\s+".toRegex(), "")
        if (cleaned.isEmpty()) return null

        val parts = cleaned.split("[:.\\-]".toRegex()).filter { it.isNotEmpty() }
        val rawHour: Int
        val rawMinute: Int

        when (parts.size) {
            2 -> {
                rawHour = parts[0].toIntOrNull() ?: return null
                rawMinute = parts[1].toIntOrNull() ?: return null
                if (rawHour !in 0..24 || rawMinute !in 0..59) return null
            }
            1 -> {
                val str = parts[0]
                when (str.length) {
                    1, 2 -> {
                        rawHour = str.toIntOrNull() ?: return null
                        if (rawHour !in 0..24) return null
                        rawMinute = 0
                    }
                    3 -> {
                        rawHour = str.substring(0, 1).toIntOrNull() ?: return null
                        rawMinute = str.substring(1, 3).toIntOrNull() ?: return null
                        if (rawMinute !in 0..59) return null
                    }
                    4 -> {
                        rawHour = str.substring(0, 2).toIntOrNull() ?: return null
                        rawMinute = str.substring(2, 4).toIntOrNull() ?: return null
                        if (rawHour !in 0..24 || rawMinute !in 0..59) return null
                    }
                    else -> return null
                }
            }
            else -> return null
        }

        val adjustedHour = when (rawHour) {
            in 1..3 -> rawHour + 12
            else -> rawHour
        }

        val clampedHour = adjustedHour.coerceIn(4, 23)
        val clampedMinute = rawMinute.coerceIn(0, 59)

        return ParsedTime(clampedHour, clampedMinute)
    }
}
