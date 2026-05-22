package studio.hazeray.applimit.ui.appselect

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import studio.hazeray.applimit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectScreen(viewModel: AppSelectViewModel, onAppAdded: (Long) -> Unit) {
    val filteredApps by viewModel.filteredApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val addedAppId by viewModel.addedAppId.collectAsState()
    val duplicateSelected by viewModel.duplicateSelected.collectAsState()
    val addedPackages by viewModel.addedPackages.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val duplicateMessage = stringResource(R.string.app_already_added_message)

    LaunchedEffect(addedAppId) {
        val id = addedAppId
        if (id != null) {
            viewModel.resetAddedAppId()
            onAppAdded(id)
        }
    }

    LaunchedEffect(duplicateSelected) {
        if (duplicateSelected) {
            snackbarHostState.showSnackbar(duplicateMessage)
            viewModel.resetDuplicateSelected()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_app_title)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text(stringResource(R.string.search_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredApps, key = { it.packageName }) { app ->
                    InstalledAppItem(
                        app = app,
                        alreadyAdded = app.packageName in addedPackages,
                        onClick = { viewModel.selectApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InstalledAppItem(app: InstalledApp, alreadyAdded: Boolean, onClick: () -> Unit) {
    val iconBitmap = remember(app.packageName) {
        app.icon?.toBitmap(width = ICON_PX, height = ICON_PX)?.asImageBitmap()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .alpha(if (alreadyAdded) 0.5f else 1f)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (alreadyAdded) {
            Text(
                text = stringResource(R.string.app_already_added_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private const val ICON_PX = 96
