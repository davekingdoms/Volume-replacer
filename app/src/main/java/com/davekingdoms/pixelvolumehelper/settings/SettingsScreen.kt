package com.davekingdoms.pixelvolumehelper.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.davekingdoms.pixelvolumehelper.data.model.AudioStream
import com.davekingdoms.pixelvolumehelper.data.model.OverlayAction
import com.davekingdoms.pixelvolumehelper.data.model.OverlayPosition

/**
 * Settings screen exposing permission status, core behavior preferences,
 * and debug/test actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs by viewModel.preferences.collectAsState()
    val permissions by viewModel.permissionState.collectAsState()

    // Refresh permissions every time the screen resumes (user may return from system settings).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshPermissions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── 1. Permission / access status ───────────────────────────
            SectionHeader("Permissions & Access")

            PermissionCard(
                label = "Overlay (Draw over apps)",
                granted = permissions.hasOverlayPermission,
                onAction = { viewModel.openOverlaySettings() },
            )
            PermissionCard(
                label = "Accessibility Service",
                granted = permissions.isAccessibilityEnabled,
                onAction = { viewModel.openAccessibilitySettings() },
            )
            PermissionCard(
                label = "Notification Policy (DND)",
                granted = permissions.hasNotificationPolicyAccess,
                onAction = { viewModel.openNotificationPolicySettings() },
            )
            PermissionCard(
                label = "Battery Optimization Exemption",
                granted = permissions.isIgnoringBatteryOptimizations,
                onAction = { viewModel.openBatteryOptimizationSettings() },
            )

            // ── 2. Core behavior settings ───────────────────────────────
            SectionHeader("Behavior")

            SwitchRow(
                label = "Overlay Enabled",
                checked = prefs.overlayEnabled,
                onCheckedChange = { viewModel.setOverlayEnabled(it) },
            )

            DropdownRow(
                label = "Audio Stream",
                selected = prefs.selectedStream.label,
                options = AudioStream.entries.map { it.label },
                onSelect = { label ->
                    AudioStream.entries.firstOrNull { it.label == label }
                        ?.let { viewModel.setSelectedStream(it) }
                },
            )

            DropdownRow(
                label = "Tap Action",
                selected = prefs.tapAction.label,
                options = OverlayAction.entries().map { it.label },
                onSelect = { label ->
                    OverlayAction.entries().firstOrNull { it.label == label }
                        ?.let { viewModel.setTapAction(it) }
                },
            )

            DropdownRow(
                label = "Long Press Action",
                selected = prefs.longPressAction.label,
                options = OverlayAction.entries().map { it.label },
                onSelect = { label ->
                    OverlayAction.entries().firstOrNull { it.label == label }
                        ?.let { viewModel.setLongPressAction(it) }
                },
            )

            DropdownRow(
                label = "Overlay Position",
                selected = prefs.overlayPosition.label,
                options = OverlayPosition.entries.map { it.label },
                onSelect = { label ->
                    OverlayPosition.entries.firstOrNull { it.label == label }
                        ?.let { viewModel.setOverlayPosition(it) }
                },
            )

            // ── 3. Debug / test actions ─────────────────────────────────
            SectionHeader("Debug Actions")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.openVolumePanel() },
                    modifier = Modifier.weight(1f),
                ) { Text("Volume Panel") }

                OutlinedButton(
                    onClick = { viewModel.openNotificationPolicySettings() },
                    modifier = Modifier.weight(1f),
                ) { Text("DND Access") }
            }

            OutlinedButton(
                onClick = { viewModel.triggerScreenshot() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Take Screenshot (Accessibility)") }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Reusable composables ────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun PermissionCard(
    label: String,
    granted: Boolean,
    onAction: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (granted) "Granted" else "Not granted",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
            FilledTonalButton(onClick = onAction) {
                Text(if (granted) "Open" else "Grant")
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownRow(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            TextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

