package com.example

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

object GameSoundEffects {
    fun playKaching() {
        playSynthTones(listOf(880 to 100, 1320 to 180))
    }

    fun playCoin() {
        playSynthTones(listOf(1200 to 60, 1500 to 60, 1800 to 100))
    }

    fun playCardDraw() {
        playSynthTones(listOf(600 to 80, 800 to 80, 1000 to 150))
    }

    fun playError() {
        playSynthTones(listOf(220 to 120, 180 to 180))
    }

    fun playDiceRoll() {
        playSynthTones(listOf(1000 to 40, 800 to 40, 1200 to 40, 900 to 40))
    }

    private fun playSynthTones(tones: List<Pair<Int, Int>>) {
        Thread {
            try {
                val sampleRate = 8000
                // Calculate total duration in samples
                val totalDurationMs = tones.sumOf { it.second }
                val totalSamples = (sampleRate * totalDurationMs) / 1000
                val buffer = ShortArray(totalSamples)

                var currentSample = 0
                for (tone in tones) {
                    val freq = tone.first
                    val durMs = tone.second
                    val samplesForTone = (sampleRate * durMs) / 1000
                    for (i in 0 until samplesForTone) {
                        if (currentSample >= totalSamples) break
                        // Generate a clean sine wave
                        val angle = 2.0 * Math.PI * i / (sampleRate.toDouble() / freq)
                        // Add linear fade-out at the end of each tone to avoid clicks
                        val fade = if (i > samplesForTone - 120) {
                            (samplesForTone - i).toFloat() / 120f
                        } else 1.0f
                        buffer[currentSample] = (Math.sin(angle) * Short.MAX_VALUE * 0.35f * fade).toInt().toShort()
                        currentSample++
                    }
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC
                )
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                // Wait for playback to complete, then release
                Thread.sleep(totalDurationMs.toLong() + 50)
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
