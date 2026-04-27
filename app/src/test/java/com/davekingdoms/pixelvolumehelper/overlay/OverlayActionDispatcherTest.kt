package com.davekingdoms.pixelvolumehelper.overlay

import com.davekingdoms.pixelvolumehelper.data.model.OverlayAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayActionDispatcherTest {

    private class Recorder {
        var openPanel = 0
        var screenshot = 0
        var volumeUp = 0
        var volumeDown = 0
        var mute = 0
        var cycleProfile = 0

        fun build(
            openPanelThrows: Boolean = false,
            screenshotThrows: Boolean = false,
        ) = OverlayActionDispatcher(
            openPanel = {
                openPanel++
                if (openPanelThrows) error("no panel")
            },
            takeScreenshot = {
                screenshot++
                if (screenshotThrows) error("no a11y")
            },
            volumeUp = { volumeUp++ },
            volumeDown = { volumeDown++ },
            toggleMute = { mute++ },
            cycleProfile = { cycleProfile++ },
            onError = { _, _ -> /* swallow in tests, avoid android.util.Log */ },
        )
    }

    @Test
    fun `dispatch routes each action to its handler`() {
        val r = Recorder()
        val d = r.build()

        d.dispatch(OverlayAction.OpenPanel)
        d.dispatch(OverlayAction.Screenshot)
        d.dispatch(OverlayAction.VolumeUp)
        d.dispatch(OverlayAction.VolumeDown)
        d.dispatch(OverlayAction.Mute)
        d.dispatch(OverlayAction.CycleProfile)

        assertEquals(1, r.openPanel)
        assertEquals(1, r.screenshot)
        assertEquals(1, r.volumeUp)
        assertEquals(1, r.volumeDown)
        assertEquals(1, r.mute)
        assertEquals(1, r.cycleProfile)
    }

    @Test
    fun `None action is a no-op and invokes no handler`() {
        val r = Recorder()
        val d = r.build()

        d.dispatch(OverlayAction.None)

        assertEquals(0, r.openPanel)
        assertEquals(0, r.screenshot)
        assertEquals(0, r.volumeUp)
        assertEquals(0, r.volumeDown)
        assertEquals(0, r.mute)
        assertEquals(0, r.cycleProfile)
    }

    @Test
    fun `failing handler does not propagate exception`() {
        val r = Recorder()
        val d = r.build(openPanelThrows = true, screenshotThrows = true)

        // Must not throw — the dispatcher swallows failures gracefully.
        d.dispatch(OverlayAction.OpenPanel)
        d.dispatch(OverlayAction.Screenshot)

        assertEquals(1, r.openPanel)
        assertEquals(1, r.screenshot)
    }

    @Test
    fun `every defined OverlayAction can be dispatched without crashing`() {
        val r = Recorder()
        val d = r.build()

        for (action in OverlayAction.all()) {
            d.dispatch(action)
        }

        // Each non-None action should have fired exactly once.
        val nonNoneCount = OverlayAction.all().count { it != OverlayAction.None }
        val totalInvocations = r.openPanel + r.screenshot + r.volumeUp +
            r.volumeDown + r.mute + r.cycleProfile
        assertEquals(nonNoneCount, totalInvocations)
        assertTrue(OverlayAction.all().contains(OverlayAction.None))
    }
}
