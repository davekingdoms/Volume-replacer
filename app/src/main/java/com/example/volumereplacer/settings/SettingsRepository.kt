package com.example.volumereplacer.settings

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/**
 * Single-instance DataStore delegate.
 * Access via `context.settingsDataStore`.
 */
val Context.settingsDataStore by preferencesDataStore(name = "settings")

/**
 * Repository that reads / writes user preferences.
 * Implementation will be added later.
 */
class SettingsRepository(private val context: Context) {
    // TODO: expose Flows for each preference key
}

