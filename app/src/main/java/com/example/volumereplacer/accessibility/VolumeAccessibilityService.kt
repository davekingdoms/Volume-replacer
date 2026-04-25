package com.example.volumereplacer.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * AccessibilityService stub.
 * Will intercept volume-key events and reroute them.
 */
class VolumeAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // TODO: handle relevant events
    }

    override fun onInterrupt() {
        // TODO: clean-up if interrupted
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // TODO: configure service info if not declared in XML
    }
}

