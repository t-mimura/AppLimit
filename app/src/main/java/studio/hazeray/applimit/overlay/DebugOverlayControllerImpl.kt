package studio.hazeray.applimit.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugOverlayControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DebugOverlayController {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var view: View? = null
    private var textView: TextView? = null

    override fun update(info: DebugOverlayInfo) {
        if (view == null) show()
        textView?.text = format(info)
    }

    override fun hide() {
        view?.let {
            try {
                windowManager.removeView(it)
            } catch (_: IllegalArgumentException) {
                // View not attached
            }
        }
        view = null
        textView = null
    }

    private fun show() {
        val dp = context.resources.displayMetrics.density
        val tv = TextView(context).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(180, 0, 0, 0))
            textSize = 11f
            setPadding(
                (8 * dp).toInt(),
                (4 * dp).toInt(),
                (8 * dp).toInt(),
                (4 * dp).toInt()
            )
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(tv)
        }
        try {
            windowManager.addView(layout, buildLayoutParams())
            view = layout
            textView = tv
        } catch (_: WindowManager.BadTokenException) {
            // Overlay permission revoked at runtime
        }
    }

    private fun format(info: DebugOverlayInfo): String {
        val header = buildString {
            append("FG: ").append(info.foregroundLabel)
            info.foregroundPackage?.let { append(" [").append(it).append("]") }
            append(" / 対象: ").append(if (info.isTarget) "○" else "✗")
        }
        val detail = if (info.isTarget && info.phase != null && info.remainingMs != null) {
            val phase = when (info.phase) {
                DebugOverlayInfo.Phase.BEFORE_LIMIT -> "制限前"
                DebugOverlayInfo.Phase.EXTENDED -> "延長中"
                DebugOverlayInfo.Phase.COOLDOWN -> "クールダウン"
            }
            "\n$phase 残り ${formatDuration(info.remainingMs)}"
        } else {
            ""
        }
        return header + detail
    }

    private fun formatDuration(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        return "%02d:%02d".format(m, s)
    }

    @Suppress("DEPRECATION")
    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val dp = context.resources.displayMetrics.density
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (8 * dp).toInt()
            y = (8 * dp).toInt()
        }
    }
}
