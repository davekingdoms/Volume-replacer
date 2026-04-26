package com.davekingdoms.pixelvolumehelper.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * BroadcastReceiver entry-point for [VolumeWidget].
 * Registered in AndroidManifest when the widget is ready.
 */
class VolumeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VolumeWidget()
}

