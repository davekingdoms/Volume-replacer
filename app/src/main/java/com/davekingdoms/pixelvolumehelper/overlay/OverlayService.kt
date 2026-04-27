package com.davekingdoms.pixelvolumehelper.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.davekingdoms.pixelvolumehelper.R
import com.davekingdoms.pixelvolumehelper.data.PreferencesRepository
import com.davekingdoms.pixelvolumehelper.data.model.OverlayAction
import com.davekingdoms.pixelvolumehelper.data.model.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Foreground service that draws a volume-control overlay on top of other apps.
 * Requires SYSTEM_ALERT_WINDOW permission.
 *
 * Tap and long-press behavior are user-configurable via [UserPreferences.tapAction]
 * and [UserPreferences.longPressAction] and are dispatched through
 * [OverlayActionDispatcher]. The overlay is draggable; the final position is persisted.
 */
class OverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var actionDispatcher: OverlayActionDispatcher

    /** Latest snapshot of preferences, refreshed before each action. */
    @Volatile
    private var cachedPreferences: UserPreferences = UserPreferences()

    companion object {
        private const val TAG = "OverlayService"
        private const val CHANNEL_ID = "overlay_channel"
        private const val NOTIFICATION_ID = 1
        private const val OVERLAY_SIZE_DP = 48
        private const val LONG_PRESS_TIMEOUT_MS = 500L
        private const val TAP_SLOP_PX = 10
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(applicationContext)
        actionDispatcher = OverlayActionDispatcher.forContext(
            context = applicationContext,
            streamProvider = { cachedPreferences.selectedStream },
        )
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        serviceScope.launch { createOverlay() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Safe to call multiple times — overlay is created once in onCreate.
        return START_STICKY
    }

    override fun onDestroy() {
        removeOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Notification ────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Overlay Service",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps the volume overlay running"
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Volume Helper")
            .setContentText("Overlay is active")
            .setSmallIcon(R.drawable.ic_overlay)
            .setOngoing(true)
            .build()
    }

    // ── Overlay window ──────────────────────────────────────────────────

    private suspend fun createOverlay() {
        if (overlayView != null) return // already created

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val sizePx = (OVERLAY_SIZE_DP * resources.displayMetrics.density).toInt()

        // Simple circle button
        val button = ImageView(this).apply {
            setImageResource(R.drawable.ic_overlay)
            setBackgroundResource(R.drawable.overlay_bg)
            contentDescription = "Volume overlay button"
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            val pad = (8 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val container = FrameLayout(this).apply {
            addView(button, FrameLayout.LayoutParams(sizePx, sizePx))
        }

        // Restore persisted position
        val prefs = preferencesRepository.userPreferencesFlow.first()
        cachedPreferences = prefs
        val startX = if (prefs.overlayX >= 0) prefs.overlayX else 0
        val startY = if (prefs.overlayY >= 0) prefs.overlayY else 200

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = startX
            y = startY
        }

        setupTouchListener(container, params)

        windowManager?.addView(container, params)
        overlayView = container
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Overlay view was already removed", e)
            }
        }
        overlayView = null
    }

    // ── Touch / drag / tap / long-press ─────────────────────────────────

    @Suppress("ClickableViewAccessibility")
    private fun setupTouchListener(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        var longPressTriggered = false

        val longPressRunnable = Runnable {
            longPressTriggered = true
            onLongPress()
        }

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    longPressTriggered = false
                    v.handler?.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT_MS)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (abs(dx) > TAP_SLOP_PX || abs(dy) > TAP_SLOP_PX)) {
                        isDragging = true
                        v.handler?.removeCallbacks(longPressRunnable)
                    }
                    if (isDragging) {
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        windowManager?.updateViewLayout(view, params)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    v.handler?.removeCallbacks(longPressRunnable)
                    if (isDragging) {
                        // Persist final position
                        persistPosition(params.x, params.y)
                    } else if (!longPressTriggered) {
                        onTap()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.handler?.removeCallbacks(longPressRunnable)
                    true
                }

                else -> false
            }
        }
    }

    // ── Actions ─────────────────────────────────────────────────────────

    private fun onTap() {
        runConfiguredAction { it.tapAction }
    }

    private fun onLongPress() {
        runConfiguredAction { it.longPressAction }
    }

    /**
     * Refresh the cached preferences and dispatch the action selected by
     * [selector]. Reading from DataStore is suspending, so the dispatch
     * happens in a coroutine on the main dispatcher.
     *
     * Each invocation dispatches against its own locally-scoped [prefs]
     * snapshot, so concurrent taps cannot cross-contaminate each other's
     * action; [cachedPreferences] is only used as a fallback when reading
     * preferences fails.
     */
    private fun runConfiguredAction(selector: (UserPreferences) -> OverlayAction) {
        serviceScope.launch {
            val prefs = try {
                preferencesRepository.userPreferencesFlow.first()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read preferences; using last known snapshot", e)
                cachedPreferences
            }
            cachedPreferences = prefs
            actionDispatcher.dispatch(selector(prefs))
        }
    }

    private fun persistPosition(x: Int, y: Int) {
        serviceScope.launch {
            preferencesRepository.setOverlayXY(x, y)
        }
    }
}

