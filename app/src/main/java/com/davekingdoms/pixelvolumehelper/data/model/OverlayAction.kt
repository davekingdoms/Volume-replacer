package com.davekingdoms.pixelvolumehelper.data.model

/**
 * Actions that can be triggered by tapping or long-pressing the overlay button.
 */
sealed class OverlayAction(val key: String, val label: String) {
    data object VolumeUp : OverlayAction("VOLUME_UP", "Volume Up")
    data object VolumeDown : OverlayAction("VOLUME_DOWN", "Volume Down")
    data object Mute : OverlayAction("MUTE", "Mute / Unmute")
    data object OpenPanel : OverlayAction("OPEN_PANEL", "Open Volume Panel")
    data object Screenshot : OverlayAction("SCREENSHOT", "Screenshot")
    data object CycleProfile : OverlayAction("CYCLE_PROFILE", "Cycle Profile")
    data object None : OverlayAction("NONE", "Do Nothing")

    companion object {
        private val all: List<OverlayAction> = listOf(
            VolumeUp, VolumeDown, Mute, OpenPanel, Screenshot, CycleProfile, None,
        )

        fun fromKey(key: String): OverlayAction =
            all.firstOrNull { it.key == key } ?: OpenPanel

        fun entries(): List<OverlayAction> = all
    }
}

