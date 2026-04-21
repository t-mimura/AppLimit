package com.example.applimit.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.applimit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onDeleted: () -> Unit) {
    val targetApp by viewModel.targetApp.collectAsState()
    val deleted by viewModel.deleted.collectAsState()

    LaunchedEffect(deleted) {
        if (deleted) onDeleted()
    }

    val app = targetApp ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.appName) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Enabled toggle
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

            // Limit minutes slider
            SliderSetting(
                label = stringResource(R.string.limit_minutes_label),
                value = app.limitMinutes.toFloat(),
                valueRange = 1f..120f,
                valueText = stringResource(R.string.minutes_format, app.limitMinutes),
                onValueChange = { viewModel.updateLimitMinutes(it.toInt()) }
            )

            // Cooldown minutes slider
            SliderSetting(
                label = stringResource(R.string.cooldown_minutes_label),
                value = app.cooldownMinutes.toFloat(),
                valueRange = 15f..480f,
                valueText = stringResource(R.string.minutes_format, app.cooldownMinutes),
                onValueChange = { viewModel.updateCooldownMinutes(it.toInt()) }
            )

            // Extension minutes slider
            SliderSetting(
                label = stringResource(R.string.extension_minutes_label),
                value = app.extensionMinutes.toFloat(),
                valueRange = 1f..30f,
                valueText = stringResource(R.string.minutes_format, app.extensionMinutes),
                onValueChange = { viewModel.updateExtensionMinutes(it.toInt()) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Delete button
            Button(
                onClick = { viewModel.deleteApp() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete_button))
            }
        }
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
            valueRange = valueRange,
            steps = (valueRange.endInclusive - valueRange.start).toInt() - 1
        )
    }
}
