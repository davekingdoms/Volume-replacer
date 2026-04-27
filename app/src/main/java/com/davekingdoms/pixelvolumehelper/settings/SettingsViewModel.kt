package com.davekingdoms.pixelvolumehelper.settings

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.davekingdoms.pixelvolumehelper.accessibility.VolumeAccessibilityService
import com.davekingdoms.pixelvolumehelper.data.PreferencesRepository
import com.davekingdoms.pixelvolumehelper.data.model.AudioStream
import com.davekingdoms.pixelvolumehelper.data.model.OverlayAction
import com.davekingdoms.pixelvolumehelper.data.model.OverlayPosition
import com.davekingdoms.pixelvolumehelper.data.model.UserPreferences
import com.davekingdoms.pixelvolumehelper.overlay.OverlayManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds the permission/access status that is refreshed each time the screen resumes.
 */
data class PermissionState(
    val hasOverlayPermission: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val hasNotificationPolicyAccess: Boolean = false,
    val isIgnoringBatteryOptimizations: Boolean = false,
)

/**
 * ViewModel for the Settings screen.
 *
 * Reads [UserPreferences] from [PreferencesRepository] and exposes permission state
 * that can be refreshed on each resume.
 */
class SettingsViewModel(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    /** Current user preferences as a hot state flow. */
    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState

    init {
        refreshPermissions()
    }

    // ── Permission checks ───────────────────────────────────────────────

    fun refreshPermissions() {
        _permissionState.value = PermissionState(
            hasOverlayPermission = Settings.canDrawOverlays(context),
            isAccessibilityEnabled = isAccessibilityServiceEnabled(),
            hasNotificationPolicyAccess = isNotificationPolicyAccessGranted(),
            isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations(),
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val componentName = ComponentName(context, VolumeAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabledServices.split(':').any {
            ComponentName.unflattenFromString(it) == componentName
        }
    }

    private fun isNotificationPolicyAccessGranted(): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    // ── System settings intents ─────────────────────────────────────────

    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openNotificationPolicySettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openBatteryOptimizationSettings() {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // ── Preference setters ──────────────────────────────────────────────

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOverlayEnabled(enabled)
            if (enabled) {
                OverlayManager.start(context)
            } else {
                OverlayManager.stop(context)
            }
        }
    }

    fun setSelectedStream(stream: AudioStream) {
        viewModelScope.launch { preferencesRepository.setSelectedStream(stream) }
    }

    fun setTapAction(action: OverlayAction) {
        viewModelScope.launch { preferencesRepository.setTapAction(action) }
    }

    fun setLongPressAction(action: OverlayAction) {
        viewModelScope.launch { preferencesRepository.setLongPressAction(action) }
    }

    fun setOverlayPosition(position: OverlayPosition) {
        viewModelScope.launch { preferencesRepository.setOverlayPosition(position) }
    }

    // ── Debug actions ───────────────────────────────────────────────────

    fun openVolumePanel() {
        val intent = Intent(Settings.Panel.ACTION_VOLUME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun triggerScreenshot() {
        VolumeAccessibilityService.instance?.takeScreenshot()
    }

    // ── Factory ─────────────────────────────────────────────────────────

    class Factory(
        private val context: Context,
        private val preferencesRepository: PreferencesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(context.applicationContext, preferencesRepository) as T
        }
    }
}
