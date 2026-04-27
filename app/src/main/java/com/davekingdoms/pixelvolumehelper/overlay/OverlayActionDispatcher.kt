package com.davekingdoms.pixelvolumehelper.overlay

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.davekingdoms.pixelvolumehelper.accessibility.VolumeAccessibilityService
import com.davekingdoms.pixelvolumehelper.audio.VolumeController
import com.davekingdoms.pixelvolumehelper.data.model.AudioStream
import com.davekingdoms.pixelvolumehelper.data.model.OverlayAction

/**
 * Dispatches an [OverlayAction] to its concrete implementation.
 *
 * The overlay service should read the user's configured tap / long-press
 * action and forward it here, instead of hard-coding the behavior in raw
 * touch handlers. Each action is executed inside [runCatching] so that an
 * unavailable backend (e.g. accessibility service not connected, missing
 * notification-policy permission) never crashes the overlay.
 *
 * Per-action operations are injected as lambdas so this class can be
 * unit-tested without any Android dependencies.
 */
class OverlayActionDispatcher(
    private val openPanel: () -> Unit,
    private val takeScreenshot: () -> Unit,
    private val volumeUp: () -> Unit,
    private val volumeDown: () -> Unit,
    private val toggleMute: () -> Unit,
    private val cycleProfile: () -> Unit,
    private val onError: (OverlayAction, Throwable) -> Unit = { action, t ->
        Log.w(TAG, "Action ${action.key} failed", t)
    },
) {

    /** Execute [action]. Failures are logged via [onError] and swallowed. */
    fun dispatch(action: OverlayAction) {
        runCatching {
            when (action) {
                OverlayAction.OpenPanel -> openPanel()
                OverlayAction.Screenshot -> takeScreenshot()
                OverlayAction.VolumeUp -> volumeUp()
                OverlayAction.VolumeDown -> volumeDown()
                OverlayAction.Mute -> toggleMute()
                OverlayAction.CycleProfile -> cycleProfile()
                OverlayAction.None -> Unit
            }
        }.onFailure { onError(action, it) }
    }

    companion object {
        private const val TAG = "OverlayDispatcher"

        /**
         * Build a dispatcher backed by the real Android subsystems used by
         * the running overlay service: [VolumeController], the volume
         * settings panel intent, and the [VolumeAccessibilityService] for
         * screenshots.
         *
         * The [streamProvider] is invoked at action time so the dispatcher
         * always acts on the user's currently-selected audio stream.
         */
        fun forContext(
            context: Context,
            streamProvider: () -> AudioStream,
            volumeController: VolumeController = VolumeController(context),
            accessibilityProvider: () -> VolumeAccessibilityService? = { VolumeAccessibilityService.instance },
        ): OverlayActionDispatcher = OverlayActionDispatcher(
            openPanel = {
                val intent = Intent(Settings.Panel.ACTION_VOLUME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            takeScreenshot = {
                accessibilityProvider()?.takeScreenshot()
                    ?: Log.w(TAG, "Screenshot requested but accessibility service is not connected")
            },
            volumeUp = { volumeController.increaseVolume(streamProvider(), showUi = true) },
            volumeDown = { volumeController.decreaseVolume(streamProvider(), showUi = true) },
            toggleMute = { volumeController.toggleMute(streamProvider(), showUi = true) },
            cycleProfile = {
                // Cycling into RINGER_MODE_SILENT requires DND policy access.
                if (volumeController.hasNotificationPolicyAccess()) {
                    volumeController.cycleRingerMode()
                } else {
                    Log.w(TAG, "CycleProfile skipped: notification policy access not granted")
                }
            },
        )
    }
}
