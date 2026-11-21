package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.sin

data class BuiltInAmbientPreset(
    val key: String,
    val title: String,
    val subtitle: String,
    val emoji: String
)

object BuiltInAmbientPresets {
    val presets = listOf(
        BuiltInAmbientPreset(
            key = "preset_music_box",
            title = "Romantik Müzik Kutusu",
            subtitle = "Yumuşak çan ve piyano akorları",
            emoji = "🎶"
        ),
        BuiltInAmbientPreset(
            key = "preset_piano_dream",
            title = "Gece Piyano Rüyası",
            subtitle = "Sakinleştirici gece romantizmi",
            emoji = "🎹"
        ),
        BuiltInAmbientPreset(
            key = "preset_starlight",
            title = "Yıldızlı Gece Ninnisi",
            subtitle = "Huzurlu kalp ritmi melodisi",
            emoji = "🌙"
        )
    )
}

class AmbientSoundHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying: Boolean = false
        private set
    var currentVolume: Float = 0.7f
        private set

    fun toggleAmbientPlayback(onStateChanged: (Boolean) -> Unit) {
        if (isPlaying) {
            stopAmbient()
            onStateChanged(false)
        } else {
            val started = startPreset("preset_music_box")
            onStateChanged(started)
        }
    }

    fun startAmbient(): Boolean = startPreset("preset_music_box")

    fun startPreset(presetKey: String): Boolean {
        return try {
            val wavFile = getOrCreatePresetWavFile(presetKey)
            startAudioFile(wavFile.absolutePath)
        } catch (e: Exception) {
            Log.e("AmbientSoundHelper", "Failed to start preset $presetKey: ${e.message}", e)
            isPlaying = false
            false
        }
    }

    fun startAudioFile(filePath: String): Boolean {
        return try {
            val targetFile = File(filePath)
            if (!targetFile.exists() || targetFile.length() == 0L) {
                Log.w("AmbientSoundHelper", "Audio file missing or empty: $filePath")
                isPlaying = false
                return false
            }
            stopAmbient()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(targetFile.absolutePath)
                isLooping = true
                setVolume(currentVolume, currentVolume)
                prepare()
                start()
            }
            isPlaying = true
            true
        } catch (e: Exception) {
            Log.e("AmbientSoundHelper", "Failed to play custom audio $filePath: ${e.message}", e)
            isPlaying = false
            false
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        } catch (e: Exception) {
            Log.w("AmbientSoundHelper", "Failed to adjust volume: ${e.message}")
        }
    }

    fun stopAmbient() {
        isPlaying = false
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("AmbientSoundHelper", "Error stopping ambient: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    /**
     * Imports user-selected audio from SAF Uri into internal filesDir without requiring storage permissions.
     * Returns Pair(savedFilePath, suggestedTrackTitle).
     */
    fun importAudioUri(uri: Uri): Result<Pair<String, String>> {
        return try {
            val resolver = context.contentResolver
            var displayName = "Romantik Melodim"
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val raw = cursor.getString(idx)
                        if (!raw.isNullOrBlank()) {
                            displayName = raw.substringBeforeLast('.')
                        }
                    }
                }
            }

            val musicDir = File(context.filesDir, "custom_music").apply { mkdirs() }
            val destFile = File(musicDir, "track_${System.currentTimeMillis()}.audio")

            val inputStream = resolver.openInputStream(uri)
                ?: return Result.failure(Exception("Ses dosyası okunamadı."))
            inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.length() == 0L) {
                destFile.delete()
                return Result.failure(Exception("Seçilen ses dosyası boş görünüyor."))
            }

            Result.success(destFile.absolutePath to displayName)
        } catch (e: Exception) {
            Log.e("AmbientSoundHelper", "Audio import failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun getOrCreatePresetWavFile(presetKey: String): File {
        val safeName = when (presetKey) {
            "preset_piano_dream" -> "romantic_piano_dream.wav"
            "preset_starlight" -> "romantic_starlight_lullaby.wav"
            else -> "romantic_ambient_melody.wav"
        }
        val file = File(context.cacheDir, safeName)
        if (file.exists() && file.length() > 1000) {
            return file
        }

        val sampleRate = 22050
        val durationSeconds = 6.0
        val numSamples = (sampleRate * durationSeconds).toInt()

        data class Note(val freq: Double, val startTime: Double, val duration: Double)

        val notes = when (presetKey) {
            "preset_piano_dream" -> listOf(
                Note(220.00, 0.0, 2.0), // A3
                Note(261.63, 0.7, 1.8), // C4
                Note(329.63, 1.4, 2.0), // E4
                Note(392.00, 2.2, 2.0), // G4
                Note(440.00, 3.0, 2.0), // A4
                Note(392.00, 4.0, 1.8), // G4
                Note(329.63, 4.8, 1.2)  // E4
            )
            "preset_starlight" -> listOf(
                Note(196.00, 0.0, 2.2), // G3
                Note(246.94, 0.8, 1.8), // B3
                Note(293.66, 1.6, 2.0), // D4
                Note(392.00, 2.4, 2.2), // G4
                Note(369.99, 3.4, 1.8), // F#4
                Note(293.66, 4.4, 1.6)  // D4
            )
            else -> listOf(
                Note(261.63, 0.0, 1.8), // C4
                Note(329.63, 0.5, 1.6), // E4
                Note(392.00, 1.0, 1.8), // G4
                Note(493.88, 1.5, 1.8), // B4
                Note(523.25, 2.2, 2.0), // C5
                Note(587.33, 3.0, 1.8), // D5
                Note(659.25, 3.8, 2.0), // E5
                Note(523.25, 4.6, 1.4)  // C5
            )
        }

        val samples = DoubleArray(numSamples)
        val decay = if (presetKey == "preset_piano_dream") 1.7 else 2.2

        for (note in notes) {
            val startSample = (note.startTime * sampleRate).toInt()
            val noteSampleCount = (note.duration * sampleRate).toInt()
            val endSample = (startSample + noteSampleCount).coerceAtMost(numSamples)

            for (i in startSample until endSample) {
                val t = (i - startSample).toDouble() / sampleRate
                val fundamental = sin(2.0 * Math.PI * note.freq * t)
                val overtone1 = 0.32 * sin(2.0 * Math.PI * (note.freq * 2.0) * t)
                val overtone2 = 0.12 * sin(2.0 * Math.PI * (note.freq * 3.0) * t)
                val envelope = exp(-decay * t)
                samples[i] += (fundamental + overtone1 + overtone2) * envelope * 0.35
            }
        }

        val byteBuffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in samples) {
            val shortVal = (sample.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
            byteBuffer.putShort(shortVal)
        }
        val pcmData = byteBuffer.array()

        FileOutputStream(file).use { out ->
            out.write("RIFF".toByteArray())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(36 + pcmData.size).array())
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array())
            out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array())
            out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate).array())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate * 2).array())
            out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(2).array())
            out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(16).array())
            out.write("data".toByteArray())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(pcmData.size).array())
            out.write(pcmData)
        }

        return file
    }
}
