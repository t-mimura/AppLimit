package studio.hazeray.applimit.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import studio.hazeray.applimit.R

@Singleton
class OverlayControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OverlayController {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var overlayView: View? = null

    override fun showCooldownOverlay(
        appName: String,
        remainingMinutes: Int,
        onExtend: () -> Unit,
        onDismiss: () -> Unit
    ) {
        hideOverlay()
        val message = context.getString(
            R.string.cooldown_message,
            appName,
            remainingMinutes
        )
        showOverlay(message, onExtend, onDismiss)
    }

    override fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: IllegalArgumentException) {
                // View not attached
            }
        }
        overlayView = null
    }

    override fun isShowing(): Boolean = overlayView != null

    private fun showOverlay(message: String, onExtend: () -> Unit, onDismiss: () -> Unit) {
        val layout = buildOverlayLayout(message, onExtend, onDismiss)
        val params = buildLayoutParams()
        try {
            windowManager.addView(layout, params)
            overlayView = layout
        } catch (_: WindowManager.BadTokenException) {
            // Overlay permission revoked at runtime
            overlayView = null
        }
    }

    private fun buildOverlayLayout(
        message: String,
        onExtend: () -> Unit,
        onDismiss: () -> Unit
    ): LinearLayout {
        val dp = context.resources.displayMetrics.density

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.argb(230, 0, 0, 0))
            setPadding((32 * dp).toInt(), (32 * dp).toInt(), (32 * dp).toInt(), (32 * dp).toInt())

            addView(
                TextView(context).apply {
                    text = message
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, (32 * dp).toInt())
                }
            )

            addView(
                Button(context).apply {
                    text = context.getString(R.string.extend_button)
                    setOnClickListener { onExtend() }
                    val buttonParams = LinearLayout.LayoutParams(
                        (200 * dp).toInt(),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = (16 * dp).toInt() }
                    layoutParams = buttonParams
                }
            )

            addView(
                Button(context).apply {
                    text = context.getString(R.string.dismiss_button)
                    setOnClickListener { onDismiss() }
                    layoutParams = LinearLayout.LayoutParams(
                        (200 * dp).toInt(),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
    }
}
