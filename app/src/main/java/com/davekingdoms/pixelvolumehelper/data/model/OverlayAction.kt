package com.davekingdoms.pixelvolumehelper.data.model

/**
 * Actions that can be triggered by tapping or long-pressing the overlay button.
 *
 * Implemented as an `enum class` instead of a sealed class so that JVM guarantees
 * complete initialization before any instance is accessed – avoiding the NPE that
 * can occur when `data object` members of a sealed class are read before the class
 * initializer has finished running on the ART runtime.
 */
enum class OverlayAction(val key: String, val label: String) {
    VolumeUp("VOLUME_UP", "Volume Up"),
    VolumeDown("VOLUME_DOWN", "Volume Down"),
    Mute("MUTE", "Mute / Unmute"),
    OpenPanel("OPEN_PANEL", "Open Volume Panel"),
    Screenshot("SCREENSHOT", "Screenshot"),
    CycleProfile("CYCLE_PROFILE", "Cycle Profile"),
    None("NONE", "Do Nothing");

    companion object {
        /** Returns the entry matching [key], or [OpenPanel] as a safe default. */
        fun fromKey(key: String): OverlayAction =
            entries.firstOrNull { it.key == key } ?: OpenPanel

        /** All available actions as an ordered list (mirrors [entries]). */
        fun all(): List<OverlayAction> = entries
    }
}
