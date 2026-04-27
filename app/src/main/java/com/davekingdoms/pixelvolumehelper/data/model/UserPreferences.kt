package com.davekingdoms.pixelvolumehelper.data.model

/**
 * Immutable snapshot of all user preferences.
 * Emitted as a single [kotlinx.coroutines.flow.Flow] from [PreferencesRepository].
 */
data class UserPreferences(
    val selectedStream: AudioStream = AudioStream.MUSIC,
    val overlayEnabled: Boolean = false,
    val overlayPosition: OverlayPosition = OverlayPosition.BOTTOM_RIGHT,
    val overlayX: Int = -1,
    val overlayY: Int = -1,
    val tapAction: OverlayAction = OverlayAction.OpenPanel,
    val longPressAction: OverlayAction = OverlayAction.Screenshot,
)

