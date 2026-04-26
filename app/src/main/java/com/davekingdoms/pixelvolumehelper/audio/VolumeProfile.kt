package com.davekingdoms.pixelvolumehelper.audio

/**
 * Data model representing a saved volume profile.
 *
 * @property name   User-visible label.
 * @property levels Map of AudioManager stream type to volume level (0-max).
 */
data class VolumeProfile(
    val name: String = "",
    val levels: Map<Int, Int> = emptyMap(),
)

