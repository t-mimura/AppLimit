package studio.hazeray.applimit.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import studio.hazeray.applimit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onSaved: () -> Unit, onDeleted: () -> Unit) {
    val draft by viewModel.draft.collectAsState()
    val deleted by viewModel.deleted.collectAsState()
    val saved by viewModel.saved.collectAsState()

    LaunchedEffect(deleted) {
        if (deleted) onDeleted()
    }
    LaunchedEffect(saved) {
        if (saved) onSaved()
    }

    val app = draft ?: return
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.appName) },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.menu_more)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_button_short)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AppIconHeader(packageName = app.packageName, appName = app.appName)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.enabled_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = app.isEnabled,
                    onCheckedChange = { viewModel.toggleEnabled() }
                )
            }

            // 1..10 step 1, 10..60 step 5
            SliderSetting(
                label = stringResource(R.string.limit_minutes_label),
                value = app.limitMinutes.toFloat(),
                valueRange = 1f..60f,
                valueText = stringResource(R.string.minutes_format, app.limitMinutes),
                onValueChange = { viewModel.updateLimitMinutes(snapMinutes(it)) }
            )

            // 1..10 step 1, 10..120 step 5
            SliderSetting(
                label = stringResource(R.string.cooldown_minutes_label),
                value = app.cooldownMinutes.toFloat(),
                valueRange = 1f..120f,
                valueText = stringResource(R.string.minutes_format, app.cooldownMinutes),
                onValueChange = { viewModel.updateCooldownMinutes(snapMinutes(it)) }
            )

            // 1..10 step 1
            SliderSetting(
                label = stringResource(R.string.extension_minutes_label),
                value = app.extensionMinutes.toFloat(),
                valueRange = 1f..10f,
                valueText = stringResource(R.string.minutes_format, app.extensionMinutes),
                onValueChange = { viewModel.updateExtensionMinutes(snapMinutes(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_button))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteApp()
                    }
                ) {
                    Text(stringResource(R.string.delete_button_short))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

@Composable
private fun AppIconHeader(packageName: String, appName: String) {
    val context = LocalContext.current
    val iconBitmap: ImageBitmap? = remember(packageName) {
        runCatching {
            context.packageManager
                .getApplicationIcon(packageName)
                .toBitmap(width = ICON_PX, height = ICON_PX)
                .asImageBitmap()
        }.getOrNull()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = appName,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
            Text(text = valueText, style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

// Snap to 1-min steps below 10, 5-min steps at/above 10.
private fun snapMinutes(value: Float): Int {
    val v = value.toInt()
    return if (v < 10) v else (v / 5) * 5
}

private const val ICON_PX = 96
