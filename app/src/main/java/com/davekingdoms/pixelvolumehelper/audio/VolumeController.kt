package com.davekingdoms.pixelvolumehelper.audio

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import com.davekingdoms.pixelvolumehelper.data.model.AudioStream

/**
 * High-level volume façade consumed by UI and overlay layers.
 *
 * Delegates to [AudioManagerWrapper] for stream control and uses
 * [NotificationManager] for ringer-mode cycling and DND policy checks.
 *
 * **Do not** access [AudioManager] directly from UI code — use this class.
 */
class VolumeController(private val context: Context) {

    private val wrapper = AudioManagerWrapper(context)

    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // ── Volume queries ──────────────────────────────────────────────────

    /** Current volume (0 … max) for [stream]. */
    fun getVolume(stream: AudioStream): Int = wrapper.getVolume(stream)

    /** Maximum volume index for [stream]. */
    fun getMaxVolume(stream: AudioStream): Int = wrapper.getMaxVolume(stream)

    /** Minimum volume index for [stream]. */
    fun getMinVolume(stream: AudioStream): Int = wrapper.getMinVolume(stream)

    /** Volume as a normalised 0f–1f fraction. */
    fun getVolumeFraction(stream: AudioStream): Float {
        val max = wrapper.getMaxVolume(stream)
        return if (max == 0) 0f else wrapper.getVolume(stream).toFloat() / max
    }

    // ── Volume mutations ────────────────────────────────────────────────

    /** Set absolute volume [level] for [stream]. */
    fun setVolume(stream: AudioStream, level: Int, showUi: Boolean = false) {
        val flags = if (showUi) AudioManager.FLAG_SHOW_UI else 0
        wrapper.setVolume(stream, level.coerceIn(wrapper.getMinVolume(stream), wrapper.getMaxVolume(stream)), flags)
    }

    /** Increase volume by one step. */
    fun increaseVolume(stream: AudioStream, showUi: Boolean = false) {
        val flags = if (showUi) AudioManager.FLAG_SHOW_UI else 0
        wrapper.raiseVolume(stream, flags)
    }

    /** Decrease volume by one step. */
    fun decreaseVolume(stream: AudioStream, showUi: Boolean = false) {
        val flags = if (showUi) AudioManager.FLAG_SHOW_UI else 0
        wrapper.lowerVolume(stream, flags)
    }

    /** Toggle mute on [stream]. */
    fun toggleMute(stream: AudioStream, showUi: Boolean = false) {
        val flags = if (showUi) AudioManager.FLAG_SHOW_UI else 0
        wrapper.toggleMute(stream, flags)
    }

    // ── Ringer mode ─────────────────────────────────────────────────────

    /**
     * Cycle through ringer modes: NORMAL → VIBRATE → SILENT → NORMAL.
     *
     * Switching to [AudioManager.RINGER_MODE_SILENT] requires notification-
     * policy access on API 23+; call [hasNotificationPolicyAccess] first.
     */
    fun cycleRingerMode() {
        val next = when (wrapper.getRingerMode()) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        wrapper.setRingerMode(next)
    }

    /**
     * Set the ringer mode directly (NORMAL / VIBRATE / SILENT).
     *
     * Switching to [AudioManager.RINGER_MODE_SILENT] requires notification-
     * policy access on API 23+; call [hasNotificationPolicyAccess] first.
     */
    fun setRingerMode(mode: Int) {
        wrapper.setRingerMode(mode)
    }

    /** Current ringer mode constant ([AudioManager.RINGER_MODE_NORMAL], …). */
    fun getRingerMode(): Int = wrapper.getRingerMode()

    // ── Notification policy (DND) ───────────────────────────────────────

    /**
     * `true` when the app is allowed to modify Do-Not-Disturb / ringer-silent.
     * Always `true` below API 23.
     */
    fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Opens the system settings screen where the user can grant
     * notification-policy access.
     */
    fun requestNotificationPolicyAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}



