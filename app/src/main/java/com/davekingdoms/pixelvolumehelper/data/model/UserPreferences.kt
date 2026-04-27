package com.davekingdoms.pixelvolumehelper.data.model

/**
 * Immutable snapshot of all user preferences.
 * Emitted as a single [kotlinx.coroutines.flow.Flow] from [PreferencesRepository].
 *
 * Overlay position is represented solely by [overlayX]/[overlayY], which are
 * persisted whenever the user drags the overlay button. Negative values mean
 * "no position saved yet" and the runtime will fall back to a sensible default.
 */
data class UserPreferences(
    val selectedStream: AudioStream = AudioStream.MUSIC,
    val overlayEnabled: Boolean = false,
    val overlayX: Int = -1,
    val overlayY: Int = -1,
    val tapAction: OverlayAction = OverlayAction.OpenPanel,
    val longPressAction: OverlayAction = OverlayAction.Screenshot,
)

