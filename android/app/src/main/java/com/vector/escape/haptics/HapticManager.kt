package com.vector.escape.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticManager {
    var enabled: Boolean = true

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun tap(context: Context) {
        if (!enabled) return
        val v = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(12)
        }
    }

    fun blocked(context: Context) {
        if (!enabled) return
        val v = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 30, 50), -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 40, 30, 50), -1)
        }
    }

    fun success(context: Context) {
        if (!enabled) return
        val v = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 30, 60, 40, 90), -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 30, 30, 60, 40, 90), -1)
        }
    }
}
