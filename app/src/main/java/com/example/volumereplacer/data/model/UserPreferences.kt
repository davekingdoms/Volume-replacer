package com.example.volumereplacer.data.model

/**
 * Immutable snapshot of all user preferences.
 * Emitted as a single [kotlinx.coroutines.flow.Flow] from [PreferencesRepository].
 */
data class UserPreferences(
    val selectedStream: AudioStream = AudioStream.MUSIC,
    val overlayEnabled: Boolean = false,
    val overlayPosition: OverlayPosition = OverlayPosition.BOTTOM_RIGHT,
    val tapAction: OverlayAction = OverlayAction.VolumeUp,
    val longPressAction: OverlayAction = OverlayAction.Mute,
)

