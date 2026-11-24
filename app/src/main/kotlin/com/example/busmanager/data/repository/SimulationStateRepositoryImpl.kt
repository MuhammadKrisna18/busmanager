package com.example.busmanager.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.SavedSimulationState
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.repository.SimulationStateRepository

class SimulationStateRepositoryImpl(
    context: Context,
    prefsName: String = "simulation_state_prefs"
) : SimulationStateRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WALL_CLOCK_MILLIS = "key_wall_clock_millis"
        private const val KEY_IS_PLAYING = "key_is_playing"
        private const val KEY_SPEED = "key_speed"
        private const val KEY_YEAR = "key_year"
        private const val KEY_MONTH = "key_month"
        private const val KEY_DAY = "key_day"
        private const val KEY_HOUR = "key_hour"
        private const val KEY_MINUTE = "key_minute"
    }

    override fun saveState(state: SavedSimulationState) {
        prefs.edit()
            .putLong(KEY_WALL_CLOCK_MILLIS, state.lastWallClockMillis)
            .putBoolean(KEY_IS_PLAYING, state.isPlaying)
            .putString(KEY_SPEED, state.speed.name)
            .putInt(KEY_YEAR, state.gameCalendar.year)
            .putInt(KEY_MONTH, state.gameCalendar.month)
            .putInt(KEY_DAY, state.gameCalendar.day)
            .putInt(KEY_HOUR, state.gameCalendar.hour)
            .putInt(KEY_MINUTE, state.gameCalendar.minute)
            .apply()
    }

    override fun loadState(): SavedSimulationState? {
        val wallClockMillis = prefs.getLong(KEY_WALL_CLOCK_MILLIS, -1L)
        if (wallClockMillis <= 0L) return null

        val isPlaying = prefs.getBoolean(KEY_IS_PLAYING, false)
        val speedName = prefs.getString(KEY_SPEED, SimulationSpeed.NORMAL.name) ?: SimulationSpeed.NORMAL.name
        val speed = try {
            SimulationSpeed.valueOf(speedName)
        } catch (e: Exception) {
            SimulationSpeed.NORMAL
        }

        val year = prefs.getInt(KEY_YEAR, 2026)
        val month = prefs.getInt(KEY_MONTH, 11)
        val day = prefs.getInt(KEY_DAY, 24)
        val hour = prefs.getInt(KEY_HOUR, 8)
        val minute = prefs.getInt(KEY_MINUTE, 0)

        return SavedSimulationState(
            lastWallClockMillis = wallClockMillis,
            isPlaying = isPlaying,
            speed = speed,
            gameCalendar = GameCalendar(
                year = year,
                month = month,
                day = day,
                hour = hour,
                minute = minute
            )
        )
    }

    override fun clearState() {
        prefs.edit().clear().apply()
    }
}
