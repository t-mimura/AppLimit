package studio.hazeray.applimit.ui.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import studio.hazeray.applimit.BuildConfig
import studio.hazeray.applimit.R
import studio.hazeray.applimit.ui.permission.hasNotificationPermission
import studio.hazeray.applimit.ui.permission.hasUsageStatsPermission
import studio.hazeray.applimit.ui.permission.openHibernationSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(onBack: () -> Unit, onDebug: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsageStats by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasNotification by remember { mutableStateOf(hasNotificationPermission(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageStats = hasUsageStatsPermission(context)
                hasOverlay = Settings.canDrawOverlays(context)
                hasNotification = hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SectionHeader(stringResource(R.string.app_settings_section_permissions))

            PermissionRow(
                label = stringResource(R.string.permission_label_usage_stats),
                granted = hasUsageStats,
                onGrant = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            )
            PermissionRow(
                label = stringResource(R.string.permission_label_overlay),
                granted = hasOverlay,
                onGrant = {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                }
            )
            PermissionRow(
                label = stringResource(R.string.permission_label_notifications),
                granted = hasNotification,
                onGrant = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    context.startActivity(intent)
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                SectionHeader(stringResource(R.string.app_settings_section_other))
                Text(
                    text = stringResource(R.string.hibernation_advice),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { openHibernationSettings(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.open_hibernation_settings))
                }
            }

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                SectionHeader(stringResource(R.string.app_settings_section_debug))
                OutlinedButton(
                    onClick = onDebug,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.app_settings_open_debug))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onGrant: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(
                text = if (granted) "✓" else "✗",
                style = MaterialTheme.typography.titleMedium,
                color = if (granted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        if (granted) {
            Text(
                text = stringResource(R.string.permission_status_granted),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Button(onClick = onGrant) {
                Text(stringResource(R.string.permission_grant_button))
            }
        }
    }
}
