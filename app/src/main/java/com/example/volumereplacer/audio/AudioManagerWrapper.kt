package com.example.volumereplacer.audio

import android.content.Context

/**
 * Wrapper around the system [android.media.AudioManager].
 * Centralises volume queries and mutations.
 */
class AudioManagerWrapper(private val context: Context) {

    private val systemAudioManager: android.media.AudioManager
        get() = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    // TODO: getVolume, setVolume, stream helpers
}

