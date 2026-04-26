package com.davekingdoms.pixelvolumehelper.data.model

import android.media.AudioManager

/**
 * Audio streams that the user can target for volume replacement.
 * Each entry maps to an [AudioManager] stream constant.
 */
enum class AudioStream(val streamType: Int, val label: String) {
    MUSIC(AudioManager.STREAM_MUSIC, "Music"),
    RING(AudioManager.STREAM_RING, "Ring"),
    NOTIFICATION(AudioManager.STREAM_NOTIFICATION, "Notification"),
    ALARM(AudioManager.STREAM_ALARM, "Alarm"),
    VOICE_CALL(AudioManager.STREAM_VOICE_CALL, "Voice Call"),
    SYSTEM(AudioManager.STREAM_SYSTEM, "System");

    companion object {
        fun fromKey(key: String): AudioStream =
            entries.firstOrNull { it.name == key } ?: MUSIC
    }
}

