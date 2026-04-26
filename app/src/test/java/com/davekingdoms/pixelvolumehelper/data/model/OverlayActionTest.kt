package com.davekingdoms.pixelvolumehelper.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayActionTest {

    @Test
    fun `entries contains Screenshot action`() {
        assertTrue(
            OverlayAction.entries().contains(OverlayAction.Screenshot),
        )
    }

    @Test
    fun `Screenshot key round-trips through fromKey`() {
        val restored = OverlayAction.fromKey(OverlayAction.Screenshot.key)
        assertEquals(OverlayAction.Screenshot, restored)
    }

    @Test
    fun `fromKey falls back to OpenPanel for unknown key`() {
        assertEquals(OverlayAction.OpenPanel, OverlayAction.fromKey("UNKNOWN"))
    }

    @Test
    fun `all known keys resolve correctly`() {
        for (action in OverlayAction.entries()) {
            assertEquals(action, OverlayAction.fromKey(action.key))
        }
    }
}
