package com.example.volumereplacer.overlay

import android.content.Context
import android.content.Intent

/**
 * Helper to start / stop [OverlayService] and check permission state.
 */
object OverlayManager {

    fun start(context: Context) {
        // TODO: check SYSTEM_ALERT_WINDOW, then startForegroundService
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, OverlayService::class.java))
    }
}

