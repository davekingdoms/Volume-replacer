package com.davekingdoms.pixelvolumehelper.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * AccessibilityService used to perform global actions such as taking screenshots.
 *
 * This service does NOT intercept or reroute physical volume keys.
 * Its sole responsibility is to support the overlay long-press flow by
 * executing [GLOBAL_ACTION_TAKE_SCREENSHOT] and similar system-level actions
 * that are only available through the AccessibilityService API.
 */
class VolumeAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: this service does not monitor accessibility events.
    }

    override fun onInterrupt() {
        // No-op: nothing to clean up.
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /** Takes a screenshot via the accessibility global action. */
    fun takeScreenshot() {
        performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }

    companion object {
        /** Singleton reference set while the service is connected. */
        var instance: VolumeAccessibilityService? = null
            private set
    }
}

