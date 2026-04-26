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
}
