package com.davekingdoms.pixelvolumehelper.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun `default tapAction is OpenPanel`() {
        val prefs = UserPreferences()
        assertEquals(OverlayAction.OpenPanel, prefs.tapAction)
    }

    @Test
    fun `default longPressAction is Screenshot`() {
        val prefs = UserPreferences()
        assertEquals(OverlayAction.Screenshot, prefs.longPressAction)
    }

    @Test
    fun `default overlayX is negative one`() {
        val prefs = UserPreferences()
        assertEquals(-1, prefs.overlayX)
    }

    @Test
    fun `default overlayY is negative one`() {
        val prefs = UserPreferences()
        assertEquals(-1, prefs.overlayY)
    }

    @Test
    fun `custom overlay coordinates are preserved`() {
        val prefs = UserPreferences(overlayX = 150, overlayY = 300)
        assertEquals(150, prefs.overlayX)
        assertEquals(300, prefs.overlayY)
    }
}
