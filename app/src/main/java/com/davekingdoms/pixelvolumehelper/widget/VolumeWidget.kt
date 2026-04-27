package com.davekingdoms.pixelvolumehelper.widget

import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.davekingdoms.pixelvolumehelper.data.PreferencesRepository
import com.davekingdoms.pixelvolumehelper.data.model.AudioStream
import kotlinx.coroutines.flow.first

/**
 * Glance home-screen widget rendered as a 6-column × 3-row compact grid:
 *
 * ```
 * ┌─────────┬─────────┬─────────┐
 * │  Sound  │ Vibrate │  Mute   │   ringer mode (one active)
 * ├─────────┴────┬────┴─────────┤
 * │      –       │       +      │   adjust selected stream
 * ├─────────┬────┴────┬─────────┤
 * │  Calls  │  Media  │ System  │   stream selection (one active)
 * └─────────┴─────────┴─────────┘
 * ```
 *
 * The active ringer mode and active stream are highlighted with the accent
 * color so the widget always reflects the current system state.
 */
class VolumeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Pull current system + user state once per render. Action callbacks
        // call [update] to retrigger this whenever something changes.
        val ringerMode =
            (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode
        val selectedStream =
            PreferencesRepository(context).userPreferencesFlow.first().selectedStream

        provideContent {
            WidgetContent(ringerMode = ringerMode, selectedStream = selectedStream)
        }
    }
}

// ── Theme ──────────────────────────────────────────────────────────────────
// Pixel / Material 3 inspired dark surface with a single accent for active
// state. Hard-coded so the widget renders identically regardless of which
// host launcher (or theme) it is dropped on.

private val ContainerBg = Color(0xFF1B1B1F)
private val InactiveBg = Color(0xFF2C2C30)
private val ActiveBg = Color(0xFF6750A4) // accent (matches overlay_bg)
private val ActiveText = Color(0xFFFFFFFF)
private val InactiveText = Color(0xFFE6E1E5)
private val PrimaryBg = Color(0xFF3A3A3F)
private val PrimaryText = Color(0xFFFFFFFF)

@Composable
private fun WidgetContent(ringerMode: Int, selectedStream: AudioStream) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(ContainerBg))
            .cornerRadius(20.dp)
            .padding(8.dp),
    ) {
        RingerRow(ringerMode)
        Spacer(GlanceModifier.height(6.dp))
        VolumeRow()
        Spacer(GlanceModifier.height(6.dp))
        StreamRow(selectedStream)
    }
}

// ── Top row: ringer-mode direct selection ─────────────────────────────────

@Composable
private fun RingerRow(currentMode: Int) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        RingerCell(
            label = "Sound",
            mode = AudioManager.RINGER_MODE_NORMAL,
            active = currentMode == AudioManager.RINGER_MODE_NORMAL,
        )
        Spacer(GlanceModifier.width(6.dp))
        RingerCell(
            label = "Vibrate",
            mode = AudioManager.RINGER_MODE_VIBRATE,
            active = currentMode == AudioManager.RINGER_MODE_VIBRATE,
        )
        Spacer(GlanceModifier.width(6.dp))
        RingerCell(
            label = "Mute",
            mode = AudioManager.RINGER_MODE_SILENT,
            active = currentMode == AudioManager.RINGER_MODE_SILENT,
        )
    }
}

@Composable
private fun RowScope.RingerCell(label: String, mode: Int, active: Boolean) {
    val bg = if (active) ActiveBg else InactiveBg
    val fg = if (active) ActiveText else InactiveText
    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
            .defaultWeight()
            .height(44.dp)
            .background(ColorProvider(bg))
            .cornerRadius(12.dp)
            .clickable(
                actionRunCallback<SetRingerModeAction>(
                    actionParametersOf(WidgetActionKeys.RingerMode to mode),
                ),
            ),
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(fg),
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

// ── Middle row: − / + on the currently selected stream ────────────────────

@Composable
private fun VolumeRow() {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        VolumeCell(label = "−", increase = false)
        Spacer(GlanceModifier.width(6.dp))
        VolumeCell(label = "+", increase = true)
    }
}

@Composable
private fun RowScope.VolumeCell(label: String, increase: Boolean) {
    val onClick = if (increase) {
        actionRunCallback<IncreaseVolumeAction>()
    } else {
        actionRunCallback<DecreaseVolumeAction>()
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
            .defaultWeight()
            .height(56.dp)
            .background(ColorProvider(PrimaryBg))
            .cornerRadius(16.dp)
            .clickable(onClick),
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(PrimaryText),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

// ── Bottom row: stream selection ──────────────────────────────────────────

@Composable
private fun StreamRow(selected: AudioStream) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        StreamCell(
            label = "Calls",
            stream = AudioStream.VOICE_CALL,
            active = selected == AudioStream.VOICE_CALL,
        )
        Spacer(GlanceModifier.width(6.dp))
        StreamCell(
            label = "Media",
            stream = AudioStream.MUSIC,
            active = selected == AudioStream.MUSIC,
        )
        Spacer(GlanceModifier.width(6.dp))
        StreamCell(
            label = "System",
            stream = AudioStream.SYSTEM,
            active = selected == AudioStream.SYSTEM,
        )
    }
}

@Composable
private fun RowScope.StreamCell(label: String, stream: AudioStream, active: Boolean) {
    val bg = if (active) ActiveBg else InactiveBg
    val fg = if (active) ActiveText else InactiveText
    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
            .defaultWeight()
            .height(44.dp)
            .background(ColorProvider(bg))
            .cornerRadius(12.dp)
            .clickable(
                actionRunCallback<SelectStreamAction>(
                    actionParametersOf(WidgetActionKeys.Stream to stream.name),
                ),
            ),
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(fg),
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
