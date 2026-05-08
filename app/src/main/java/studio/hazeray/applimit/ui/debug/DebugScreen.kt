package studio.hazeray.applimit.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import studio.hazeray.applimit.debug.DebugTickRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(viewModel: DebugViewModel, onBack: () -> Unit) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val overlayEnabled by viewModel.overlayEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("デバッグ") },
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
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "デバッグオーバーレイを表示",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = overlayEnabled,
                    onCheckedChange = { viewModel.setOverlayEnabled(it) }
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ティック記録 (${entries.size}件)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                )
                OutlinedButton(onClick = { copyToClipboard(context, entries) }) {
                    Text("コピー")
                }
                OutlinedButton(onClick = { viewModel.clear() }) {
                    Text("クリア")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true
            ) {
                items(entries.asReversed()) { entry ->
                    Text(
                        text = formatEntry(entry),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.JAPAN)

private fun formatEntry(entry: DebugTickRecord): String {
    val time = timeFormat.format(Date(entry.timestamp))
    val pkg = entry.foregroundPackage ?: "(null)"
    val cls = entry.foregroundClassName
        ?.takeIf { it.isNotEmpty() }
        ?.substringAfterLast('.')
        ?.let { "/$it" }
        .orEmpty()
    val targetMark = if (entry.isTarget) "○${entry.targetAppName ?: "?"}" else "✗"
    val sessionPart = entry.sessionState?.let {
        val remaining = entry.remainingMs?.let { ms -> formatRemaining(ms) } ?: "-"
        val ext = if (entry.isExtended) "+" else ""
        " | $it$ext $remaining"
    } ?: ""
    return "$time | fg=$pkg$cls | $targetMark$sessionPart"
}

private fun formatRemaining(ms: Long): String {
    val sec = (ms / 1000).coerceAtLeast(0)
    val m = sec / 60
    val s = sec % 60
    return "%dm%02ds".format(m, s)
}

private fun copyToClipboard(context: Context, entries: List<DebugTickRecord>) {
    val text = entries.joinToString("\n") { formatEntry(it) }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("AppLimit debug log", text))
}
