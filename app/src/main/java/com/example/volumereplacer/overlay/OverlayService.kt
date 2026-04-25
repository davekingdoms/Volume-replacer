package com.example.volumereplacer.overlay

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Foreground service that draws a volume-control overlay on top of other apps.
 * Requires SYSTEM_ALERT_WINDOW permission.
 */
class OverlayService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // TODO: create overlay window, start foreground notification
    }

    override fun onDestroy() {
        // TODO: remove overlay window
        super.onDestroy()
    }
}

