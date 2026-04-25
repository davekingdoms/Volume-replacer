package com.example.volumereplacer.audio

import android.content.Context
import android.media.AudioManager
import com.example.volumereplacer.data.model.AudioStream

/**
 * Thin wrapper that exposes only the [AudioManager] queries the app needs.
 * Keeps the system service access in one place so it can be swapped in tests.
 */
class AudioManagerWrapper(private val context: Context) {

    private val audioManager: AudioManager
        get() = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /** Current volume for [stream] (0 … [getMaxVolume]). */
    fun getVolume(stream: AudioStream): Int =
        audioManager.getStreamVolume(stream.streamType)

    /** Maximum volume index for [stream]. */
    fun getMaxVolume(stream: AudioStream): Int =
        audioManager.getStreamMaxVolume(stream.streamType)

    /** Minimum volume index for [stream] (API 28+). */
    fun getMinVolume(stream: AudioStream): Int =
        audioManager.getStreamMinVolume(stream.streamType)

    /** Set volume for [stream] to an absolute [level]. */
    fun setVolume(stream: AudioStream, level: Int, flags: Int = 0) {
        audioManager.setStreamVolume(stream.streamType, level, flags)
    }

    /** Raise volume by one step. */
    fun raiseVolume(stream: AudioStream, flags: Int = 0) {
        audioManager.adjustStreamVolume(stream.streamType, AudioManager.ADJUST_RAISE, flags)
    }

    /** Lower volume by one step. */
    fun lowerVolume(stream: AudioStream, flags: Int = 0) {
        audioManager.adjustStreamVolume(stream.streamType, AudioManager.ADJUST_LOWER, flags)
    }

    /** Current ringer mode (NORMAL / VIBRATE / SILENT). */
    fun getRingerMode(): Int = audioManager.ringerMode

    /** Set ringer mode (requires notification-policy access for DND). */
    fun setRingerMode(mode: Int) {
        audioManager.ringerMode = mode
    }
}
