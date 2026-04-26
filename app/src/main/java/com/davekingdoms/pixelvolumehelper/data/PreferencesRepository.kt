package com.davekingdoms.pixelvolumehelper.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.davekingdoms.pixelvolumehelper.data.model.AudioStream
import com.davekingdoms.pixelvolumehelper.data.model.OverlayAction
import com.davekingdoms.pixelvolumehelper.data.model.OverlayPosition
import com.davekingdoms.pixelvolumehelper.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Single DataStore instance scoped to the application process. */
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

/**
 * Reads and writes user preferences backed by Jetpack DataStore.
 *
 * Every setter is a `suspend` function so callers can react to completion;
 * [userPreferencesFlow] exposes the current state as a cold [Flow].
 */
class PreferencesRepository(private val context: Context) {

    /* ---- keys ---- */
    private object Keys {
        val SELECTED_STREAM = stringPreferencesKey("selected_stream")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val OVERLAY_POSITION = stringPreferencesKey("overlay_position")
        val OVERLAY_X = intPreferencesKey("overlay_x")
        val OVERLAY_Y = intPreferencesKey("overlay_y")
        val TAP_ACTION = stringPreferencesKey("tap_action")
        val LONG_PRESS_ACTION = stringPreferencesKey("long_press_action")
    }

    /* ---- read ---- */

    /** Emits a new [UserPreferences] snapshot whenever any value changes. */
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            selectedStream = AudioStream.fromKey(
                prefs[Keys.SELECTED_STREAM] ?: AudioStream.MUSIC.name,
            ),
            overlayEnabled = prefs[Keys.OVERLAY_ENABLED] ?: false,
            overlayPosition = OverlayPosition.fromKey(
                prefs[Keys.OVERLAY_POSITION] ?: OverlayPosition.BOTTOM_RIGHT.name,
            ),
            overlayX = prefs[Keys.OVERLAY_X] ?: -1,
            overlayY = prefs[Keys.OVERLAY_Y] ?: -1,
            tapAction = OverlayAction.fromKey(
                prefs[Keys.TAP_ACTION] ?: OverlayAction.OpenPanel.key,
            ),
            longPressAction = OverlayAction.fromKey(
                prefs[Keys.LONG_PRESS_ACTION] ?: OverlayAction.Screenshot.key,
            ),
        )
    }

    /* ---- write ---- */

    suspend fun setSelectedStream(stream: AudioStream) {
        context.dataStore.edit { it[Keys.SELECTED_STREAM] = stream.name }
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OVERLAY_ENABLED] = enabled }
    }

    suspend fun setOverlayPosition(position: OverlayPosition) {
        context.dataStore.edit { it[Keys.OVERLAY_POSITION] = position.name }
    }

    suspend fun setOverlayXY(x: Int, y: Int) {
        context.dataStore.edit {
            it[Keys.OVERLAY_X] = x
            it[Keys.OVERLAY_Y] = y
        }
    }

    suspend fun setTapAction(action: OverlayAction) {
        context.dataStore.edit { it[Keys.TAP_ACTION] = action.key }
    }

    suspend fun setLongPressAction(action: OverlayAction) {
        context.dataStore.edit { it[Keys.LONG_PRESS_ACTION] = action.key }
    }
}

