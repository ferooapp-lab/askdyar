package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecordHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordFile: File? = null

    var onPlaybackCompleted: (() -> Unit)? = null

    fun startRecording(): String? {
        val audioDir = File(context.filesDir, "audio_notes")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        val file = File(audioDir, "love_voice_${System.currentTimeMillis()}.m4a")
        currentRecordFile = file

        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder = recorder
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()
            file.absolutePath
        } catch (e: Exception) {
            Log.e("AudioRecordHelper", "Failed to start recording: ${e.message}", e)
            mediaRecorder?.release()
            mediaRecorder = null
            null
        }
    }

    fun stopRecording(): String? {
        return try {
            mediaRecorder?.let {
                it.stop()
                it.release()
            }
            mediaRecorder = null
            currentRecordFile?.absolutePath
        } catch (e: Exception) {
            Log.e("AudioRecordHelper", "Failed to stop recording: ${e.message}", e)
            mediaRecorder?.release()
            mediaRecorder = null
            null
        }
    }

    fun startPlaying(path: String, onCompletion: () -> Unit) {
        stopPlaying()
        val file = File(path)
        if (!file.exists()) {
            onCompletion()
            return
        }
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    stopPlaying()
                    onCompletion()
                }
            }
        } catch (e: IOException) {
            Log.e("AudioRecordHelper", "Failed to play audio: ${e.message}", e)
            onCompletion()
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecordHelper", "Failed to stop playback: ${e.message}", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun isCurrentlyPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    fun cleanUp() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // ignore
        }
    }
}
