package com.davekingdoms.pixelvolumehelper.data.model

/**
 * Where the floating overlay button is anchored on screen.
 */
enum class OverlayPosition(val label: String) {
    TOP_LEFT("Top Left"),
    TOP_RIGHT("Top Right"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_RIGHT("Bottom Right"),
    CENTER_LEFT("Center Left"),
    CENTER_RIGHT("Center Right");

    companion object {
        fun fromKey(key: String): OverlayPosition =
            entries.firstOrNull { it.name == key } ?: BOTTOM_RIGHT
    }
}

