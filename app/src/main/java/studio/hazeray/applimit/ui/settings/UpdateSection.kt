package studio.hazeray.applimit.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Date
import studio.hazeray.applimit.BuildConfig
import studio.hazeray.applimit.R
import studio.hazeray.applimit.data.update.UpdateState

@Composable
fun UpdateSection(viewModel: UpdateSectionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val autoUpdateEnabled by viewModel.autoUpdateEnabled.collectAsStateWithLifecycle()
    val lastCheckedAt by viewModel.lastCheckedAt.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.update_current_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.update_auto_update_label),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.update_auto_update_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = autoUpdateEnabled,
                onCheckedChange = viewModel::setAutoUpdateEnabled
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        UpdateStatusRow(state = state)

        Spacer(modifier = Modifier.height(8.dp))

        UpdateActionRow(
            state = state,
            onCheck = viewModel::checkNow,
            onDownload = viewModel::startDownload,
            onInstall = viewModel::launchInstaller
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.update_last_checked, formatLastChecked(lastCheckedAt)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpdateStatusRow(state: UpdateState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state is UpdateState.Checking || state is UpdateState.Downloading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(16.dp),
                strokeWidth = 2.dp
            )
        }
        Text(
            text = stringForState(state),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun stringForState(state: UpdateState): String = when (state) {
    UpdateState.Idle -> stringResource(R.string.update_status_idle)
    UpdateState.Checking -> stringResource(R.string.update_status_checking)
    UpdateState.UpToDate -> stringResource(R.string.update_status_up_to_date)
    is UpdateState.UpdateAvailable -> stringResource(
        R.string.update_status_available,
        state.version
    )
    is UpdateState.Downloading -> stringResource(R.string.update_status_downloading, state.version)
    is UpdateState.ReadyToInstall -> stringResource(R.string.update_status_ready, state.version)
    is UpdateState.Error -> stringResource(R.string.update_status_error, state.message)
}

@Composable
private fun UpdateActionRow(
    state: UpdateState,
    onCheck: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit
) {
    when (state) {
        is UpdateState.UpdateAvailable -> Button(
            onClick = onDownload,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.update_download_now))
        }
        is UpdateState.ReadyToInstall -> Button(
            onClick = onInstall,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.update_install_now))
        }
        UpdateState.Checking, is UpdateState.Downloading -> Unit
        else -> OutlinedButton(
            onClick = onCheck,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.update_check_now))
        }
    }
}

@Composable
private fun formatLastChecked(timestamp: Long): String {
    if (timestamp <= 0L) return stringResource(R.string.update_last_checked_never)
    return DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT
    ).format(Date(timestamp))
}
