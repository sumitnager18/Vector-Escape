package com.vector.escape.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.sin

object SoundManager {
    var enabled: Boolean = true

    fun playTap() {
        if (!enabled) return
        generateTone(800.0, 400.0, 0.04, 0.2)
    }

    fun playLaunch(multiplier: Int = 1) {
        if (!enabled) return
        val startFreq = 380.0 + (multiplier - 1) * 50.0
        generateTone(startFreq, startFreq * 1.6, 0.22, 0.3)
    }

    fun playBlocked() {
        if (!enabled) return
        generateTone(140.0, 120.0, 0.12, 0.25)
    }

    fun playWin() {
        if (!enabled) return
        thread {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.5)
            for (note in notes) {
                generateTone(note, note, 0.18, 0.25)
                Thread.sleep(70)
            }
        }
    }

    private fun generateTone(startFreq: Double, endFreq: Double, durationSec: Double, volume: Double) {
        thread {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * durationSec).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = startFreq + (endFreq - startFreq) * progress
                    val envelope = 1.0 - progress
                    val sample = sin(2.0 * Math.PI * i * freq / sampleRate) * envelope
                    buffer[i] = (sample * Short.MAX_VALUE * volume).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
            } catch (_: Exception) {}
        }
    }
}
