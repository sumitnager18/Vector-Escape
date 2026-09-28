package com.vector.escape.storage

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.max

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vector_escape_prefs", Context.MODE_PRIVATE)

    var unlockedLevel: Int
        get() = prefs.getInt("unlocked_level", 1)
        set(value) = prefs.edit().putInt("unlocked_level", value).apply()

    var currentCampaignLevel: Int
        get() = prefs.getInt("current_level", 1)
        set(value) = prefs.edit().putInt("current_level", value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("haptics_enabled", value).apply()

    var reducedMotion: Boolean
        get() = prefs.getBoolean("reduced_motion", false)
        set(value) = prefs.edit().putBoolean("reduced_motion", value).apply()

    fun getLevelStars(levelNumber: Int): Int {
        return prefs.getInt("stars_lvl_\$levelNumber", 0)
    }

    fun saveLevelStars(levelNumber: Int, stars: Int) {
        val prev = getLevelStars(levelNumber)
        if (stars > prev) {
            prefs.edit().putInt("stars_lvl_\$levelNumber", stars).apply()
        }
        if (levelNumber >= unlockedLevel && levelNumber < 50) {
            unlockedLevel = levelNumber + 1
        }
        currentCampaignLevel = maxOf(currentCampaignLevel, (levelNumber + 1).coerceAtMost(50))
    }

    fun getTotalStars(): Int {
        var sum = 0
        for (i in 1..50) {
            sum += getLevelStars(i)
        }
        return sum
    }

    fun isDailyCompleted(dateStr: String): Boolean {
        return prefs.getBoolean("daily_\$dateStr", false)
    }

    fun recordDailyCompleted(dateStr: String, stars: Int) {
        prefs.edit().putBoolean("daily_\$dateStr", true).putInt("daily_stars_\$dateStr", stars).apply()
    }

    fun resetAll() {
        val s = soundEnabled
        val h = hapticsEnabled
        prefs.edit().clear().apply()
        soundEnabled = s
        hapticsEnabled = h
    }
}
