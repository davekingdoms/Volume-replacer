package com.davekingdoms.pixelvolumehelper.overlay

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Helper to start / stop [OverlayService] and check permission state.
 */
object OverlayManager {

    fun start(context: Context) {
        if (!Settings.canDrawOverlays(context)) return
        val intent = Intent(context, OverlayService::class.java)
        context.startForegroundService(intent)
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, OverlayService::class.java))
    }
}

