package com.example.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object OdooAudioPlayer {

    fun play(context: Context, soundName: String) {
        if (soundName.equals("None", ignoreCase = true)) return

        CoroutineScope(Dispatchers.Default).launch {
            val sampleRate = 44100
            val durationSeconds = 0.5
            val numSamples = (durationSeconds * sampleRate).toInt()
            val sample = ByteArray(numSamples)

            when (soundName.lowercase()) {
                "chirp" -> {
                    // Ascending frequency sweep from 600Hz to 1600Hz
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val freq = 600.0 + (1600.0 - 600.0) * (t / durationSeconds)
                        val angle = 2.0 * Math.PI * freq * t
                        val value = (sin(angle) * 127).toInt()
                        sample[i] = value.toByte()
                    }
                }
                "bell" -> {
                    // Sweet high-frequency ring with exponent status decay
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val freq = 1500.0
                        val angle = 2.0 * Math.PI * freq * t
                        val decay = Math.pow(0.01, t / durationSeconds)
                        val value = (sin(angle) * decay * 127).toInt()
                        sample[i] = value.toByte()
                    }
                }
                "piano" -> {
                    // Chord resonance
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val angle1 = 2.0 * Math.PI * 523.25 * t // C5
                        val angle2 = 2.0 * Math.PI * 659.25 * t // E5
                        val angle3 = 2.0 * Math.PI * 783.99 * t // G5
                        val mixed = (sin(angle1) + sin(angle2) + sin(angle3)) / 3.0
                        val decay = Math.pow(0.05, t / durationSeconds)
                        val value = (mixed * decay * 127).toInt()
                        sample[i] = value.toByte()
                    }
                }
                else -> return@launch
            }

            try {
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT
                )

                val bufferSize = if (minBufSize > numSamples) minBufSize else numSamples
                
                val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_NOTIFICATION,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_8BIT,
                        bufferSize,
                        AudioTrack.MODE_STATIC
                    )
                }

                audioTrack.write(sample, 0, sample.size)
                audioTrack.play()
                
                kotlinx.coroutines.delay((durationSeconds * 1000).toLong() + 200)
                
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Ignore
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
