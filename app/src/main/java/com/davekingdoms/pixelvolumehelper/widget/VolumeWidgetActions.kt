package com.davekingdoms.pixelvolumehelper.widget

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.davekingdoms.pixelvolumehelper.audio.VolumeController
import com.davekingdoms.pixelvolumehelper.data.PreferencesRepository
import com.davekingdoms.pixelvolumehelper.data.model.AudioStream
import kotlinx.coroutines.flow.first

private const val TAG = "VolumeWidget"

/**
 * [ActionParameters.Key] definitions used by widget action callbacks.
 */
internal object WidgetActionKeys {
    /** Ringer-mode constant from [AudioManager] (`RINGER_MODE_*`). */
    val RingerMode: ActionParameters.Key<Int> =
        ActionParameters.Key("ringer_mode")

    /** Name of an [AudioStream] enum entry. */
    val Stream: ActionParameters.Key<String> =
        ActionParameters.Key("stream")
}

/**
 * Set the system ringer mode directly (Sound / Vibrate / Mute).
 *
 * Silent mode is silently ignored if notification-policy access has not
 * been granted; that permission is requested from the settings screen.
 */
class SetRingerModeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val mode = parameters[WidgetActionKeys.RingerMode] ?: return
        val controller = VolumeController(context)
        if (mode == AudioManager.RINGER_MODE_SILENT &&
            !controller.hasNotificationPolicyAccess()
        ) {
            return
        }
        runCatching { controller.setRingerMode(mode) }
            .onFailure { Log.w(TAG, "setRingerMode($mode) failed", it) }
        VolumeWidget().update(context, glanceId)
    }
}

/**
 * Increase the volume of the user's currently selected stream by one step.
 */
class IncreaseVolumeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val stream = PreferencesRepository(context)
            .userPreferencesFlow.first().selectedStream
        runCatching { VolumeController(context).increaseVolume(stream) }
            .onFailure { Log.w(TAG, "increaseVolume($stream) failed", it) }
        VolumeWidget().update(context, glanceId)
    }
}

/**
 * Decrease the volume of the user's currently selected stream by one step.
 */
class DecreaseVolumeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val stream = PreferencesRepository(context)
            .userPreferencesFlow.first().selectedStream
        runCatching { VolumeController(context).decreaseVolume(stream) }
            .onFailure { Log.w(TAG, "decreaseVolume($stream) failed", it) }
        VolumeWidget().update(context, glanceId)
    }
}

/**
 * Persist a new selected stream (Calls / Media / System) so that the
 * +/- buttons and the rest of the app act on that stream.
 */
class SelectStreamAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val key = parameters[WidgetActionKeys.Stream] ?: return
        val stream = AudioStream.fromKey(key)
        PreferencesRepository(context).setSelectedStream(stream)
        VolumeWidget().update(context, glanceId)
    }
}
